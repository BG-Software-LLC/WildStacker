package com.bgsoftware.wildstacker.utils.names;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.regex.Pattern;

public class CustomNames {

    private static final Pattern LEGACY_PATTERN = Pattern.compile("LEGACY_");

    private static final WildStackerPlugin plugin = WildStackerPlugin.getPlugin();

    public static String getEggCustomName(EntityType entityType) {
        String typeName = entityType.name();

        if (typeName.contains(String.valueOf(ChatColor.COLOR_CHAR)))
            return typeName;

        String customName = plugin.getSettings().eggsCustomNames.get(typeName);

        if (customName != null)
            return customName;

        return getGlobalCustomName(typeName);
    }

    public static String getItemCustomName(ItemStack itemStack) {
        String typeName = LEGACY_PATTERN.matcher(itemStack.getType().name()).replaceAll("");

        String customName = plugin.getSettings().itemsCustomNames.get(typeName);

        if (customName != null)
            return customName;

        customName = plugin.getSettings().itemsCustomNames.get(typeName + ":" + itemStack.getDurability());

        if (customName != null)
            return customName;

        return getGlobalCustomName(typeName, itemStack);
    }

    public static String getEntityCustomName(EntityType entityType) {
        String typeName = entityType.name();

        if (typeName.contains(String.valueOf(ChatColor.COLOR_CHAR)))
            return typeName;

        String customName = plugin.getSettings().entitiesCustomNames.get(typeName);

        if (customName != null)
            return customName;

        return getGlobalCustomName(typeName);
    }

    public static String getSpawnerCustomName(EntityType entityType) {
        String typeName = entityType.name();

        if (typeName.contains(String.valueOf(ChatColor.COLOR_CHAR)))
            return typeName;

        String customName = plugin.getSettings().spawnersCustomNames.get(typeName);

        if (customName != null)
            return customName;

        return getGlobalCustomName(typeName);
    }

    public static String getBarrelCustomName(ItemStack itemStack) {
        String typeName = LEGACY_PATTERN.matcher(itemStack.getType().name()).replaceAll("");

        String customName = plugin.getSettings().barrelsCustomNames.get(typeName);

        if (customName != null)
            return customName;

        customName = plugin.getSettings().barrelsCustomNames.get(typeName + ":" + itemStack.getDurability());

        if (customName != null)
            return customName;

        return getGlobalCustomName(typeName, itemStack);
    }

    private static String getGlobalCustomName(String typeName) {
        String customName = plugin.getSettings().globalCustomNames.get(typeName);

        if (customName != null)
            return customName;

        return format(typeName);
    }

    private static String getGlobalCustomName(String typeName, ItemStack itemStack) {
        String customName = plugin.getSettings().globalCustomNames.get(typeName);

        if (customName != null)
            return customName;

        customName = plugin.getSettings().globalCustomNames.get(typeName + ":" + itemStack.getDurability());

        if (customName != null)
            return customName;

        return format(typeName);
    }

    public static String format(String type) {
        StringBuilder name = new StringBuilder();

        for (String section : type.split("_"))
            name.append(section.substring(0, 1).toUpperCase()).append(section.substring(1).toLowerCase()).append(" ");

        return name.substring(0, name.length() - 1);
    }

}
