package com.bgsoftware.wildstacker.loot;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.utils.threads.Executor;
import com.bgsoftware.wildstacker.utils.Random;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("WeakerAccess")
public class LootPair {

    private List<LootItem> lootItems = new ArrayList<>();
    private List<LootCommand> lootCommands = new ArrayList<>();
    private List<String> killer = new ArrayList<>();
    private double chance, lootingChance;
    private String requiredPermission, spawnCauseFilter;

    private LootPair(List<LootItem> lootItems, List<LootCommand> lootCommands, List<String> killer, double chance, double lootingChance, String requiredPermission, String spawnCauseFilter){
        this.lootItems.addAll(lootItems);
        this.lootCommands.addAll(lootCommands);
        this.killer.addAll(killer);
        this.chance = chance;
        this.lootingChance = lootingChance;
        this.requiredPermission = requiredPermission;
        this.spawnCauseFilter = spawnCauseFilter;
    }

    public List<ItemStack> getItems(StackedEntity stackedEntity, int amountOfPairs, int lootBonusLevel){
        List<ItemStack> items = new ArrayList<>();

        for(LootItem lootItem : lootItems){
            if(!lootItem.getRequiredPermission().isEmpty() && LootTable.isKilledByPlayer(stackedEntity) &&
                    !LootTable.getKiller(stackedEntity).hasPermission(lootItem.getRequiredPermission()))
                continue;

            if(!lootItem.getSpawnCauseFilter().isEmpty() && !stackedEntity.getSpawnCause().name().equals(lootItem.getSpawnCauseFilter()))
                continue;

            int amountOfItems = (int) (lootItem.getChance(lootBonusLevel, lootingChance) * amountOfPairs / 100);

            if (amountOfItems == 0) {
                amountOfItems = Random.nextChance(lootItem.getChance(lootBonusLevel, lootingChance), amountOfPairs);
            }

            ItemStack itemStack = lootItem.getItemStack(stackedEntity, amountOfItems, lootBonusLevel);
            if(itemStack != null)
                items.add(itemStack);
        }

        return items;
    }

    public void executeCommands(Player player, int amountOfPairs, int lootBonusLevel){
        List<String> commands = new ArrayList<>();

        for(LootCommand lootCommand : lootCommands){
            int amountOfCommands = (int) Math.round(lootCommand.getChance(lootBonusLevel, lootingChance) * amountOfPairs / 100);
            commands.addAll(lootCommand.getCommands(player, amountOfCommands));
        }

        Executor.sync(() -> commands.forEach(command -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command)));
    }

    public List<String> getKiller(){
        return killer;
    }

    public double getChance() {
        return chance;
    }

    public String getRequiredPermission() {
        return requiredPermission;
    }

    public String getSpawnCauseFilter() {
        return spawnCauseFilter;
    }

    @Override
    public String toString() {
        return "LootPair{items=" + lootItems + "}";
    }

    public static LootPair fromJson(JsonObject jsonObject, String lootTableName){
        double chance = jsonObject.has("chance") ? jsonObject.get("chance").getAsDouble() : 100;
        double lootingChance = jsonObject.has("lootingChance") ? jsonObject.get("lootingChance").getAsDouble() : 0;
        String requiredPermission = jsonObject.has("permission") ? jsonObject.get("permission").getAsString() : "";
        String spawnCauseFilter = jsonObject.has("spawn-cause") ? jsonObject.get("spawn-cause").getAsString() : "";
        List<LootItem> lootItems = new ArrayList<>();
        List<LootCommand> lootCommands = new ArrayList<>();
        List<String> killer = new ArrayList<>();

        if(jsonObject.has("items")){
            jsonObject.get("items").getAsJsonArray().forEach(element -> {
                try {
                    lootItems.add(LootItem.fromJson(element.getAsJsonObject()));
                }catch(IllegalArgumentException ex){
                    WildStackerPlugin.log("[" + lootTableName + "] " + ex.getMessage());
                }
            });
        }

        if(jsonObject.has("commands")){
            jsonObject.get("commands").getAsJsonArray().forEach(element -> lootCommands.add(LootCommand.fromJson(element.getAsJsonObject())));
        }

        if(jsonObject.has("killedByPlayer") && jsonObject.get("killedByPlayer").getAsBoolean()){
            killer.add("PLAYER");
        }

        if(jsonObject.has("killedByCharged") && jsonObject.get("killedByCharged").getAsBoolean()){
            killer.add("CHARGED_CREEPER");
        }

        if(jsonObject.has("killer")){
            if(jsonObject.get("killer").isJsonArray()){
                jsonObject.getAsJsonArray("killer").forEach(type -> killer.add(type.getAsString().toUpperCase()));
            }else{
                killer.add(jsonObject.get("killer").getAsString().toUpperCase());
            }
        }

        return new LootPair(lootItems, lootCommands, killer, chance, lootingChance, requiredPermission, spawnCauseFilter);
    }

}
