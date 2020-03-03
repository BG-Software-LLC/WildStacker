package com.bgsoftware.wildstacker.command.commands;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.command.ICommand;
import com.bgsoftware.wildstacker.menu.EditorMainMenu;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public final class CommandSettings implements ICommand {

    @Override
    public String getLabel() {
        return "settings";
    }

    @Override
    public String getUsage() {
        return "stacker settings";
    }

    @Override
    public String getPermission() {
        return "wildstacker.settings";
    }

    @Override
    public String getDescription() {
        return "Open settings editor.";
    }

    @Override
    public int getMinArgs() {
        return 1;
    }

    @Override
    public int getMaxArgs() {
        return 1;
    }

    @Override
    public void perform(WildStackerPlugin plugin, CommandSender sender, String[] args) {
        if(!(sender instanceof Player)){
            sender.sendMessage(ChatColor.RED + "Only players can perform this command.");
            return;
        }

        EditorMainMenu.open((Player) sender);
    }

    @Override
    public List<String> tabComplete(WildStackerPlugin plugin, CommandSender sender, String[] args) {
        return null;
    }
}
