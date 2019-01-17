package xyz.wildseries.wildstacker.loot;

import com.google.gson.JsonObject;
import org.bukkit.inventory.ItemStack;
import xyz.wildseries.wildstacker.WildStackerPlugin;
import xyz.wildseries.wildstacker.api.objects.StackedEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@SuppressWarnings("WeakerAccess")
public class LootTable implements xyz.wildseries.wildstacker.api.loot.LootTable {

    private static WildStackerPlugin plugin = WildStackerPlugin.getPlugin();
    static ThreadLocalRandom random = ThreadLocalRandom.current();

    private List<LootPair> lootPairs;
    private int min, max;

    protected LootTable(List<LootPair> lootPairs, int min, int max){
        this.lootPairs = new ArrayList<>(lootPairs);
        this.min = min;
        this.max = max;
    }


    public List<ItemStack> getDrops(StackedEntity stackedEntity, int lootBonusLevel, int stackAmount){
        List<ItemStack> drops = new ArrayList<>();

        for(int i = 0; i < stackAmount; i++) {
            List<LootPair> lootPairs = getLootPairs(stackedEntity);

            for(LootPair lootPair : lootPairs)
                drops.addAll(lootPair.getItems(stackedEntity, lootBonusLevel));

            drops.addAll(plugin.getNMSAdapter().getEquipment(stackedEntity.getLivingEntity()));
        }

        return drops;
    }

    private List<LootPair> getLootPairs(StackedEntity stackedEntity){
        List<LootPair> lootPairs = new ArrayList<>();
        Collections.shuffle(this.lootPairs, random);

        for(LootPair lootPair : this.lootPairs){
            if(max != -1 && lootPairs.size() >= max)
                break;
            if(lootPair.isKilledByPlayer() && !isKilledByPlayer(stackedEntity))
                continue;
            if(random.nextDouble(101) < lootPair.getChance())
                lootPairs.add(lootPair);
        }

        if(min != -1 && lootPairs.size() < min)
            lootPairs.addAll(getLootPairs(stackedEntity));

        return lootPairs;
    }

    static boolean isBurning(StackedEntity stackedEntity){
        return stackedEntity.getLivingEntity().getFireTicks() > 0;
    }

    static boolean isKilledByPlayer(StackedEntity stackedEntity){
        return stackedEntity.getLivingEntity().getKiller() != null;
    }

    public static LootTable fromJson(JsonObject jsonObject){
        int min = jsonObject.has("min") ? jsonObject.get("min").getAsInt() : -1;
        int max = jsonObject.has("max") ? jsonObject.get("max").getAsInt() : -1;
        List<LootPair> lootPairs = new ArrayList<>();
        if(jsonObject.has("pairs")){
            jsonObject.get("pairs").getAsJsonArray().forEach(element -> lootPairs.add(LootPair.fromJson(element.getAsJsonObject())));
        }
        return new LootTable(lootPairs, min, max);
    }

}
