package com.bgsoftware.wildstacker.loot;

import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.utils.legacy.Materials;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.entity.Sheep;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class LootTableSheep extends LootTable {

    private static boolean legacy = !Bukkit.getBukkitVersion().contains("1.13") && !Bukkit.getBukkitVersion().contains("1.14");

    private LootTableSheep(List<LootPair> lootPairs, int min, int max, int minExp, int maxExp, boolean dropEquipment){
        super(lootPairs, min, max, minExp, maxExp, dropEquipment);
    }

    @Override
    public List<ItemStack> getDrops(StackedEntity stackedEntity, int lootBonusLevel, int stackAmount) {
        List<ItemStack> drops = super.getDrops(stackedEntity, lootBonusLevel, stackAmount);

        if(stackedEntity.getLivingEntity() instanceof Sheep) {
            Sheep sheep = (Sheep) stackedEntity.getLivingEntity();
            ItemStack wool = Materials.getWool(sheep.getColor());
            for (ItemStack itemStack : drops) {
                if (itemStack.getType().name().contains("WOOL")) {
                    if (legacy) {
                        //noinspection deprecation
                        itemStack.setDurability(wool.getData().getData());
                    } else {
                        itemStack.setType(wool.getType());
                    }
                }
            }
        }

        return drops;
    }

    public static LootTableSheep fromJson(JsonObject jsonObject){
        boolean dropEquipment = !jsonObject.has("dropEquipment") || jsonObject.get("dropEquipment").getAsBoolean();
        int min = jsonObject.has("min") ? jsonObject.get("min").getAsInt() : -1;
        int max = jsonObject.has("max") ? jsonObject.get("max").getAsInt() : -1;
        int minExp = -1, maxExp = -1;

        if(jsonObject.has("exp")){
            JsonObject expObject = jsonObject.getAsJsonObject("exp");
            minExp = expObject.get("min").getAsInt();
            maxExp = expObject.get("max").getAsInt();
        }

        List<LootPair> lootPairs = new ArrayList<>();
        if(jsonObject.has("pairs")){
            jsonObject.get("pairs").getAsJsonArray().forEach(element -> lootPairs.add(LootPair.fromJson(element.getAsJsonObject())));
        }

        return new LootTableSheep(lootPairs, min, max, minExp, maxExp, dropEquipment);
    }

}
