package com.bgsoftware.wildstacker.loot;

import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.loot.LootEntityAttributes;
import com.bgsoftware.wildstacker.utils.ServerVersion;
import com.bgsoftware.wildstacker.utils.json.JsonUtils;
import com.bgsoftware.wildstacker.utils.legacy.Materials;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.Nullable;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LootItem extends FilteredLoot {

    @Nullable
    private static final Material OMINOUS_BOTTLE = getMaterialSafe("OMINOUS_BOTTLE");
    @Nullable
    private static final Material TIPPED_ARROW = getMaterialSafe("TIPPED_ARROW");
    private static final ReflectMethod<Void> POTION_META_SET_BASE_TYPE = new ReflectMethod<>(
            PotionMeta.class, "setBasePotionType", PotionType.class);

    private static final WildStackerPlugin plugin = WildStackerPlugin.getPlugin();

    private final List<ItemModifiers.ItemModifierFunction> itemModifiers = new LinkedList<>();
    private final ItemStack itemStack, burnableItem;
    private final double chance;
    private final boolean looting;

    private LootItem(ItemStack itemStack, @Nullable ItemStack burnableItem, double chance, boolean looting,
                     List<ItemModifiers.ItemModifierFunction> itemModifiers,
                     List<Predicate<LootEntityAttributes>> entityFilters,
                     List<Predicate<LootEntityAttributes>> killerFilters) {
        super(entityFilters, killerFilters);
        this.itemStack = itemStack;
        this.burnableItem = burnableItem;
        this.chance = chance;
        this.looting = looting;
        this.itemModifiers.addAll(itemModifiers);
    }

    public boolean isLooting() {
        return looting;
    }

    public double getChance(int lootBonusLevel, double lootMultiplier) {
        return chance + (lootBonusLevel * lootMultiplier);
    }

    public ItemStack getItemStack(LootEntityAttributes lootEntityAttributes, int amountOfItems, int lootBonusLevel) {
        ItemStack itemStack = lootEntityAttributes.isBurning() && this.burnableItem != null ?
                this.burnableItem.clone() : this.itemStack.clone();

        if (!this.itemModifiers.isEmpty()) {
            ItemMeta itemMeta = itemStack.getItemMeta();

            for (ItemModifiers.ItemModifierFunction function : this.itemModifiers) {
                if (!function.apply(this, itemStack, itemMeta, amountOfItems, lootBonusLevel))
                    return null;
            }

            if (itemMeta != null)
                itemStack.setItemMeta(itemMeta);
        }

        return itemStack;
    }

    @Override
    public String toString() {
        return "LootItem{item=" + itemStack + ",burnable=" + burnableItem + "}";
    }

    public static LootItem fromJson(JSONObject jsonObject) {
        ItemStack itemStack = buildItemStack(jsonObject);
        ItemStack burnableItem = jsonObject.containsKey("burnable") ?
                buildItemStack((JSONObject) jsonObject.get("burnable")) : null;
        double chance = JsonUtils.getDouble(jsonObject, "chance", 100);
        boolean looting = (boolean) jsonObject.getOrDefault("looting", false);

        List<Predicate<LootEntityAttributes>> entityFilters = new LinkedList<>();
        List<Predicate<LootEntityAttributes>> killerFilters = new LinkedList<>();
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

        String requiredPermission = (String) jsonObject.getOrDefault("permission", "");
        if (!requiredPermission.isEmpty())
            killerFilters.add(EntityFilters.checkPermissionFilter(requiredPermission));

        String requiredUpgrade = (String) jsonObject.getOrDefault("upgrade", "");
        if (!requiredUpgrade.isEmpty())
            entityFilters.add(EntityFilters.checkUpgradeFilter(requiredUpgrade));

        try {
            Object jsonKillerFilters = jsonObject.get("killer");
            if (jsonKillerFilters instanceof JSONArray) {
                ((JSONArray) jsonKillerFilters).forEach(filterObject -> {
                    if (filterObject instanceof String)
                        killerFilters.add(EntityFilters.typeFilter((String) filterObject));
                    else if (filterObject instanceof JSONObject)
                        killerFilters.add(EntityFilters.advancedFilter((JSONObject) filterObject));
                });
            } else if (jsonKillerFilters instanceof String) {
                killerFilters.add(EntityFilters.typeFilter((String) jsonKillerFilters));
            }
        } catch (IllegalArgumentException ignored) {
        }

        try {
            Object spawnCauseFilterObject = jsonObject.get("spawn-cause");
            if (spawnCauseFilterObject instanceof String)
                entityFilters.add(EntityFilters.spawnCauseFilter((String) spawnCauseFilterObject));
            else if (spawnCauseFilterObject instanceof JSONArray)
                entityFilters.add(EntityFilters.spawnCausesFilter((JSONArray) spawnCauseFilterObject));
        } catch (IllegalArgumentException ignored) {
        }

        try {
            Object deathCauseFilterObject = jsonObject.get("death-cause");
            if (deathCauseFilterObject instanceof String)
                entityFilters.add(EntityFilters.deathCauseFilter((String) deathCauseFilterObject));
            else if (deathCauseFilterObject instanceof JSONArray)
                entityFilters.add(EntityFilters.deathCausesFilter((JSONArray) deathCauseFilterObject));
        } catch (IllegalArgumentException ignored) {
        }

        try {
            Object slimeSizeFilterObject = jsonObject.get("slime-size");
            if (slimeSizeFilterObject instanceof Number)
                entityFilters.add(EntityFilters.slimeSizeFilter((Number) slimeSizeFilterObject));
        } catch (IllegalArgumentException ignored) {
        }

        try {
            Object captainFilterObject = jsonObject.get("captain");
            if (captainFilterObject instanceof Boolean)
                entityFilters.add(EntityFilters.captainFilter((Boolean) captainFilterObject));
        } catch (IllegalArgumentException ignored) {
        }

        return new LootItem(itemStack, burnableItem, chance, looting, itemModifiers, entityFilters, killerFilters);
    }

    private static ItemStack buildItemStack(JSONObject jsonObject) {
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
    private static Material getMaterialSafe(String name) {
        try {
            return Material.valueOf(name);
        } catch (IllegalArgumentException error) {
            return null;
        }
    }

}
