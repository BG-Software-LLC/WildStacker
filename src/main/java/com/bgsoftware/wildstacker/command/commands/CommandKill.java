package com.bgsoftware.wildstacker.command.commands;

import com.bgsoftware.wildstacker.Locale;
import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.enums.SpawnCause;
import com.bgsoftware.wildstacker.command.ICommand;
import com.bgsoftware.wildstacker.stacker.entities.WStackedEntity;
import com.bgsoftware.wildstacker.utils.legacy.EntityTypes;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public final class CommandKill implements ICommand {

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
        Set<EntityTypes> entityTypes = new HashSet<>();
        Set<SpawnCause> spawnCauses = new HashSet<>();
        IntegerValue integerValue = new IntegerValue(-1);
        boolean applyTaskFilter = true;

        if (args.length > 1) {
            for (int i = 1; i < args.length; i++) {
                if (!args[i].contains("=")) {
                    Locale.COMMAND_USAGE.send(sender, getUsage());
                    return;
                }

                String value = args[i].split("=")[1];
                if (args[i].toLowerCase().startsWith("radius=")) {
                    if (!(sender instanceof Player)) {
                        sender.sendMessage(ChatColor.RED + "You must be a player to use the radius filter.");
                        return;
                    }

                    try {
                        integerValue.i = Integer.parseInt(value);
                    } catch (Exception ignored) {
                        Locale.INVALID_NUMBER.send(sender);
                        return;
                    }
                } else if (args[i].toLowerCase().startsWith("type=")) {
                    for (String entityType : value.split(",")) {
                        try {
                            entityTypes.add(EntityTypes.fromName(entityType.toUpperCase()));
                        } catch (IllegalArgumentException ex) {
                            Locale.INVALID_ENTITY.send(sender, entityType);
                            return;
                        }
                    }
                } else if (args[i].toLowerCase().startsWith("spawn=")) {
                    for (String spawnCause : value.split(",")) {
                        try {
                            spawnCauses.add(SpawnCause.valueOf(spawnCause));
                        } catch (IllegalArgumentException ex) {
                            Locale.INVALID_SPAWN_CAUSE.send(sender, spawnCause);
                            return;
                        }
                    }
                } else if (args[i].toLowerCase().startsWith("config=")) {
                    applyTaskFilter = Boolean.parseBoolean(value);
                }
            }
        }

        Predicate<Entity> entityPredicate = entity ->
                (integerValue.i == -1 || inRadius(((Player) sender).getLocation(), entity.getLocation(), integerValue.i)) &&
                        (entityTypes.isEmpty() || entityTypes.contains(EntityTypes.fromEntity((LivingEntity) entity))) &&
                        (spawnCauses.isEmpty() || spawnCauses.contains(WStackedEntity.of(entity).getSpawnCause()));

        Predicate<Item> itemPredicate = item ->
                integerValue.i == -1 || inRadius(((Player) sender).getLocation(), item.getLocation(), integerValue.i);

        plugin.getSystemManager().performKillAll(entityPredicate, itemPredicate, applyTaskFilter);
    }

    @Override
    public List<String> tabComplete(WildStackerPlugin plugin, CommandSender sender, String[] args) {
        List<String> list = new ArrayList<>();
        List<String> alreadyFiltered = new ArrayList<>();

        for (int i = 1; i < args.length; i++)
            alreadyFiltered.add(args[i].toLowerCase());

        if (args.length > 1 && args.length <= 4) {
            String arg = args[args.length - 1];
            if ("type=".startsWith(arg.toLowerCase()))
                list.add("type=");
            if ("radius=".startsWith(arg.toLowerCase()))
                list.add("radius=");
            if ("spawn=".startsWith(arg.toLowerCase()))
                list.add("spawn=");
            if ("config=".startsWith(arg.toLowerCase()))
                list.add("config=");
        }

        if (alreadyFiltered.stream().anyMatch(arg -> !arg.equals("") && !arg.equals("type=") && !arg.equals("radius=") && !arg.equals("spawn=") && !arg.equals("config="))) {
            list.clear();
        } else {
            list.removeAll(alreadyFiltered);
        }

        return list;
    }

    private boolean inRadius(Location player, Location entity, int radius) {
        return Math.abs(player.getBlockX() - entity.getBlockX()) <= radius &&
                Math.abs(player.getBlockY() - entity.getBlockY()) <= radius &&
                Math.abs(player.getBlockY() - entity.getBlockY()) <= radius;
    }

    private static class IntegerValue {

        private int i;

        IntegerValue(int i) {
            this.i = i;
        }

    }

}
