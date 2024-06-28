package com.willfp.ecoscrolls

import com.willfp.ecoscrolls.scrolls.Scrolls
import com.willfp.ecoscrolls.commands.CommandEcoScrolls
import com.willfp.eco.core.command.impl.PluginCommand
import com.willfp.eco.core.display.DisplayModule
import com.willfp.eco.core.integrations.placeholder.PlaceholderManager
import com.willfp.eco.core.placeholder.context.PlaceholderContext
import com.willfp.eco.core.placeholder.templates.DynamicPlaceholder
import com.willfp.ecoscrolls.commands.CommandInscribe
import com.willfp.ecoscrolls.config.TargetsYml
import com.willfp.ecoscrolls.display.ScrollDisplay
import com.willfp.ecoscrolls.gui.updateInscribeMenu
import com.willfp.ecoscrolls.scrolls.ScrollLevel
import com.willfp.ecoscrolls.target.ScrollFinder
import com.willfp.ecoscrolls.target.Targets
import com.willfp.ecoscrolls.util.DiscoverRecipeListener
import com.willfp.libreforge.NamedValue
import com.willfp.libreforge.loader.LibreforgePlugin
import com.willfp.libreforge.loader.configs.ConfigCategory
import com.willfp.libreforge.registerHolderPlaceholderProvider
import com.willfp.libreforge.registerHolderProvider
import org.bukkit.event.Listener
import org.checkerframework.checker.units.qual.m
import java.util.regex.Pattern

internal lateinit var plugin: EcoScrollsPlugin
    private set

class EcoScrollsPlugin : LibreforgePlugin() {
    val targetsYml = TargetsYml(this)

    init {
        plugin = this
    }

    override fun handleEnable() {
        registerHolderProvider(ScrollFinder.toHolderProvider())

        registerHolderPlaceholderProvider<ScrollLevel> { it, _ ->
            listOf(
                NamedValue("level", it.level),
            )
        }

        PlaceholderManager.registerPlaceholder(
            object : DynamicPlaceholder(plugin, Pattern.compile("scroll_([a-z]+)_([a-zA-Z0-9_]+)")) {
                override fun getValue(args: String, context: PlaceholderContext): String? {
                    val matcher = pattern.matcher(args)

                    if (!matcher.matches()) {
                        return null
                    }

                    // Get the scroll and identifier from the matched groups
                    val scroll = matcher.group(1)
                    val identifier = matcher.group(2)

                    // Return empty instead of null for optional placeholders, like hot potato books
                    val scrollInstance = Scrolls[scroll] ?: return ""

                    return scrollInstance.getPlaceholder(identifier, context) ?: ""
                }
            }
        )
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

    override fun createDisplayModule(): DisplayModule {
        return ScrollDisplay(this)
    }
}
