package com.bgsoftware.wildstacker.loot;

import com.bgsoftware.wildstacker.api.loot.LootEntityAttributes;
import com.bgsoftware.wildstacker.loot.entity.LivingLootEntityAttributes;
import com.bgsoftware.wildstacker.utils.Random;
import com.bgsoftware.wildstacker.utils.threads.Executor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

public class LootPair extends FilteredLoot {

    private final List<LootItem> lootItems = new LinkedList<>();
    private final List<LootCommand> lootCommands = new LinkedList<>();
    private final double chance, lootingChance;

    public LootPair(List<LootItem> lootItems, List<LootCommand> lootCommands, double chance,
                    double lootingChance, List<Predicate<LootEntityAttributes>> entityFilters,
                    List<Predicate<LootEntityAttributes>> killerFilters,
                    List<Predicate<LootEntityAttributes>> vehicleFilters) {
        super(entityFilters, killerFilters, vehicleFilters);
        this.lootItems.addAll(lootItems);
        this.lootCommands.addAll(lootCommands);
        this.chance = chance;
        this.lootingChance = lootingChance;
    }

    public List<ItemStack> getItems(LootEntityAttributes lootEntityAttributes, int amountOfPairs, int lootBonusLevel) {
        List<ItemStack> items = new LinkedList<>();

        LootEntityAttributes directKillerEntityData = lootEntityAttributes.getKiller();
        LootEntityAttributes sourceKillerEntityData = LivingLootEntityAttributes.getSourceKiller(lootEntityAttributes);

        for (LootItem lootItem : lootItems) {
            if ((!lootItem.checkKiller(directKillerEntityData) && !lootItem.checkKiller(sourceKillerEntityData)) ||
                    !lootItem.checkEntity(lootEntityAttributes))
                continue;

            int amountOfItems = (int) (lootItem.getChance(lootBonusLevel, lootingChance) * amountOfPairs / 100);

            if (amountOfItems == 0) {
                amountOfItems = Random.nextChance(lootItem.getChance(lootBonusLevel, lootingChance), amountOfPairs);
            }

            ItemStack itemStack = lootItem.getItemStack(lootEntityAttributes, amountOfItems, lootBonusLevel);

            if (itemStack != null)
                items.add(itemStack);
        }

        return items;
    }

    public void executeCommands(Player player, int amountOfPairs, int lootBonusLevel) {
        List<String> commands = new ArrayList<>();

        for (LootCommand lootCommand : lootCommands) {
            if (!lootCommand.getRequiredPermission().isEmpty() && !player.hasPermission(lootCommand.getRequiredPermission()))
                continue;

            int amountOfCommands = (int) (lootCommand.getChance(lootBonusLevel, lootingChance) * amountOfPairs / 100);

            if (amountOfCommands == 0) {
                amountOfCommands = Random.nextChance(lootCommand.getChance(lootBonusLevel, lootingChance), amountOfPairs);
            }

            commands.addAll(lootCommand.getCommands(player, amountOfCommands));
        }

        Executor.sync(() -> commands.forEach(command -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command)));
    }

    public double getChance() {
        return chance;
    }

    @Override
    public String toString() {
        return "LootPair{items=" + lootItems + "}";
    }

}
