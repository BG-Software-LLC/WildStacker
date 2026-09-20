package com.bgsoftware.wildstacker.command.commands;

import com.bgsoftware.wildstacker.Locale;
import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.upgrades.SpawnerUpgrade;
import com.bgsoftware.wildstacker.command.ICommand;
import com.bgsoftware.wildstacker.utils.items.ItemUtils;
import com.bgsoftware.wildstacker.utils.names.CustomNames;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class CommandGive implements ICommand {

    @Override
    public String getLabel() {
        return "give";
    }

    @Override
    public String getUsage() {
        return "stacker give [-s] <player-name> <spawner/egg/barrel> <entity-type/material-type> <stack-size> [upgrade]";
    }

    @Override
    public String getPermission() {
        return "wildstacker.give";
    }

    @Override
    public String getDescription() {
        return "Give a stacked spawn-egg or spawner to players.";
    }

    @Override
    public int getMinArgs() {
        return 5;
    }

    @Override
    public int getMaxArgs() {
        return 7;
    }

    @Override
    public void perform(WildStackerPlugin plugin, CommandSender sender, String[] args) {
        boolean silence = false;

        if (args[1].equalsIgnoreCase("-s")) {
            silence = true;
            if (args.length != 6 && args.length != 7) {
                Locale.COMMAND_USAGE.send(sender, getUsage());
                return;
            }
            if (args.length == 7) {
                args = new String[]{"give", args[2], args[3], args[4], args[5], args[6]};
            } else {
                args = new String[]{"give", args[2], args[3], args[4], args[5]};
            }
        }

        Player target = Bukkit.getPlayer(args[1]);

        if (target == null) {
            Locale.INVALID_PLAYER.send(sender, args[1]);
            return;
        }

        int stackSize;

        try {
            stackSize = Integer.parseInt(args[4]);
        } catch (NumberFormatException ex) {
            Locale.INVALID_NUMBER.send(sender);
            return;
        }

        ItemStack itemStack;
        String typeName;

        if (args[2].equalsIgnoreCase("egg")) {
            EntityType entityType;

            try {
                entityType = EntityType.valueOf(args[3].toUpperCase());
                typeName = CustomNames.getEggCustomName(entityType);
            } catch (IllegalArgumentException ex) {
                Locale.INVALID_ENTITY.send(sender, args[3]);
                return;
            }

            SpawnerUpgrade spawnerUpgrade = null;

            if (args.length == 6) {
                spawnerUpgrade = plugin.getUpgradesManager().getUpgrade(args[5]);

                if (spawnerUpgrade == null) {
                    Locale.INVALID_UPGRADE.send(sender, args[5]);
                    return;
                }
            }

            itemStack = ItemUtils.getEggItem(entityType, stackSize, spawnerUpgrade);
        } else if (args[2].equalsIgnoreCase("spawner")) {
            EntityType entityType;

            try {
                entityType = EntityType.valueOf(args[3].toUpperCase());
                typeName = CustomNames.getSpawnerCustomName(entityType);
            } catch (IllegalArgumentException ex) {
                Locale.INVALID_ENTITY.send(sender, args[3]);
                return;
            }

            SpawnerUpgrade spawnerUpgrade = null;

            if (args.length == 6) {
                spawnerUpgrade = plugin.getUpgradesManager().getUpgrade(args[5]);

                if (spawnerUpgrade == null) {
                    Locale.INVALID_UPGRADE.send(sender, args[5]);
                    return;
                }
            }

            itemStack = plugin.getProviders().getSpawnersProvider().getSpawnerItem(entityType,
                    stackSize, spawnerUpgrade);
        } else if (args[2].equalsIgnoreCase("barrel")) {
            Material barrelType;

            try {
                barrelType = Material.getMaterial(args[3].toUpperCase());
                typeName = CustomNames.getBarrelCustomName(new ItemStack(barrelType));
            } catch (IllegalArgumentException | NullPointerException ex) {
                Locale.INVALID_BARREL.send(sender, args[3]);
                return;
            }

            if (!WildStackerPlugin.getPlugin().getSettings().whitelistedBarrels.contains(barrelType)) {
                Locale.INVALID_BARREL.send(sender, args[3]);
                return;
            }

            itemStack = ItemUtils.getBarrelItem(barrelType, stackSize);
        } else {
            Locale.INVALID_TYPE.send(sender);
            return;
        }

        ItemUtils.addItem(itemStack, target.getInventory(), target.getLocation());

        if (!silence || !(sender instanceof Player))
            Locale.STACK_GIVE_PLAYER.send(sender, target.getName(), args[4], typeName, args[2]);

        if (!silence && !target.equals(sender))
            Locale.STACK_RECEIVE.send(target, args[4], typeName + " " + args[2], sender.getName());
    }

    @Override
    public List<String> tabComplete(WildStackerPlugin plugin, CommandSender sender, String[] args) {
        List<String> list = new ArrayList<>();

        if (args.length >= 2 && args[1].equalsIgnoreCase("-s"))
            return tabCompleteSilence(plugin, args);

        switch (args.length) {
            case 2:
                return null;
            case 3:
                Stream.of("egg", "spawner", "barrel")
                        .filter(stack -> stack.toLowerCase().startsWith(args[2]))
                        .forEach(stack -> list.add(stack.toLowerCase()));
                break;
            case 4:
                if (args[2].equalsIgnoreCase("egg") || args[2].equalsIgnoreCase("spawner")) {
                    Arrays.stream(EntityType.values())
                            .filter(entityType -> entityType.name().toLowerCase().startsWith(args[3]) &&
                                    entityType.getEntityClass() != null && LivingEntity.class.isAssignableFrom(entityType.getEntityClass()))
                            .forEach(entityType -> list.add(entityType.name().toLowerCase()));
                } else if (args[2].equalsIgnoreCase("barrel")) {
                    plugin.getSettings().whitelistedBarrels.collect().forEach(mat -> list.add(mat.name().toLowerCase()));
                }
                break;
            case 6:
                if (args[2].equalsIgnoreCase("egg") || args[2].equalsIgnoreCase("spawner")) {
                    EntityType entityType = EntityType.valueOf(args[3].toUpperCase());
                    list.addAll(plugin.getUpgradesManager().getAllUpgrades().stream()
                            .filter(upgrade -> !upgrade.isDefault() && upgrade.isEntityAllowed(entityType) &&
                                    upgrade.getName().toLowerCase().startsWith(args[5]))
                            .map(upgrade -> upgrade.getName().toLowerCase()).collect(Collectors.toList()));
                }
                break;
        }

        return list;
    }

    private List<String> tabCompleteSilence(WildStackerPlugin plugin, String[] args) {
        List<String> list = new ArrayList<>();

        switch (args.length) {
            case 3:
                return null;
            case 4:
                Stream.of("egg", "spawner", "barrel")
                        .filter(stack -> stack.toLowerCase().startsWith(args[3]))
                        .forEach(stack -> list.add(stack.toLowerCase()));
                break;
            case 5:
                if (args[3].equalsIgnoreCase("egg") || args[3].equalsIgnoreCase("spawner")) {
                    Arrays.stream(EntityType.values())
                            .filter(entityType -> entityType.name().toLowerCase().startsWith(args[4]) &&
                                    entityType.getEntityClass() != null && LivingEntity.class.isAssignableFrom(entityType.getEntityClass()))
                            .forEach(entityType -> list.add(entityType.name().toLowerCase()));
                } else if (args[3].equalsIgnoreCase("barrel")) {
                    plugin.getSettings().whitelistedBarrels.collect().forEach(mat -> list.add(mat.name().toLowerCase()));
                }
                break;
            case 7:
                if (args[3].equalsIgnoreCase("egg") || args[3].equalsIgnoreCase("spawner")) {
                    EntityType entityType = EntityType.valueOf(args[4].toUpperCase());
                    list.addAll(plugin.getUpgradesManager().getAllUpgrades().stream()
                            .filter(upgrade -> !upgrade.isDefault() && upgrade.isEntityAllowed(entityType) &&
                                    upgrade.getName().toLowerCase().startsWith(args[6]))
                            .map(upgrade -> upgrade.getName().toLowerCase()).collect(Collectors.toList()));
                }
                break;
        }

        return list;
    }

}
