package com.bgsoftware.wildstacker.command.commands;

import com.bgsoftware.wildstacker.Locale;
import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.enums.SpawnCause;
import com.bgsoftware.wildstacker.command.ICommand;
import com.bgsoftware.wildstacker.objects.WStackedEntity;
import com.bgsoftware.wildstacker.utils.legacy.EntityTypes;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public final class CommandKill implements ICommand {

    private static final Map<String, ApplyFilterFunction> FILTERS = initializeFilters();

    private static Map<String, ApplyFilterFunction> initializeFilters() {
        Map<String, ApplyFilterFunction> filters = new LinkedHashMap<>();

        filters.put("radius=", CommandKill::applyFilterRadius);
        filters.put("type=", CommandKill::applyFilterType);
        filters.put("spawn=", CommandKill::applyFilterSpawn);
        filters.put("config=", CommandKill::applyFilterConfig);

        return Collections.unmodifiableMap(filters);
    }

    @Override
    public String getLabel() {
        return "kill";
    }

    @Override
    public String getUsage() {
        return "stacker kill [radius=?] [type=?] [spawn=?] [config=<true/false>]";
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
        return 5;
    }

    @Override
    public void perform(WildStackerPlugin plugin, CommandSender sender, String[] args) {
        KillFilter killFilter = new KillFilter();

        if (args.length > 1) {
            for (int i = 1; i < args.length; i++) {
                if (args[i].indexOf('=') < 0) {
                    Locale.COMMAND_USAGE.send(sender, getUsage());
                    return;
                }

                String[] sections = args[i].split("=");
                if (sections.length != 2) {
                    Locale.COMMAND_USAGE.send(sender, getUsage());
                    return;
                }

                String type = sections[0].toLowerCase(java.util.Locale.ENGLISH) + "=";

                ApplyFilterFunction filterFunction = FILTERS.get(type);
                if (filterFunction == null) {
                    Locale.COMMAND_USAGE.send(sender, getUsage());
                    return;
                }

                String value = sections[1];

                if (!filterFunction.apply(sender, value, killFilter))
                    return;
            }
        }

        Predicate<Entity> entityPredicate = entity ->
                killFilter.isInRange(((Player) sender).getLocation(), entity.getLocation()) &&
                        killFilter.hasEntityType(EntityTypes.fromEntity((LivingEntity) entity)) &&
                        killFilter.hasSpawnCause(WStackedEntity.of(entity).getSpawnCause());

        Predicate<Item> itemPredicate = item ->
                killFilter.isInRange(((Player) sender).getLocation(), item.getLocation());

        plugin.getSystemManager().performKillAll(entityPredicate, itemPredicate, killFilter.applyTaskFilter);
    }

    @Override
    public List<String> tabComplete(WildStackerPlugin plugin, CommandSender sender, String[] args) {
        List<String> list = new LinkedList<>();

        if (args.length > getMinArgs() && args.length <= getMaxArgs()) {
            String value = args[args.length - 1].toLowerCase(java.util.Locale.ENGLISH);
            for (String type : FILTERS.keySet()) {
                if (type.startsWith(value))
                    list.add(type);
            }
        }

        return list;
    }

    private static boolean applyFilterRadius(CommandSender sender, String value, KillFilter filter) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "You must be a player to use the radius filter.");
            return false;
        }

        try {
            filter.radius = Integer.parseInt(value);
        } catch (Exception ignored) {
            Locale.INVALID_NUMBER.send(sender);
            return false;
        }

        return true;
    }

    private static boolean applyFilterType(CommandSender sender, String value, KillFilter filter) {
        for (String entityType : value.split(",")) {
            try {
                filter.entityTypes.add(EntityTypes.fromName(entityType.toUpperCase()));
            } catch (IllegalArgumentException ex) {
                Locale.INVALID_ENTITY.send(sender, entityType);
                return false;
            }
        }

        return true;
    }

    private static boolean applyFilterSpawn(CommandSender sender, String value, KillFilter filter) {
        for (String spawnCause : value.split(",")) {
            try {
                filter.spawnCauses.add(SpawnCause.valueOf(spawnCause));
            } catch (IllegalArgumentException ex) {
                Locale.INVALID_SPAWN_CAUSE.send(sender, spawnCause);
                return false;
            }
        }

        return true;
    }

    private static boolean applyFilterConfig(CommandSender sender, String value, KillFilter filter) {
        filter.applyTaskFilter = Boolean.parseBoolean(value);
        return true;
    }

    private static class KillFilter {

        private final EnumSet<EntityTypes> entityTypes = EnumSet.noneOf(EntityTypes.class);
        private final EnumSet<SpawnCause> spawnCauses = EnumSet.noneOf(SpawnCause.class);
        private int radius = -1;
        private boolean applyTaskFilter = true;

        private boolean isInRange(Location player, Location entity) {
            if (radius < 0)
                return true;

            return Math.abs(player.getBlockX() - entity.getBlockX()) <= radius &&
                    Math.abs(player.getBlockY() - entity.getBlockY()) <= radius &&
                    Math.abs(player.getBlockZ() - entity.getBlockZ()) <= radius;
        }

        private boolean hasEntityType(EntityTypes entityType) {
            return entityTypes.isEmpty() || entityTypes.contains(entityType);
        }

        private boolean hasSpawnCause(SpawnCause spawnCause) {
            return spawnCauses.isEmpty() || spawnCauses.contains(spawnCause);
        }

    }

    private interface ApplyFilterFunction {

        boolean apply(CommandSender sender, String value, KillFilter filter);

    }

}
