package com.bgsoftware.wildstacker.command.commands;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.command.ICommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.List;

public final class CommandTest implements ICommand {

    @Override
    public String getLabel() {
        return "test";
    }

    @Override
    public String getUsage() {
        return "stacker test";
    }

    @Override
    public String getPermission() {
        return "wildstacker.test";
    }

    @Override
    public String getDescription() {
        return "Test the status of the stacking thread.";
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
        sender.sendMessage(ChatColor.RED + "This command is temporary disabled.");
    }

    @Override
    public List<String> tabComplete(WildStackerPlugin plugin, CommandSender sender, String[] args) {
        return null;
    }
}
