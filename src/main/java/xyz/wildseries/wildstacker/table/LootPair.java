package xyz.wildseries.wildstacker.table;

import com.google.gson.JsonObject;
import org.bukkit.inventory.ItemStack;
import xyz.wildseries.wildstacker.api.objects.StackedEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("WeakerAccess")
public class LootPair {

    private List<LootItem> lootItems = new ArrayList<>();
    private boolean killedByPlayer;
    private int min, max;
    private double chance;

    private LootPair(List<LootItem> lootItems, boolean killedByPlayer, int min, int max, double chance){
        this.lootItems.addAll(lootItems);
        this.killedByPlayer = killedByPlayer;
        this.min = min;
        this.max = max;
        this.chance = chance;
    }

    public List<ItemStack> getItems(StackedEntity stackedEntity, int lootBonusLevel){
        List<ItemStack> items = new ArrayList<>();
        int amountOfItems = LootTable.random.nextInt(max - min + 1) + min;

        for(int i = 0; i < amountOfItems; i++){
            LootItem lootItem = getLootItem();

            if(lootItem != null) {
                items.add(lootItem.getItemStack(stackedEntity, lootBonusLevel));
            }
        }

        return items;
    }

    private LootItem getLootItem(){
        double chance = LootTable.random.nextDouble(101);
        double baseChance = 0;

        Collections.shuffle(lootItems, LootTable.random);

        for(LootItem lootItem : lootItems){
            if(chance < baseChance + lootItem.getChance()) {
                return lootItem;
            }else{
                baseChance += lootItem.getChance();
            }
        }

        return null;
    }

    public boolean isKilledByPlayer(){
        return killedByPlayer;
    }

    public double getChance() {
        return chance;
    }

    public static LootPair fromJson(JsonObject jsonObject){
        boolean killedByPlayer = jsonObject.has("killedByPlayer") && jsonObject.get("killedByPlayer").getAsBoolean();
        double chance = jsonObject.has("chance") ? jsonObject.get("chance").getAsDouble() : 100;
        int min = jsonObject.has("min") ? jsonObject.get("min").getAsInt() : 1;
        int max = jsonObject.has("max") ? jsonObject.get("max").getAsInt() : 1;
        List<LootItem> lootItems = new ArrayList<>();
        if(jsonObject.has("items")){
            jsonObject.get("items").getAsJsonArray().forEach(element -> lootItems.add(LootItem.fromJson(element.getAsJsonObject())));
        }
        return new LootPair(lootItems, killedByPlayer, min, max, chance);
    }

}
