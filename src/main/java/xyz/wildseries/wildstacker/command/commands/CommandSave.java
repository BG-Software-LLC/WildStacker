package xyz.wildseries.wildstacker.command.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import xyz.wildseries.wildstacker.WildStackerPlugin;
import xyz.wildseries.wildstacker.command.ICommand;

import java.util.ArrayList;
import java.util.List;

public final  class CommandSave implements ICommand {

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
        return "Save all cached data into files.";
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
        new Thread(() -> {
            plugin.getDataHandler().saveDatabase();
            sender.sendMessage(ChatColor.YELLOW + "Successfully saved all cached data.");
        }).start();
    }

    @Override
    public List<String> tabComplete(WildStackerPlugin plugin, CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}
