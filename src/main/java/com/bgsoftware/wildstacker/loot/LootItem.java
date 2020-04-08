package com.bgsoftware.wildstacker.loot;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.utils.Random;
import com.bgsoftware.wildstacker.utils.items.GlowEnchantment;
import com.bgsoftware.wildstacker.utils.json.JsonUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings({"WeakerAccess", "unchecked"})
public class LootItem {

    private static WildStackerPlugin plugin = WildStackerPlugin.getPlugin();

    private final ItemStack itemStack, burnableItem;
    private final double chance;
    private final int min, max;
    private final boolean looting;
    private final String requiredPermission, spawnCauseFilter;

    private LootItem(ItemStack itemStack, @Nullable ItemStack burnableItem, int min, int max, double chance, boolean looting, String requiredPermission, String spawnCauseFilter){
        this.itemStack = itemStack;
        this.burnableItem = burnableItem;
        this.min = min;
        this.max = max;
        this.chance = chance;
        this.looting = looting;
        this.requiredPermission = requiredPermission;
        this.spawnCauseFilter = spawnCauseFilter;
    }

    public double getChance(int lootBonusLevel, double lootMultiplier) {
        return chance + (lootBonusLevel * lootMultiplier);
    }

    public String getRequiredPermission() {
        return requiredPermission;
    }

    public String getSpawnCauseFilter() {
        return spawnCauseFilter;
    }

    public ItemStack getItemStack(StackedEntity stackedEntity, int amountOfItems, int lootBonusLevel){
        ItemStack itemStack = LootTable.isBurning(stackedEntity) && burnableItem != null ? burnableItem.clone() : this.itemStack.clone();

        int lootingBonus = 0;

        if (looting && lootBonusLevel > 0) {
            lootingBonus = Random.nextInt(lootBonusLevel + 1);
        }

        int itemAmount = amountOfItems < 10 ? Random.nextInt(((max + lootingBonus) * amountOfItems) - ((min + lootingBonus) * amountOfItems) + 1) + ((min + lootingBonus) * amountOfItems) :
                Random.nextInt((min + lootingBonus) * amountOfItems, (max + lootingBonus) * amountOfItems);

        if(itemAmount <= 0)
            return null;

        itemStack.setAmount(itemAmount);

        return itemStack;
    }

    @Override
    public String toString() {
        return "LootItem{item=" + itemStack + ",burnable=" + burnableItem + "}";
    }

    public static LootItem fromJson(JSONObject jsonObject){
        ItemStack itemStack = buildItemStack(jsonObject), burnableItem = null;
        double chance = JsonUtils.getDouble(jsonObject, "chance", 100);
        int min = JsonUtils.getInt(jsonObject, "min", 1);
        int max = JsonUtils.getInt(jsonObject, "max", 1);
        boolean looting = (boolean) jsonObject.getOrDefault("looting", false);
        String requiredPermission = (String) jsonObject.getOrDefault("permission", "");
        String spawnCauseFilter = (String) jsonObject.getOrDefault("spawn-cause", "");

        if(jsonObject.containsKey("burnable")){
            burnableItem = buildItemStack((JSONObject) jsonObject.get("burnable"));
        }

        return new LootItem(itemStack, burnableItem, min, max, chance, looting, requiredPermission, spawnCauseFilter);
    }

    private static ItemStack buildItemStack(JSONObject jsonObject){
        Material type;

        try{
            type = Material.valueOf((String) jsonObject.get("type"));
        }catch(IllegalArgumentException ex){
            throw new IllegalArgumentException("Couldn't load item with an invalid material " + jsonObject.get("type") + ".");
        }

        short data = JsonUtils.getShort(jsonObject, "data", (short) 0);

        ItemStack itemStack = new ItemStack(type, 1, data);

        if(jsonObject.containsKey("skull"))
            itemStack = plugin.getNMSAdapter().getPlayerSkull((String) jsonObject.get("skull"));

        ItemMeta itemMeta = itemStack.getItemMeta();

        if(jsonObject.containsKey("name"))
            itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', (String) jsonObject.get("name")));

        if(jsonObject.containsKey("lore")){
            JSONArray jsonArray = (JSONArray) jsonObject.get("lore");
            itemMeta.setLore(((Stream<String>) jsonArray.stream()).map(line ->
                    ChatColor.translateAlternateColorCodes('&', line)).collect(Collectors.toList()));
        }

        if(jsonObject.containsKey("enchants")){
            JSONObject enchants = (JSONObject) jsonObject.get("enchants");
            for(Map.Entry<String, Object> entry : (Set<Map.Entry<String, Object>>) enchants.entrySet()){
                try {
                    itemMeta.addEnchant(Enchantment.getByName(entry.getKey()), (Integer) entry.getValue(), true);
                }catch(Exception ignored){}
            }
        }

        if((Boolean) jsonObject.getOrDefault("glow", false)){
            itemMeta.addEnchant(GlowEnchantment.getGlowEnchant(), 1, true);
        }

        itemStack.setItemMeta(itemMeta);

        if(jsonObject.containsKey("nbt-data")){
            JSONObject nbtData = (JSONObject) jsonObject.get("nbt-data");
            for(Map.Entry<String, Object> entry : (Set<Map.Entry<String, Object>>) nbtData.entrySet()){
                itemStack = plugin.getNMSAdapter().setTag(itemStack, entry.getKey(), entry.getValue());
            }
        }

        return itemStack;
    }

}
