package com.willfp.ecoscrolls.scrolls

import com.willfp.ecoscrolls.EcoScrollsPlugin
import com.willfp.ecoscrolls.scrolls.event.ScrollInscribeEvent
import com.willfp.ecoscrolls.scrolls.event.ScrollTryInscribeEvent
import com.willfp.libreforge.NamedValue
import com.willfp.libreforge.ViolationContext
import com.willfp.libreforge.effects.EffectList
import com.willfp.libreforge.effects.Effects
import com.willfp.libreforge.toDispatcher
import com.willfp.libreforge.triggers.TriggerData
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class InscriptionHandler(private val plugin: EcoScrollsPlugin) {
    private val context = ViolationContext(plugin, "Inscriptions")

    private lateinit var applyEffects: EffectList
    private lateinit var denyEffects: EffectList

    val scrollLimit = plugin.configYml.getInt("inscription.scroll-limit")
        .let { if (it <= 0) Int.MAX_VALUE else it }

    init {
        reload()
    }

    internal fun reload() {
        applyEffects = Effects.compile(
            plugin.configYml.getSubsections("inscription.apply-effects"),
            context.with("Apply Effects")
        )

        denyEffects = Effects.compile(
            plugin.configYml.getSubsections("inscription.deny-effects"),
            context.with("Deny Effects")
        )
    }

    private fun inscriptionTrigger(item: ItemStack, scroll: Scroll, player: Player) =
        TriggerData(
            player = player,
            item = item,
            text = scroll.name,
            value = item.getScrollLevel(scroll)?.level?.toDouble() ?: 1.0
        ).dispatch(player.toDispatcher())
            .apply {
                addPlaceholder(
                    NamedValue(
                        "scroll",
                        scroll.name
                    )
                )
                addPlaceholder(
                    NamedValue(
                        "scroll_id",
                        scroll.id
                    )
                )
                addPlaceholder(
                    NamedValue(
                        "level",
                        item.getScrollLevel(scroll)?.level?.toString() ?: "1"
                    )
                )
            }

    fun tryInscribe(item: ItemStack, scroll: Scroll, player: Player): Boolean {
        val tryEvent = ScrollTryInscribeEvent(player, scroll, item)
        plugin.server.pluginManager.callEvent(tryEvent)

        if (tryEvent.isCancelled) {
            return false
        }

        val didInscribe = scroll.inscribe(item, player)

        if (didInscribe) {
            applyEffects.trigger(inscriptionTrigger(item, scroll, player))

            val event = ScrollInscribeEvent(player, scroll, item)
            plugin.server.pluginManager.callEvent(event)
        } else {
            denyEffects.trigger(inscriptionTrigger(item, scroll, player))
        }

        return didInscribe
    }
}
