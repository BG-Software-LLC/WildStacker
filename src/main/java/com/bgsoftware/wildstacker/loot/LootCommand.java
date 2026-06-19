package com.bgsoftware.wildstacker.loot;

import com.bgsoftware.wildstacker.utils.Random;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class LootCommand {

    private final List<String> commands = new LinkedList<>();
    private final double chance;
    private final int min, max;
    private final String requiredPermission;

    public LootCommand(List<String> commands, double chance, int min, int max, String requiredPermission) {
        this.commands.addAll(commands);
        this.chance = chance;
        this.min = min;
        this.max = max;
        this.requiredPermission = requiredPermission;
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
