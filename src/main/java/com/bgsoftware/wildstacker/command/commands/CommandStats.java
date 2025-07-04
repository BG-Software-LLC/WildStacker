package com.bgsoftware.wildstacker.command.commands;

import com.bgsoftware.wildstacker.Locale;
import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.command.ICommand;
import org.bukkit.command.CommandSender;

import java.util.List;

public final class CommandStats implements ICommand {

    @Override
    public String getLabel() {
        return "stats";
    }

    @Override
    public String getUsage() {
        return "stacker stats";
    }

    @Override
    public String getPermission() {
        return "wildstacker.stats";
    }

    @Override
    public String getDescription() {
        return "See all the cached stats of WildStacker.";
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
        int entitiesAmount = plugin.getDataHandler().CACHED_ENTITIES.size();
        int unloadedEntitiesAmount = plugin.getDataHandler().CACHED_ENTITIES_RAW.size();
        int itemsAmount = plugin.getDataHandler().CACHED_ITEMS.size();
        int unloadedItemsAmount = plugin.getDataHandler().CACHED_ITEMS_RAW.size();
        int spawnersAmount = plugin.getDataHandler().CACHED_SPAWNERS.size();
        int barrelsAmount = plugin.getDataHandler().CACHED_BARRELS.size();
        int spawnersUnloadedAmount = plugin.getDataHandler().CACHED_SPAWNERS_RAW.size();
        int barrelsUnloadedAmount = plugin.getDataHandler().CACHED_BARRELS_RAW.size();

        String message = "&eWildStacker Stats:" +
                "\n&e - Stacked Entities: (Loaded: " + entitiesAmount + ", Unloaded: " + unloadedEntitiesAmount + ")" +
                "\n&e - Stacked Items: (Loaded: " + itemsAmount + ", Unloaded: " + unloadedItemsAmount + ")" +
                "\n&e - Stacked Spawners: (Loaded: " + spawnersAmount + ", Unloaded: " + spawnersUnloadedAmount + ")" +
                "\n&e - Stacked Barrels: (Loaded: " + barrelsAmount + ", Unloaded: " + barrelsUnloadedAmount + ")";

        Locale.sendMessage(sender, message);
    }

    @Override
    public List<String> tabComplete(WildStackerPlugin plugin, CommandSender sender, String[] args) {
        return null;
    }
}
