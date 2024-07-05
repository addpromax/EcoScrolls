package com.willfp.ecoscrolls.util

import com.willfp.eco.core.items.isEcoEmpty
import com.willfp.ecoscrolls.EcoScrollsPlugin
import com.willfp.ecoscrolls.scrolls.scroll
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent

class DragAndDropListener(private val plugin: EcoScrollsPlugin) : Listener {
    @EventHandler
    fun onDrag(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return

        if (player.gameMode == GameMode.CREATIVE) {
            return
        }

        val current = event.currentItem ?: return
        val cursor = event.cursor ?: return

        if (current.isEcoEmpty) {
            return
        }

        val scroll = cursor.scroll ?: return

        if (!scroll.isDragAndDropEnabled) {
            return
        }

        if (!scroll.canInscribe(current)) {
            return
        }

        val didInscribe = plugin.inscriptionHandler.tryInscribe(current, scroll, player)

        if (didInscribe) {
            cursor.amount -= 1
            event.isCancelled = true
        }
    }
}
