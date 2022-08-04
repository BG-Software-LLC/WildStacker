package com.bgsoftware.wildstacker.loot;

import com.bgsoftware.wildstacker.utils.Random;
import com.bgsoftware.wildstacker.utils.json.JsonUtils;
import org.bukkit.entity.Player;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@SuppressWarnings({"WeakerAccess", "unchecked"})
public class LootCommand {

    private final List<String> commands = new LinkedList<>();
    private final double chance;
    private final int min, max;
    private final String requiredPermission;

    private LootCommand(List<String> commands, double chance, int min, int max, String requiredPermission) {
        this.commands.addAll(commands);
        this.chance = chance;
        this.min = min;
        this.max = max;
        this.requiredPermission = requiredPermission;
    }

    public static LootCommand fromJson(JSONObject jsonObject) {
        double chance = JsonUtils.getDouble(jsonObject, "chance", 100D);
        int min = JsonUtils.getInt(jsonObject, "min", -1);
        int max = JsonUtils.getInt(jsonObject, "max", -1);
        String requiredPermission = (String) jsonObject.getOrDefault("permission", "");

        List<String> commands = new ArrayList<>();
        if (jsonObject.containsKey("commands")) {
            ((JSONArray) jsonObject.get("commands")).forEach(element -> commands.add((String) element));
        }

        return new LootCommand(commands, chance, min, max, requiredPermission);
    }

    public double getChance(int lootBonusLevel, double lootMultiplier) {
        return chance + (lootBonusLevel * lootMultiplier);
    }

    public String getRequiredPermission() {
        return requiredPermission;
    }

    public List<String> getCommands(Player player, int amountOfCommands) {
        List<String> commands = new ArrayList<>();

        this.commands.forEach(command -> {
            for (int i = 0; i < amountOfCommands; i++) {
                int randomNumber = min < 0 || max < 0 ? 0 : Random.nextInt(max - min + 1) + min;
                commands.add(command.replace("{player-name}", player.getName()).replace("{number}", String.valueOf(randomNumber)));
            }
        });

        return commands;
    }

}
