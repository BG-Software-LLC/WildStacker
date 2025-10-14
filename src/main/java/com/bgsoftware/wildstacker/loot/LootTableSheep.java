package com.bgsoftware.wildstacker.loot;

import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.utils.json.JsonUtils;
import com.bgsoftware.wildstacker.utils.legacy.Materials;
import org.bukkit.entity.Sheep;
import org.bukkit.inventory.ItemStack;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@SuppressWarnings("unchecked")
public class LootTableSheep extends LootTable {

    private LootTableSheep(List<LootPair> lootPairs, int min, int max, int minExp, int maxExp, boolean dropEquipment, boolean alwaysDropsExp) {
        super(lootPairs, min, max, minExp, maxExp, dropEquipment, alwaysDropsExp);
    }

    public static LootTableSheep fromJson(JSONObject jsonObject, String lootTableName) {
        boolean dropEquipment = (boolean) jsonObject.getOrDefault("dropEquipment", true);
        boolean alwaysDropsExp = false;
        int min = (int) jsonObject.getOrDefault("min", -1);
        int max = (int) jsonObject.getOrDefault("max", -1);
        int minExp = -1, maxExp = -1;

        if (jsonObject.containsKey("exp")) {
            JSONObject expObject = (JSONObject) jsonObject.get("exp");
            minExp = JsonUtils.getInt(expObject, "min", -1);
            maxExp = JsonUtils.getInt(expObject, "max", -1);
            alwaysDropsExp = (boolean) expObject.getOrDefault("always-drop", false);
        }

        List<LootPair> lootPairs = new ArrayList<>();
        if (jsonObject.containsKey("pairs")) {
            ((JSONArray) jsonObject.get("pairs")).forEach(element -> lootPairs.add(LootPair.fromJson((JSONObject) element, lootTableName)));
        }

        return new LootTableSheep(lootPairs, min, max, minExp, maxExp, dropEquipment, alwaysDropsExp);
    }

    @Override
    public List<ItemStack> getDrops(StackedEntity stackedEntity, int lootBonusLevel, int stackAmount) {
        List<ItemStack> drops = super.getDrops(stackedEntity, lootBonusLevel, stackAmount);

        if (stackedEntity.getLivingEntity() instanceof Sheep) {
            Sheep sheep = (Sheep) stackedEntity.getLivingEntity();

            if (sheep.isSheared()) {
                drops.removeIf(Materials::isWool);
            } else {
                Iterator<ItemStack> dropsIterator = drops.iterator();
                int woolCount = 0;

                while (dropsIterator.hasNext()) {
                    ItemStack itemStack = dropsIterator.next();
                    if (Materials.isWool(itemStack)) {
                        ++woolCount;
                        dropsIterator.remove();
                    }
                }

                if (woolCount > 0) {
                    ItemStack wool = Materials.getWool(sheep.getColor());
                    wool.setAmount(woolCount);
                    drops.add(wool);
                }
            }
        }

        return drops;
    }

}
