package xyz.wildseries.wildstacker.command.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.wildseries.wildstacker.WildStackerPlugin;
import xyz.wildseries.wildstacker.command.ICommand;

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

        Player pl = (Player) sender;

        pl.openInventory(plugin.getEditor().getSettingsEditor());
    }

    @Override
    public List<String> tabComplete(WildStackerPlugin plugin, CommandSender sender, String[] args) {
        return null;
    }
}
