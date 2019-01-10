package xyz.wildseries.wildstacker.table;

import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import xyz.wildseries.wildstacker.api.objects.StackedEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@SuppressWarnings("WeakerAccess")
public class LootTable {

    static ThreadLocalRandom random = ThreadLocalRandom.current();

    private List<LootPair> lootPairs;
    private int min, max;

    private LootTable(List<LootPair> lootPairs, int min, int max){
        this.lootPairs = new ArrayList<>(lootPairs);
        this.min = min;
        this.max = max;
    }

    public List<ItemStack> getDrops(StackedEntity stackedEntity, int lootBonusLevel){
        List<ItemStack> drops = new ArrayList<>();
        int amountOfPairs = random.nextInt(max - min + 1) + max;

        Bukkit.broadcastMessage("1");

        for(int i = 0; i < amountOfPairs; i++){
            LootPair lootPair;


            do{
                lootPair = getLootPair();
            }while(lootPair != null && (lootPair.isKilledByPlayer() && !isKilledByPlayer(stackedEntity)));

            if(lootPair != null) {
                drops.addAll(lootPair.getItems(stackedEntity, lootBonusLevel));
            }
        }

        return drops;
    }

    private LootPair getLootPair(){
        double chance = random.nextDouble(101);
        double baseChance = 0;

        Collections.shuffle(lootPairs, random);

        for(LootPair lootPair : lootPairs){
            if(chance < baseChance + lootPair.getChance()) {
                return lootPair;
            }else{
                baseChance += lootPair.getChance();
            }
        }

        return null;
    }

    static boolean isBurning(StackedEntity stackedEntity){
        return stackedEntity.getLivingEntity().getFireTicks() > 0;
    }

    static boolean isKilledByPlayer(StackedEntity stackedEntity){
        return stackedEntity.getLivingEntity().getKiller() != null;
    }

    public static LootTable fromJson(JsonObject jsonObject){
        int min = jsonObject.has("min") ? jsonObject.get("min").getAsInt() : 1;
        int max = jsonObject.has("max") ? jsonObject.get("max").getAsInt() : 1;
        List<LootPair> lootPairs = new ArrayList<>();
        if(jsonObject.has("pairs")){
            jsonObject.get("pairs").getAsJsonArray().forEach(element -> lootPairs.add(LootPair.fromJson(element.getAsJsonObject())));
        }
        return new LootTable(lootPairs, min, max);
    }

}
