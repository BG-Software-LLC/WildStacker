package com.bgsoftware.wildstacker.loot;

import com.bgsoftware.wildstacker.utils.Random;
import org.bukkit.entity.Player;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"WeakerAccess", "unchecked"})
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

    public List<String> getCommands(Player player, int amountOfCommands){
        List<String> commands = new ArrayList<>();

        this.commands.forEach(command -> {
            for(int i = 0; i < amountOfCommands; i++) {
                int randomNumber = min == null || max == null ? 0 : Random.nextInt(max - min + 1) + min;
                commands.add(command.replace("{player-name}", player.getName()).replace("{number}", String.valueOf(randomNumber)));
            }
        });

        return commands;
    }

    public static LootCommand fromJson(JSONObject jsonObject){
        double chance = (double) jsonObject.getOrDefault("chance", 100D);
        Integer min = (Integer) jsonObject.get("min");
        Integer max = (Integer) jsonObject.get("max");

        List<String> commands = new ArrayList<>();
        if(jsonObject.containsKey("commands")){
            ((JSONArray) jsonObject.get("commands")).forEach(element -> commands.add((String) element));
        }

        return new LootCommand(commands, chance, min, max);
    }

}
