package com.bgsoftware.wildstacker.loot;

import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

@SuppressWarnings("WeakerAccess")
public class LootItem {

    private Material type, burnableType;
    private Map<Enchantment, Integer> enchantments;
    private short data, burnableData;
    private double chance;
    private int min, max;
    private boolean looting;

    private LootItem(Material type, Material burnableType, Map<Enchantment, Integer> enchantments, short data, short burnableData, int min, int max, double chance, boolean looting){
        this.type = type;
        this.burnableType = burnableType;
        this.data = data;
        this.burnableData = burnableData;
        this.min = min;
        this.max = max;
        this.chance = chance;
        this.looting = looting;
        this.enchantments = Maps.newHashMap(enchantments);
    }

    public double getChance(int lootBonusLevel, double lootMultiplier) {
        return chance + (lootBonusLevel * lootMultiplier);
    }

    public ItemStack getItemStack(StackedEntity stackedEntity, int lootBonusLevel){
        ItemStack itemStack = data == 0 ? new ItemStack(type) : new ItemStack(type, 1, data);
        if(LootTable.isBurning(stackedEntity))
            itemStack = burnableData == 0 ? new ItemStack(burnableType) : new ItemStack(burnableType, 1, burnableData);

        int itemAmount = LootTable.random.nextInt(max - min + 1) + min;

        if (looting && lootBonusLevel > 0) {
            itemAmount += LootTable.random.nextInt(lootBonusLevel + 1);
        }

        if(itemAmount > 0)
            itemStack.setAmount(itemAmount);

        itemStack.addEnchantments(enchantments);

        return itemStack;
    }

    public static LootItem fromJson(JsonObject jsonObject){
        Material burnableType;
        Material type = burnableType = Material.valueOf(jsonObject.get("type").getAsString());
        Map<Enchantment, Integer> enchantments = Maps.newHashMap();
        short burnableData;
        short data = burnableData = jsonObject.has("data") ? jsonObject.get("data").getAsShort() : 0;
        double chance = jsonObject.has("chance") ? jsonObject.get("chance").getAsDouble() : 100;
        int min = jsonObject.has("min") ? jsonObject.get("min").getAsInt() : 1;
        int max = jsonObject.has("max") ? jsonObject.get("max").getAsInt() : 1;
        boolean looting = jsonObject.has("looting") && jsonObject.get("looting").getAsBoolean();

        if(jsonObject.has("burnable")){
            JsonObject burnable = jsonObject.get("burnable").getAsJsonObject();
            burnableType = Material.valueOf(burnable.get("type").getAsString());
            burnableData = burnable.has("data") ? burnable.get("data").getAsShort() : 0;
        }

        if(jsonObject.has("enchants")){
            JsonObject enchants = jsonObject.get("enchants").getAsJsonObject();
            for(Map.Entry<String, JsonElement> entry : enchants.entrySet()){
                try {
                    enchantments.put(Enchantment.getByName(entry.getKey()), entry.getValue().getAsInt());
                }catch(Exception ignored){}
            }
        }

        return new LootItem(type, burnableType, enchantments, data, burnableData, min, max, chance, looting);
    }

}
