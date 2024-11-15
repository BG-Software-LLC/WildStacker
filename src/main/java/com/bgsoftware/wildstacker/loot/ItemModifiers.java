package com.bgsoftware.wildstacker.loot;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.utils.Random;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

public class ItemModifiers {

    private static final WildStackerPlugin plugin = WildStackerPlugin.getPlugin();

    public static ItemModifierFunction countModifier(int min, int max) {
        if (min >= max) {
            return (lootItem, itemStack, itemMeta, amountOfItems, lootBonusLevel) -> {
                itemStack.setAmount(min);
                return true;
            };
        }

        return (lootItem, itemStack, itemMeta, amountOfItems, lootBonusLevel) -> {
            int lootingBonus = 0;

            if (lootItem.isLooting() && lootBonusLevel > 0) {
                lootingBonus = Random.nextInt(lootBonusLevel + 1);
            }

            int itemAmount = Random.nextInt(min + lootingBonus, max + lootingBonus, amountOfItems);

            if (itemAmount <= 0)
                return false;

            itemStack.setAmount(itemAmount);

            return true;
        };
    }

    public static ItemModifierFunction ominousBottleModifier(int min, int max) {
        int minBound = Math.max(min, 0);
        int maxBound = Math.min(max, 4);

        if (minBound >= maxBound) {
            return (lootItem, itemStack, itemMeta, amountOfItems, lootBonusLevel) -> {
                if (itemMeta != null)
                    plugin.getNMSAdapter().setOminousBottleAmplifier(itemMeta, minBound);

                return true;
            };
        }

        return (lootItem, itemStack, itemMeta, amountOfItems, lootBonusLevel) -> {
            if (itemMeta != null) {
                int amplifier = Random.nextInt(minBound, maxBound, 1);
                plugin.getNMSAdapter().setOminousBottleAmplifier(itemMeta, amplifier);
            }

            return true;
        };
    }

    private ItemModifiers() {

    }

    public interface ItemModifierFunction {

        boolean apply(LootItem lootItem, ItemStack itemStack, @Nullable ItemMeta itemMeta,
                      int amountOfItems, int lootBonusLevel);

    }

}
