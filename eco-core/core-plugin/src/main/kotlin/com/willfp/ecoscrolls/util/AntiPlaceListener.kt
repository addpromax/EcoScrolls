package com.willfp.ecoscrolls.util

import com.willfp.ecoscrolls.scrolls.scroll
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent

object AntiPlaceListener: Listener {
    @EventHandler
    fun handle(event: BlockPlaceEvent) {
        if (event.itemInHand.scroll != null) {
            event.isCancelled = true
        }
    }
}
