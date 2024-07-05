package com.willfp.ecoscrolls.scrolls.event

import com.willfp.ecoscrolls.scrolls.Scroll
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.HandlerList
import org.bukkit.event.player.PlayerEvent
import org.bukkit.inventory.ItemStack

class ScrollTryInscribeEvent(
    player: Player,
    override val scroll: Scroll,
    val itemStack: ItemStack,
): PlayerEvent(player), Cancellable, ScrollEvent {
    private var cancelled = false

    override fun isCancelled() = cancelled

    override fun setCancelled(cancel: Boolean) {
        cancelled = cancel
    }

    override fun getHandlers(): HandlerList {
        return handlerList
    }

    companion object {
        private val handlerList = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList {
            return handlerList
        }
    }
}
