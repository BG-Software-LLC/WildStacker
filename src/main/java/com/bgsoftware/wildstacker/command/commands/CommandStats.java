package com.bgsoftware.wildstacker.command.commands;

import com.bgsoftware.wildstacker.Locale;
import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.command.ICommand;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Set;

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
        int entitiesAmount = plugin.getDataHandler().CACHED_ENTITIES.size(),
                entitiesUnloadedAmount = plugin.getDataHandler().CACHED_ENTITIES_RAW.size(),
                itemsAmount = plugin.getDataHandler().CACHED_ITEMS.size(),
                itemsUnloadedAmount = plugin.getDataHandler().CACHED_ITEMS_RAW.size(),
                spawnersAmount = plugin.getDataHandler().CACHED_SPAWNERS.size(),
                barrelsAmount = plugin.getDataHandler().CACHED_BARRELS.size();

        int spawnersUnloadedAmount = 0, barrelsUnloadedAmount = 0;

        for(Set<?> set : plugin.getDataHandler().CACHED_SPAWNERS_RAW.values())
            spawnersUnloadedAmount += set.size();

        for(Set<?> set : plugin.getDataHandler().CACHED_BARRELS_RAW.values())
            barrelsUnloadedAmount += set.size();

        String message = "&eWildStacker Stats:" +
                "\n&e - Stacked Entities: (Loaded: " + entitiesAmount + ", Unloaded: " + entitiesUnloadedAmount + ")" +
                "\n&e - Stacked Items: (Loaded: " + itemsAmount + ", Unloaded: " + itemsUnloadedAmount + ")" +
                "\n&e - Stacked Spawners: (Loaded: " + spawnersAmount + ", Unloaded: " + spawnersUnloadedAmount + ")" +
                "\n&e - Stacked Barrels: (Loaded: " + barrelsAmount + ", Unloaded: " + barrelsUnloadedAmount + ")";

        Locale.sendMessage(sender, message);
    }

    @Override
    public List<String> tabComplete(WildStackerPlugin plugin, CommandSender sender, String[] args) {
        return null;
    }
}
