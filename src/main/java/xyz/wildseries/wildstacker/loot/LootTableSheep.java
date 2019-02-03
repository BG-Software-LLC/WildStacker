package xyz.wildseries.wildstacker.loot;

import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.entity.Sheep;
import org.bukkit.inventory.ItemStack;
import xyz.wildseries.wildstacker.api.objects.StackedEntity;
import xyz.wildseries.wildstacker.utils.legacy.Materials;

import java.util.ArrayList;
import java.util.List;

public class LootTableSheep extends LootTable {

    private static boolean legacy = !Bukkit.getBukkitVersion().contains("1.13");

    private LootTableSheep(List<LootPair> lootPairs, int min, int max, boolean dropEquipment){
        super(lootPairs, min, max, dropEquipment);
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
                        itemStack.setData(wool.getData());
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
        List<LootPair> lootPairs = new ArrayList<>();
        if(jsonObject.has("pairs")){
            jsonObject.get("pairs").getAsJsonArray().forEach(element -> lootPairs.add(LootPair.fromJson(element.getAsJsonObject())));
        }
        return new LootTableSheep(lootPairs, min, max, dropEquipment);
    }

}
