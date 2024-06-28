package com.willfp.ecoscrolls.scrolls

import com.willfp.eco.core.config.interfaces.Config
import com.willfp.eco.core.items.Items
import com.willfp.eco.core.price.ConfiguredPrice
import com.willfp.eco.core.recipe.Recipes
import com.willfp.eco.core.registry.KRegistrable
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

    private val _item = Items.lookup(config.getString("item.item")).item.apply {
        this.scroll = this@Scroll
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

    val inscriptionConditions = Conditions.compile(
        config.getSubsections("inscription.conditions"),
        context.with("inscription conditions")
    )

    val inscriptionEffects = Effects.compile(
        config.getSubsections("inscription.effects"),
        context.with("inscription effects")
    )

    val inscriptionPrice = ConfiguredPrice.create(
        config.getSubsection("inscription.price"),
    )

    fun getLevel(level: Int): ScrollLevel {
        return levels.getOrPut(level) {
            createLevel(level)
        }
    }

    private fun createLevel(level: Int): ScrollLevel {
        return ScrollLevel(plugin, this, level, effects, conditions)
    }

    fun canInscribe(itemStack: ItemStack): Boolean {
        if (!targets.none { it.matches(itemStack) }) {
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
                    player = player
                ).dispatch(player.toDispatcher())
            )
        ) {
            return false
        }

        if (inscriptionPrice != null) {
            if (!inscriptionPrice.canAfford(player)) {
                return false
            }

            inscriptionPrice.pay(player)
        }

        inscribe(itemStack)

        inscriptionEffects.trigger(
            TriggerData(
                player = player
            ).dispatch(player.toDispatcher())
        )

        return true
    }

    fun inscribe(itemStack: ItemStack) {
        val current = itemStack.scrolls.find { it.scroll == this }?.level ?: 0
        val next = current + 1

        val level = this.getLevel(next)
        itemStack.scrolls = itemStack.scrolls.filter { it.scroll != this }.toSet() + level
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
