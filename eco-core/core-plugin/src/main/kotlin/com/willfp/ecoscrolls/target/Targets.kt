package com.willfp.ecoscrolls.target

import com.github.benmanes.caffeine.cache.Caffeine
import com.willfp.eco.core.items.HashedItem
import com.willfp.eco.core.registry.Registry
import com.willfp.ecoscrolls.EcoScrollsPlugin
import com.willfp.ecoscrolls.plugin
import com.willfp.ecoscrolls.scrolls.Scroll
import com.willfp.ecoscrolls.scrolls.Scrolls
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import java.util.concurrent.TimeUnit

object Targets : Registry<Target>() {
    init {
        register(AllTarget)
        update(plugin)
    }

    private fun getForItem(item: ItemStack): List<Target> {
        return values()
            .filter { !it.id.equals("all", ignoreCase = true) }
            .filter { it.matches(item) }
    }

    val ItemStack.isModifiable: Boolean
        get() = modifiableCache.get(HashedItem.of(this)) {
            getForItem(this).isNotEmpty() || this.type == Material.BOOK || this.type == Material.ENCHANTED_BOOK
        }

    val ItemStack.applicableScrolls: List<Scroll>
        get() = canModifyCache.get(HashedItem.of(this)) {
            Scrolls.values().filter { it.canInscribe(this) }
        }

    internal fun update(plugin: EcoScrollsPlugin) {
        for (target in values()) {
            if (target is AllTarget) {
                continue
            }
            remove(target)
        }

        for (config in plugin.targetsYml.getSubsections("targets")) {
            register(ConfiguredTarget(config))
        }

        AllTarget.updateItems()
    }
}

private val modifiableCache = Caffeine.newBuilder()
    .expireAfterAccess(5, TimeUnit.SECONDS)
    .build<HashedItem, Boolean>()

private val canModifyCache = Caffeine.newBuilder()
    .expireAfterAccess(5, TimeUnit.SECONDS)
    .build<HashedItem, List<Scroll>>()
