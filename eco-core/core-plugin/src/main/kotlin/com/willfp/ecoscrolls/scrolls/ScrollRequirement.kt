package com.willfp.ecoscrolls.scrolls

import org.bukkit.inventory.ItemStack

class ScrollRequirement(
    val scrollId: String,
    private val level: Int?
) {
    fun isPresent(itemStack: ItemStack): Boolean {
        val scroll = Scrolls[scrollId] ?: return false

        return (itemStack.getScrollLevel(scroll)?.level ?: 0) >= (level ?: 1)
    }
}
