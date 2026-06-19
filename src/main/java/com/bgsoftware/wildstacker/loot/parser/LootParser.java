package com.bgsoftware.wildstacker.loot.parser;

import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.loot.LootEntityAttributes;
import com.bgsoftware.wildstacker.loot.ItemModifiers;
import com.bgsoftware.wildstacker.loot.LootCommand;
import com.bgsoftware.wildstacker.loot.LootItem;
import com.bgsoftware.wildstacker.loot.LootPair;
import com.bgsoftware.wildstacker.loot.LootTable;
import com.bgsoftware.wildstacker.loot.LootTableSheep;
import com.bgsoftware.wildstacker.loot.filters.EntityFilters;
import com.bgsoftware.wildstacker.utils.ServerVersion;
import com.bgsoftware.wildstacker.utils.json.JsonUtils;
import com.bgsoftware.wildstacker.utils.legacy.Materials;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.Nullable;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LootParser {

    private static final WildStackerPlugin plugin = WildStackerPlugin.getPlugin();

    private static final ReflectMethod<Void> POTION_META_SET_BASE_TYPE = new ReflectMethod<>(
            PotionMeta.class, "setBasePotionType", PotionType.class);

    @Nullable
    private static final Material OMINOUS_BOTTLE = Materials.getMaterialOrNull("OMINOUS_BOTTLE");
    @Nullable
    private static final Material TIPPED_ARROW = Materials.getMaterialOrNull("TIPPED_ARROW");

    public static LootTable parseLootTable(JSONObject jsonObject, String lootTableName) {
        boolean dropEquipment = (boolean) jsonObject.getOrDefault("dropEquipment", true);
        boolean alwaysDropsExp = false;
        int min = JsonUtils.getInt(jsonObject, "min", -1);
        int max = JsonUtils.getInt(jsonObject, "max", -1);
        int minExp = -1, maxExp = -1;

        if (jsonObject.containsKey("exp")) {
            JSONObject expObject = (JSONObject) jsonObject.get("exp");
            minExp = JsonUtils.getInt(expObject, "min", -1);
            maxExp = JsonUtils.getInt(expObject, "max", -1);
            alwaysDropsExp = (boolean) expObject.getOrDefault("always-drop", false);
        }

        List<LootPair> lootPairs = new ArrayList<>();
        if (jsonObject.containsKey("pairs")) {
            ((JSONArray) jsonObject.get("pairs")).forEach(element ->
                    lootPairs.add(parseLootPair(((JSONObject) element), lootTableName)));
        }

        boolean isSheep = lootTableName.startsWith("sheep");

        return isSheep ? new LootTableSheep(lootPairs, min, max, minExp, maxExp, dropEquipment, alwaysDropsExp) :
            new LootTable(lootPairs, min, max, minExp, maxExp, dropEquipment, alwaysDropsExp);
    }

    public static LootPair parseLootPair(JSONObject jsonObject, String lootTableName) {
        double chance = JsonUtils.getDouble(jsonObject, "chance", 100);
        double lootingChance = JsonUtils.getDouble(jsonObject, "lootingChance", 0);
        List<LootItem> lootItems = new ArrayList<>();
        List<LootCommand> lootCommands = new ArrayList<>();

        List<Predicate<LootEntityAttributes>> entityFilters = new ArrayList<>();
        List<Predicate<LootEntityAttributes>> killerFilters = new ArrayList<>();
        List<Predicate<LootEntityAttributes>> vehicleFilters = new ArrayList<>();

        parseEntitiesListInternal(jsonObject.get("killer"), killerFilters);
        parseEntitiesListInternal(jsonObject.get("vehicle"), vehicleFilters);
        parseGenericLootEntryExtraFiltersInternal(jsonObject, entityFilters, killerFilters);

        if (jsonObject.containsKey("items")) {
            ((JSONArray) jsonObject.get("items")).forEach(element -> {
                try {
                    lootItems.add(parseLootItem((JSONObject) element));
                } catch (IllegalArgumentException ex) {
                    WildStackerPlugin.log("[" + lootTableName + "] " + ex.getMessage());
                }
            });
        }

        if (jsonObject.containsKey("commands")) {
            ((JSONArray) jsonObject.get("commands")).forEach(element ->
                    lootCommands.add(parseLootCommand((JSONObject) element)));
        }

        return new LootPair(lootItems, lootCommands, chance, lootingChance, entityFilters, killerFilters, vehicleFilters);
    }

    public static LootItem parseLootItem(JSONObject jsonObject) {
        ItemStack itemStack = parseItemStackInternal(jsonObject);
        ItemStack burnableItem = jsonObject.containsKey("burnable") ?
                parseItemStackInternal((JSONObject) jsonObject.get("burnable")) : null;
        double chance = JsonUtils.getDouble(jsonObject, "chance", 100);

        LootItem.Looting looting = parseLootingInternal(jsonObject.get("looting"));

        List<Predicate<LootEntityAttributes>> entityFilters = new LinkedList<>();
        List<Predicate<LootEntityAttributes>> killerFilters = new LinkedList<>();
        List<Predicate<LootEntityAttributes>> vehicleFilters = new LinkedList<>();
        List<ItemModifiers.ItemModifierFunction> itemModifiers = new LinkedList<>();

        {
            int min = JsonUtils.getInt(jsonObject, "min", 1);
            int max = JsonUtils.getInt(jsonObject, "max", 1);
            int limit = JsonUtils.getInt(jsonObject, "limit", -1);
            itemModifiers.add(ItemModifiers.countModifier(min, max, limit));
        }

        // Custom case for ominous bottle
        if (itemStack.getType() == OMINOUS_BOTTLE && ServerVersion.isAtLeast(ServerVersion.v1_21) &&
                jsonObject.containsKey("amplifier")) {
            try {
                JSONObject amplifier = (JSONObject) jsonObject.get("amplifier");
                int min = JsonUtils.getInt(amplifier, "min", 0);
                int max = JsonUtils.getInt(amplifier, "max", 4);
                itemModifiers.add(ItemModifiers.ominousBottleModifier(min, max));
            } catch (Exception ignored) {
            }
        }

        parseEntitiesListInternal(jsonObject.get("killer"), killerFilters);
        parseEntitiesListInternal(jsonObject.get("vehicle"), vehicleFilters);
        parseGenericLootEntryExtraFiltersInternal(jsonObject, entityFilters, killerFilters);

        return new LootItem(itemStack, burnableItem, chance, looting, itemModifiers, entityFilters, killerFilters, vehicleFilters);
    }

    public static LootCommand parseLootCommand(JSONObject jsonObject) {
        double chance = JsonUtils.getDouble(jsonObject, "chance", 100D);
        int min = JsonUtils.getInt(jsonObject, "min", -1);
        int max = JsonUtils.getInt(jsonObject, "max", -1);
        String requiredPermission = (String) jsonObject.getOrDefault("permission", "");

        List<String> commands = new LinkedList<>();
        if (jsonObject.containsKey("commands")) {
            ((JSONArray) jsonObject.get("commands")).forEach(element -> commands.add((String) element));
        }

        return new LootCommand(commands, chance, min, max, requiredPermission);
    }

    private static ItemStack parseItemStackInternal(JSONObject jsonObject) {
        Material type;

        try {
            type = Material.valueOf((String) jsonObject.get("type"));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Couldn't load item with an invalid material " + jsonObject.get("type") + ".");
        }

        short data = JsonUtils.getShort(jsonObject, "data", (short) 0);

        ItemStack itemStack = new ItemStack(type, 1, data);

        if (jsonObject.containsKey("skull"))
            itemStack = plugin.getNMSAdapter().getPlayerSkull(Materials.PLAYER_HEAD.toBukkitItem(), (String) jsonObject.get("skull"));

        ItemMeta itemMeta = itemStack.getItemMeta();

        if (jsonObject.containsKey("name"))
            itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', (String) jsonObject.get("name")));

        if (jsonObject.containsKey("lore")) {
            JSONArray jsonArray = (JSONArray) jsonObject.get("lore");
            itemMeta.setLore(((Stream<String>) jsonArray.stream()).map(line ->
                    ChatColor.translateAlternateColorCodes('&', line)).collect(Collectors.toList()));
        }

        if (jsonObject.containsKey("enchants")) {
            JSONObject enchants = (JSONObject) jsonObject.get("enchants");
            for (Map.Entry<String, Object> entry : (Set<Map.Entry<String, Object>>) enchants.entrySet()) {
                try {
                    itemMeta.addEnchant(Enchantment.getByName(entry.getKey()), (Integer) entry.getValue(), true);
                } catch (Exception ignored) {
                }
            }
        }

        if ((Boolean) jsonObject.getOrDefault("glow", false)) {
            plugin.getNMSAdapter().makeItemGlow(itemMeta);
        }

        if (itemStack.getType() == TIPPED_ARROW && jsonObject.containsKey("arrow-effect") &&
                POTION_META_SET_BASE_TYPE.isValid()) {
            try {
                PotionMeta potionMeta = (PotionMeta) itemMeta;
                POTION_META_SET_BASE_TYPE.invoke(potionMeta, PotionType.valueOf((String) jsonObject.get("arrow-effect")));
            } catch (Exception ignored) {

            }
        }

        itemStack.setItemMeta(itemMeta);

        if (jsonObject.containsKey("nbt-data")) {
            JSONObject nbtData = (JSONObject) jsonObject.get("nbt-data");
            for (Map.Entry<String, Object> entry : (Set<Map.Entry<String, Object>>) nbtData.entrySet()) {
                itemStack = plugin.getNMSAdapter().setTag(itemStack, entry.getKey(), entry.getValue());
            }
        }

        return itemStack;
    }

    @Nullable
    private static LootItem.Looting parseLootingInternal(@Nullable Object value) {
        if (value == null)
            return null;

        if (value instanceof JSONObject) {
            int min = JsonUtils.getInt((JSONObject) value, "min", LootItem.Looting.DEFAULT.getMin());
            int max = JsonUtils.getInt((JSONObject) value, "max", LootItem.Looting.DEFAULT.getMax());
            return min == LootItem.Looting.DEFAULT.getMin() && max == LootItem.Looting.DEFAULT.getMax() ?
                    LootItem.Looting.DEFAULT : new LootItem.Looting(min, max);
        }

        // We assume the value is boolean.
        boolean isLooting = (boolean) value;

        return isLooting ? LootItem.Looting.DEFAULT : null;
    }

    private static void parseGenericLootEntryExtraFiltersInternal(JSONObject entry,
                                                                  List<Predicate<LootEntityAttributes>> entityFilters,
                                                                  List<Predicate<LootEntityAttributes>> killerFilters) {
        // Parse entity filters
        String requiredUpgrade = (String) entry.getOrDefault("upgrade", "");
        if (!requiredUpgrade.isEmpty())
            entityFilters.add(EntityFilters.checkUpgradeFilter(requiredUpgrade));

        try {
            Object spawnCauseFilterObject = entry.get("spawn-cause");
            if (spawnCauseFilterObject instanceof String)
                entityFilters.add(EntityFilters.spawnCauseFilter((String) spawnCauseFilterObject));
            else if (spawnCauseFilterObject instanceof JSONArray)
                entityFilters.add(EntityFilters.spawnCausesFilter((JSONArray) spawnCauseFilterObject));
        } catch (IllegalArgumentException ignored) {
        }

        try {
            Object deathCauseFilterObject = entry.get("death-cause");
            if (deathCauseFilterObject instanceof String)
                entityFilters.add(EntityFilters.deathCauseFilter((String) deathCauseFilterObject));
            else if (deathCauseFilterObject instanceof JSONArray)
                entityFilters.add(EntityFilters.deathCausesFilter((JSONArray) deathCauseFilterObject));
        } catch (IllegalArgumentException ignored) {
        }

        try {
            Object slimeSizeFilterObject = entry.get("slime-size");
            if (slimeSizeFilterObject instanceof Number)
                entityFilters.add(EntityFilters.slimeSizeFilter((Number) slimeSizeFilterObject));
        } catch (IllegalArgumentException ignored) {
        }

        try {
            Object captainFilterObject = entry.get("captain");
            if (captainFilterObject instanceof Boolean)
                entityFilters.add(EntityFilters.captainFilter((Boolean) captainFilterObject));
        } catch (IllegalArgumentException ignored) {
        }

        // Parse killer filters
        String requiredPermission = (String) entry.getOrDefault("permission", "");
        if (!requiredPermission.isEmpty())
            killerFilters.add(EntityFilters.checkPermissionFilter(requiredPermission));

        if ((Boolean) entry.getOrDefault("killedByPlayer", false)) {
            killerFilters.add(entityAttributes -> entityAttributes.getEntityType() == EntityType.PLAYER);
        }

        if ((Boolean) entry.getOrDefault("killedByCharged", false)) {
            killerFilters.add(EntityFilters.creeperChargedFilter(true));
        }
    }

    private static void parseEntitiesListInternal(Object entities, List<Predicate<LootEntityAttributes>> filters) {
        try {
            if (entities instanceof JSONArray) {
                ((JSONArray) entities).forEach(filterObject -> {
                    if (filterObject instanceof String)
                        filters.add(EntityFilters.typeFilter((String) filterObject));
                    else if (filterObject instanceof JSONObject)
                        filters.add(EntityFilters.advancedFilter((JSONObject) filterObject));
                });
            } else if (entities instanceof String) {
                filters.add(EntityFilters.typeFilter((String) entities));
            }
        } catch (IllegalArgumentException ignored) {
        }
    }

    private LootParser() {

    }

}
