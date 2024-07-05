package com.willfp.ecoscrolls.scrolls.event

import com.willfp.ecoscrolls.scrolls.Scroll
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import org.bukkit.event.player.PlayerEvent
import org.bukkit.inventory.ItemStack

class ScrollInscribeEvent(
    player: Player,
    override val scroll: Scroll,
    val itemStack: ItemStack
): PlayerEvent(player), ScrollEvent {
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
