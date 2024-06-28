package com.willfp.ecoscrolls.target

import com.willfp.ecoscrolls.scrolls.ScrollLevel
import com.willfp.ecoscrolls.scrolls.scrolls
import com.willfp.libreforge.slot.ItemHolderFinder
import com.willfp.libreforge.slot.SlotType
import org.bukkit.inventory.ItemStack

object ScrollFinder : ItemHolderFinder<ScrollLevel>() {
    override fun find(item: ItemStack): List<ScrollLevel> {
        return item.scrolls.toList()
    }

    override fun isValidInSlot(holder: ScrollLevel, slot: SlotType): Boolean {
        return holder.scroll.targets.map { it.slot }.any { it.isOrContains(slot) }
    }
}
