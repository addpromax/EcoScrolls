package com.willfp.ecoscrolls

import com.willfp.ecoscrolls.scrolls.Scrolls
import com.willfp.ecoscrolls.commands.CommandEcoScrolls
import com.willfp.eco.core.command.impl.PluginCommand
import com.willfp.ecoscrolls.commands.CommandInscribe
import com.willfp.ecoscrolls.config.TargetsYml
import com.willfp.ecoscrolls.gui.updateInscribeMenu
import com.willfp.ecoscrolls.target.ScrollFinder
import com.willfp.ecoscrolls.target.Targets
import com.willfp.ecoscrolls.util.DiscoverRecipeListener
import com.willfp.libreforge.SimpleProvidedHolder
import com.willfp.libreforge.loader.LibreforgePlugin
import com.willfp.libreforge.loader.configs.ConfigCategory
import com.willfp.libreforge.registerGenericHolderProvider
import com.willfp.libreforge.registerHolderProvider
import org.bukkit.event.Listener

internal lateinit var plugin: EcoScrollsPlugin
    private set

class EcoScrollsPlugin : LibreforgePlugin() {
    val targetsYml = TargetsYml(this)

    override fun handleEnable() {
        registerHolderProvider(ScrollFinder.toHolderProvider())
    }

    override fun handleReload() {
        updateInscribeMenu(this)
        Targets.update(this)
    }

    override fun loadConfigCategories(): List<ConfigCategory> {
        return listOf(
            Scrolls
        )
    }

    override fun loadPluginCommands(): List<PluginCommand> {
        return listOf(
            CommandEcoScrolls(this),
            CommandInscribe(this)
        )
    }

    override fun loadListeners(): List<Listener> {
        return listOf(
            DiscoverRecipeListener(this)
        )
    }
}
