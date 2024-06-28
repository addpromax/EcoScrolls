package com.willfp.ecoscrolls.commands

import com.willfp.eco.core.EcoPlugin
import com.willfp.eco.core.command.impl.PluginCommand
import org.bukkit.command.CommandSender

class CommandEcoScrolls(plugin: EcoPlugin) : PluginCommand(
    plugin,
    "ecoscrolls",
    "ecoscrolls.command.ecoscrolls",
    false
) {
    init {
        this.addSubcommand(CommandReload(plugin))
            .addSubcommand(CommandGive(plugin))
    }

    override fun onExecute(sender: CommandSender, args: List<String>) {
        sender.sendMessage(
            plugin.langYml.getMessage("invalid-command")
        )
    }

    override fun getAliases(): List<String> {
        return listOf(
            "scrolls"
        )
    }
}
