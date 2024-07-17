package com.willfp.ecoscrolls.libreforge

import com.willfp.eco.core.config.interfaces.Config
import com.willfp.ecoscrolls.scrolls.Scroll
import com.willfp.ecoscrolls.scrolls.ScrollLevel
import com.willfp.ecoscrolls.scrolls.Scrolls
import com.willfp.libreforge.Dispatcher
import com.willfp.libreforge.NoCompileData
import com.willfp.libreforge.ProvidedHolder
import com.willfp.libreforge.arguments
import com.willfp.libreforge.conditions.Condition
import com.willfp.libreforge.effects.Effect
import com.willfp.libreforge.getHoldersOfType
import com.willfp.libreforge.triggers.TriggerData
import com.willfp.libreforge.triggers.TriggerParameter

object EffectInscribeItem : Effect<NoCompileData>("inscribe_item") {
    override val isPermanent = false

    override val arguments = arguments {
        require("scroll", "You must specify the scroll!")
    }

    override fun onTrigger(config: Config, data: TriggerData, compileData: NoCompileData): Boolean {
        val item = data.foundItem ?: return false

        val scroll = Scrolls[config.getString("scroll")] ?: return false

        scroll.inscribe(item)

        return true
    }
}
