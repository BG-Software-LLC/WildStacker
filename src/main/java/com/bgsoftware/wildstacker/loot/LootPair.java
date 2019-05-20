package com.bgsoftware.wildstacker.loot;

import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.google.gson.JsonObject;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@SuppressWarnings("WeakerAccess")
public class LootPair {

    private List<LootItem> lootItems = new ArrayList<>();
    private List<LootCommand> lootCommands = new ArrayList<>();
    private List<String> killer = new ArrayList<>();
    private double chance, lootingChance;

    private LootPair(List<LootItem> lootItems, List<LootCommand> lootCommands, List<String> killer, double chance, double lootingChance){
        this.lootItems.addAll(lootItems);
        this.lootCommands.addAll(lootCommands);
        this.killer.addAll(killer);
        this.chance = chance;
        this.lootingChance = lootingChance;
    }

    public List<ItemStack> getItems(StackedEntity stackedEntity, int lootBonusLevel){
        List<ItemStack> items = new ArrayList<>();

        LootItem lootItem = getLootItem(lootBonusLevel);

        if(lootItem != null) {
            items.add(lootItem.getItemStack(stackedEntity, lootBonusLevel));
        }

        return items;
    }

    public void executeCommands(Player player, int lootBonusLevel){
        ThreadLocalRandom random = ThreadLocalRandom.current();
        double chance = random.nextDouble(101);
        double baseChance = 0;

        Collections.shuffle(lootCommands, random);

        for(LootCommand lootCommand: lootCommands){
            if(chance < baseChance + lootCommand.getChance(lootBonusLevel, lootingChance)) {
                lootCommand.executeCommands(player);
            }else{
                baseChance += lootCommand.getChance(lootBonusLevel, 0);
            }
        }
    }

    private LootItem getLootItem(int lootBonusLevel){
        ThreadLocalRandom random = ThreadLocalRandom.current();
        double chance = random.nextDouble(101);
        double baseChance = 0;

        Collections.shuffle(lootItems, random);

        for(LootItem lootItem : lootItems){
            if(chance < baseChance + lootItem.getChance(lootBonusLevel, lootingChance)) {
                return lootItem;
            }else{
                baseChance += lootItem.getChance(lootBonusLevel, 0);
            }
        }

        return null;
    }

    public List<String> getKiller(){
        return killer;
    }

    public double getChance() {
        return chance;
    }

    public static LootPair fromJson(JsonObject jsonObject){
        double chance = jsonObject.has("chance") ? jsonObject.get("chance").getAsDouble() : 100;
        double lootingChance = jsonObject.has("lootingChance") ? jsonObject.get("lootingChance").getAsDouble() : 0;
        List<LootItem> lootItems = new ArrayList<>();
        List<LootCommand> lootCommands = new ArrayList<>();
        List<String> killer = new ArrayList<>();
        if(jsonObject.has("items")){
            jsonObject.get("items").getAsJsonArray().forEach(element -> lootItems.add(LootItem.fromJson(element.getAsJsonObject())));
        }
        if(jsonObject.has("commands")){
            jsonObject.get("commands").getAsJsonArray().forEach(element -> lootCommands.add(LootCommand.fromJson(element.getAsJsonObject())));
        }
        if(jsonObject.has("killedByPlayer") && jsonObject.get("killedByPlayer").getAsBoolean()){
            killer.add("PLAYER");
        }
        if(jsonObject.has("killer")){
            if(jsonObject.get("killer").isJsonArray()){
                jsonObject.getAsJsonArray("killer").forEach(type -> killer.add(type.getAsString().toUpperCase()));
            }else{
                killer.add(jsonObject.get("killer").getAsString().toUpperCase());
            }
        }
        return new LootPair(lootItems, lootCommands, killer, chance, lootingChance);
    }

}
