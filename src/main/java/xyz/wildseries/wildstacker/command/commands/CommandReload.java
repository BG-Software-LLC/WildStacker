package xyz.wildseries.wildstacker.command.commands;

import org.bukkit.command.CommandSender;
import xyz.wildseries.wildstacker.Locale;
import xyz.wildseries.wildstacker.WildStackerPlugin;
import xyz.wildseries.wildstacker.command.ICommand;
import xyz.wildseries.wildstacker.handlers.LootHandler;
import xyz.wildseries.wildstacker.handlers.SettingsHandler;
import xyz.wildseries.wildstacker.tasks.KillTask;
import xyz.wildseries.wildstacker.tasks.SaveTask;
import xyz.wildseries.wildstacker.tasks.StackTask;

import java.util.ArrayList;
import java.util.List;

public final class CommandReload implements ICommand {

    @Override
    public String getLabel() {
        return "reload";
    }

    @Override
    public String getUsage() {
        return "stacker reload";
    }

    @Override
    public String getPermission() {
        return "wildstacker.reload";
    }

    @Override
    public String getDescription() {
        return "Reload the settings and the language files.";
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
            SettingsHandler.reload();
            LootHandler.reload();
            Locale.reload();
            KillTask.start();
            SaveTask.start();
            StackTask.start();
            plugin.getEditor().reloadConfiguration();
            Locale.RELOAD_SUCCESS.send(sender);
        }).start();
    }

    @Override
    public List<String> tabComplete(WildStackerPlugin plugin, CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}
