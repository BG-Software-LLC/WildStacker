package com.bgsoftware.wildstacker.loot;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.utils.GeneralUtils;
import com.bgsoftware.wildstacker.utils.Random;
import com.bgsoftware.wildstacker.utils.json.JsonUtils;
import com.bgsoftware.wildstacker.utils.threads.Executor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"WeakerAccess", "unchecked"})
public class LootPair {

    private List<LootItem> lootItems = new ArrayList<>();
    private List<LootCommand> lootCommands = new ArrayList<>();
    private List<String> killer = new ArrayList<>();
    private double chance, lootingChance;
    private String requiredPermission;
    private final List<String> spawnCauseFilter, deathCauseFilter;

    private LootPair(List<LootItem> lootItems, List<LootCommand> lootCommands, List<String> killer, double chance, double lootingChance,
                     String requiredPermission, List<String> spawnCauseFilter, List<String> deathCauseFilter){
        this.lootItems.addAll(lootItems);
        this.lootCommands.addAll(lootCommands);
        this.killer.addAll(killer);
        this.chance = chance;
        this.lootingChance = lootingChance;
        this.requiredPermission = requiredPermission;
        this.spawnCauseFilter = spawnCauseFilter;
        this.deathCauseFilter = deathCauseFilter;
    }

    public List<ItemStack> getItems(StackedEntity stackedEntity, int amountOfPairs, int lootBonusLevel){
        List<ItemStack> items = new ArrayList<>();

        for(LootItem lootItem : lootItems){
            if(!lootItem.getRequiredPermission().isEmpty() && LootTable.isKilledByPlayer(stackedEntity) &&
                    !LootTable.getKiller(stackedEntity).hasPermission(lootItem.getRequiredPermission()))
                continue;

            if(!GeneralUtils.containsOrEmpty(lootItem.getSpawnCauseFilter(), stackedEntity.getSpawnCause().name()))
                continue;

            if(!GeneralUtils.containsOrEmpty(lootItem.getDeathCauseFilter(), LootTable.getDeathCause(stackedEntity)))
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
            int amountOfCommands = (int) (lootCommand.getChance(lootBonusLevel, lootingChance) * amountOfPairs / 100);

            if (amountOfCommands == 0) {
                amountOfCommands = Random.nextChance(lootCommand.getChance(lootBonusLevel, lootingChance), amountOfPairs);
            }

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

    public List<String> getSpawnCauseFilter() {
        return spawnCauseFilter;
    }

    public List<String> getDeathCauseFilter() {
        return deathCauseFilter;
    }

    @Override
    public String toString() {
        return "LootPair{items=" + lootItems + "}";
    }

    public static LootPair fromJson(JSONObject jsonObject, String lootTableName){
        double chance = JsonUtils.getDouble(jsonObject, "chance", 100);
        double lootingChance = JsonUtils.getDouble(jsonObject, "lootingChance", 0);
        String requiredPermission = (String) jsonObject.getOrDefault("permission", "");
        List<LootItem> lootItems = new ArrayList<>();
        List<LootCommand> lootCommands = new ArrayList<>();
        List<String> killer = new ArrayList<>();
        List<String> spawnCauseFilter = new ArrayList<>(), deathCauseFilter = new ArrayList<>();

        Object spawnCauseFilterObject = jsonObject.get("spawn-cause");
        if(spawnCauseFilterObject instanceof String)
            spawnCauseFilter.add(spawnCauseFilterObject + "");
        else if(spawnCauseFilterObject instanceof JSONArray)
            ((JSONArray) spawnCauseFilterObject).forEach(element -> spawnCauseFilter.add(element + ""));

        Object deathCauseFilterObject = jsonObject.get("death-cause");
        if(deathCauseFilterObject instanceof String)
            deathCauseFilter.add(deathCauseFilterObject + "");
        else if(deathCauseFilterObject instanceof JSONArray)
            ((JSONArray) deathCauseFilterObject).forEach(element -> deathCauseFilter.add(element + ""));

        if(jsonObject.containsKey("items")){
            ((JSONArray) jsonObject.get("items")).forEach(element -> {
                try {
                    lootItems.add(LootItem.fromJson((JSONObject) element));
                }catch(IllegalArgumentException ex){
                    WildStackerPlugin.log("[" + lootTableName + "] " + ex.getMessage());
                }
            });
        }

        if(jsonObject.containsKey("commands")){
            ((JSONArray) jsonObject.get("commands")).forEach(element -> lootCommands.add(LootCommand.fromJson((JSONObject) element)));
        }

        if((Boolean) jsonObject.getOrDefault("killedByPlayer", false)){
            killer.add("PLAYER");
        }

        if((Boolean) jsonObject.getOrDefault("killedByCharged", false)){
            killer.add("CHARGED_CREEPER");
        }

        if(jsonObject.containsKey("killer")){
            Object killerObject = jsonObject.get("killer");
            if(killerObject instanceof JSONArray){
                ((JSONArray) killerObject).forEach(type -> killer.add(((String) type).toUpperCase()));
            }else{
                killer.add(((String) killerObject).toUpperCase());
            }
        }

        return new LootPair(lootItems, lootCommands, killer, chance, lootingChance, requiredPermission, spawnCauseFilter, deathCauseFilter);
    }

}
