package com.willfp.ecoscrolls.scrolls

import com.willfp.eco.core.EcoPlugin
import com.willfp.eco.core.items.tag.CustomItemTag
import org.bukkit.inventory.ItemStack

class ScrollTag(plugin: EcoPlugin) : CustomItemTag(plugin.createNamespacedKey("scroll")) {
    override fun matches(p0: ItemStack): Boolean {
        return p0.scroll != null
    }

    override fun getExampleItem(): ItemStack? {
        return Scrolls.values().randomOrNull()?.item
    }
}
