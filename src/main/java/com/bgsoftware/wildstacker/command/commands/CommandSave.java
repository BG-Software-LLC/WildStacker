package com.bgsoftware.wildstacker.command.commands;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.command.ICommand;
import com.bgsoftware.wildstacker.utils.threads.Executor;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public final class CommandSave implements ICommand {

    @Override
    public String getLabel() {
        return "save";
    }

    @Override
    public String getUsage() {
        return "stacker save";
    }

    @Override
    public String getPermission() {
        return "wildstacker.save";
    }

    @Override
    public String getDescription() {
        return "Save cached entities and items to database.";
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
        Executor.async(() -> {
            long startTime = System.currentTimeMillis();
            plugin.getSystemManager().performCacheSave();
            sender.sendMessage(ChatColor.YELLOW + "Saved all cached entities & items (" + (System.currentTimeMillis() - startTime) + "ms).");
        });
    }

    @Override
    public List<String> tabComplete(WildStackerPlugin plugin, CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}
