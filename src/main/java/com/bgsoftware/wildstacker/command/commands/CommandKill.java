package com.bgsoftware.wildstacker.command.commands;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.command.ICommand;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public final class CommandKill implements ICommand {

    @Override
    public String getLabel() {
        return "kill";
    }

    @Override
    public String getUsage() {
        return "stacker kill";
    }

    @Override
    public String getPermission() {
        return "wildstacker.kill";
    }

    @Override
    public String getDescription() {
        return "Kill all the stacked mobs in the server.";
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
        plugin.getSystemManager().performKillAll();
    }

    @Override
    public List<String> tabComplete(WildStackerPlugin plugin, CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}
