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
        int entitiesAmount = plugin.getSystemManager().getStackedEntities().size(),
                itemsAmount = plugin.getSystemManager().getStackedItems().size(),
                spawnersAmount = plugin.getSystemManager().getStackedSpawners().size(),
                barrelsAmount = plugin.getSystemManager().getStackedBarrels().size(),
                entitiesCachedAmounts = plugin.getDataHandler().CACHED_AMOUNT_ENTITIES.size(),
                itemsCachedAmount = plugin.getDataHandler().CACHED_AMOUNT_ITEMS.size();

        String message = "&eWildStacker Stats:" +
                "\n&e - Stacked Entities: " + entitiesAmount +
                "\n&e - Stacked Items: " + itemsAmount +
                "\n&e - Stacked Spawners: " + spawnersAmount +
                "\n&e - Stacked Barrels: " + barrelsAmount +
                "\n&e - Cached Entities Amount (Not Objects): " + entitiesCachedAmounts +
                "\n&e - Cached Items Amount (Not Objects): " + itemsCachedAmount;

        Locale.sendMessage(sender, message);
    }

    @Override
    public List<String> tabComplete(WildStackerPlugin plugin, CommandSender sender, String[] args) {
        return null;
    }
}
