package com.willfp.ecoscrolls.scrolls

import com.willfp.eco.core.config.interfaces.Config
import com.willfp.eco.core.display.Display
import com.willfp.eco.core.fast.FastItemStack
import com.willfp.eco.core.fast.fast
import com.willfp.eco.core.items.Items
import com.willfp.eco.core.placeholder.InjectablePlaceholder
import com.willfp.eco.core.placeholder.PlaceholderInjectable
import com.willfp.eco.core.placeholder.context.PlaceholderContext
import com.willfp.eco.core.placeholder.context.placeholderContext
import com.willfp.eco.core.placeholder.templates.DynamicInjectablePlaceholder
import com.willfp.eco.core.price.ConfiguredPrice
import com.willfp.eco.core.recipe.Recipes
import com.willfp.eco.core.registry.KRegistrable
import com.willfp.eco.util.formatEco
import com.willfp.ecoscrolls.EcoScrollsPlugin
import com.willfp.ecoscrolls.plugin
import com.willfp.ecoscrolls.target.Targets
import com.willfp.libreforge.ViolationContext
import com.willfp.libreforge.conditions.Conditions
import com.willfp.libreforge.effects.Effects
import com.willfp.libreforge.toDispatcher
import com.willfp.libreforge.triggers.TriggerData
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.Objects
import java.util.regex.Pattern

class Scroll(
    plugin: EcoScrollsPlugin,
    override val id: String,
    val config: Config
) : KRegistrable {
    private val context = ViolationContext(plugin, "scroll $id")

    private val levels = mutableMapOf<Int, ScrollLevel>()

    private val effects = Effects.compile(
        config.getSubsections("effects"),
        context.with("effects")
    )

    private val conditions = Conditions.compile(
        config.getSubsections("conditions"),
        context.with("conditions")
    )

    val name = config.getFormattedString("name")

    val maxLevel = config.getInt("max-level")

    private val itemName = config.getString("item.name")
    private val itemLore = config.getStrings("item.lore")

    private val _item = run {
        val base = Items.lookup(config.getString("item.item")).item
        val fis = base.fast()
        fis.scroll = this
        fis.displayName = itemName.formatEco()
        fis.lore = itemLore.formatEco().map { Display.PREFIX + it } + fis.lore
        fis.unwrap()
    }

    val item: ItemStack
        get() = _item.clone()

    val recipe = if (config.getBool("item.craftable")) Recipes.createAndRegisterRecipe(
        plugin,
        id,
        item,
        config.getStrings("item.recipe")
    ) else null

    val targets = config.getStrings("targets")
        .mapNotNull { Targets[it] }

    private val conflicts = config.getStrings("conflicts")

    private val requirements = config.getSubsections("requirements")
        .map {
            ScrollRequirement(
                it.getString("scroll"),
                it.getIntOrNull("level")
            )
        }

    val removeRequirements = config.getBool("remove-requirements")

    val inscriptionConditions = Conditions.compile(
        config.getSubsections("inscription.conditions"),
        context.with("inscription conditions")
    )

    val inscriptionEffects = Effects.compile(
        config.getSubsections("inscription.effects"),
        context.with("inscription effects")
    )

    val inscriptionPrice = ConfiguredPrice.createOrFree(
        config.getSubsection("inscription.price"),
    )

    val isDragAndDropEnabled = config.getBool("inscription.drag-and-drop")

    val isInscriptionTableEnabled = config.getBool("inscription.inscription-table")

    private val lore = config.getStrings("lore")

    private val levelPlaceholder = object : DynamicInjectablePlaceholder(Pattern.compile("level")) {
        override fun getValue(p0: String, p1: PlaceholderContext): String? {
            return p1.itemStack?.getScrollLevel(this@Scroll)?.level?.toString()
        }
    }

    private val levelInjectable = object : PlaceholderInjectable {
        override fun getPlaceholderInjections(): List<InjectablePlaceholder> {
            return listOf(
                levelPlaceholder
            )
        }

        override fun addInjectablePlaceholder(p0: MutableIterable<InjectablePlaceholder>) {
            //
        }

        override fun clearInjectedPlaceholders() {
            //
        }
    }

    fun getLevel(level: Int): ScrollLevel {
        return levels.getOrPut(level) {
            createLevel(level)
        }
    }

    private fun createLevel(level: Int): ScrollLevel {
        return ScrollLevel(plugin, this, level, effects, conditions)
    }

    fun canInscribe(itemStack: ItemStack): Boolean {
        if (targets.none { it.matches(itemStack) }) {
            return false
        }

        val currentScrolls = itemStack.scrolls

        if (currentScrolls.size >= plugin.inscriptionHandler.scrollLimit) {
            return false
        }

        if (currentScrolls.any { it.scroll.conflictsWith(this) }) {
            return false
        }

        if (requirements.any { !it.isPresent(itemStack) }) {
            return false
        }

        val level = itemStack.getScrollLevel(this)?.level ?: 0

        if (level >= maxLevel) {
            return false
        }

        return true
    }

    fun inscribe(itemStack: ItemStack, player: Player): Boolean {
        if (!canInscribe(itemStack)) {
            return false
        }

        if (!inscriptionConditions.areMetAndTrigger(
                TriggerData(
                    player = player,
                    item = itemStack
                ).dispatch(player.toDispatcher())
            )
        ) {
            return false
        }

        if (!inscriptionPrice.canAfford(player)) {
            return false
        }

        inscriptionPrice.pay(player)

        inscribe(itemStack)

        inscriptionEffects.trigger(
            TriggerData(
                player = player,
                item = itemStack,
                value = itemStack.getScrollLevel(this)?.level?.toDouble() ?: 1.0
            ).dispatch(player.toDispatcher())
        )

        return true
    }

    fun inscribe(itemStack: ItemStack) {
        val current = itemStack.scrolls.find { it.scroll == this }?.level ?: 0
        val next = current + 1

        val level = this.getLevel(next)

        var scrolls = itemStack.scrolls.filter { it.scroll != this }.toSet() + level

        if (removeRequirements) {
            scrolls = scrolls.filter { scroll ->
                !requirements.any { it.scrollId == scroll.scroll.id }
            }.toSet()
        }

        itemStack.scrolls = scrolls
    }

    fun getLore(itemStack: ItemStack, player: Player?): List<String> {
        return lore.formatEco(
            placeholderContext(
                player = player,
                item = itemStack,
                injectable = levelInjectable
            )
        ).map { Display.PREFIX + it }
    }

    fun displayScroll(fis: FastItemStack, player: Player?) {
        fis.displayName = itemName.formatEco(player = player)
        fis.lore = itemLore.formatEco(placeholderContext(player = player))
            .map { Display.PREFIX + it } + fis.lore
    }

    fun getPlaceholder(identifier: String, context: PlaceholderContext): String? {
        if ((levelPlaceholder.getValue(levelPlaceholder.pattern.pattern(), context)?.toIntOrNull() ?: 0) < 1) {
            return null
        }

        val expression = config.getString("placeholders.$identifier")
        return expression.formatEco(context.withInjectableContext(levelInjectable))
    }

    fun conflictsWith(other: Scroll): Boolean {
        return this.conflicts.contains(other.id) ||
                other.conflicts.contains(this.id)
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Scroll) {
            return false
        }

        return this.id == other.id
    }

    override fun hashCode(): Int {
        return Objects.hash(this.id)
    }
}
