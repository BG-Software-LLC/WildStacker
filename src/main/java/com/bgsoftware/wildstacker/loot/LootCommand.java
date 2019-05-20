package com.bgsoftware.wildstacker.loot;

import com.bgsoftware.wildstacker.utils.Executor;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@SuppressWarnings("WeakerAccess")
public class LootCommand {

    private List<String> commands = new ArrayList<>();
    private double chance;
    private Integer min, max;

    private LootCommand(List<String> commands, double chance, Integer min, Integer max){
        this.commands.addAll(commands);
        this.chance = chance;
        this.min = min;
        this.max = max;
    }

    public double getChance(int lootBonusLevel, double lootMultiplier) {
        return chance + (lootBonusLevel * lootMultiplier);
    }

    public void executeCommands(Player player){
        ThreadLocalRandom random = ThreadLocalRandom.current();
        List<String> commands = new ArrayList<>();

        for(String command : this.commands){
            int randomNumber = min == null || max == null ? 0 : random.nextInt(max - min + 1) + min;
            commands.add(command.replace("{player-name}", player.getName()).replace("{number}", String.valueOf(randomNumber)));
        }

        Executor.sync(() -> commands.forEach(command -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command)));
    }

    public static LootCommand fromJson(JsonObject jsonObject){
        double chance = jsonObject.has("chance") ? jsonObject.get("chance").getAsDouble() : 100;
        Integer min = jsonObject.has("min") ? jsonObject.get("min").getAsInt() : null;
        Integer max = jsonObject.has("max") ? jsonObject.get("max").getAsInt() : null;

        List<String> commands = new ArrayList<>();
        if(jsonObject.has("commands")){
            jsonObject.getAsJsonArray("commands").forEach(element -> commands.add(element.getAsString()));
        }

        return new LootCommand(commands, chance, min, max);
    }

}
