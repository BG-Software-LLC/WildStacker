package xyz.wildseries.wildstacker.loot;

import org.bukkit.inventory.ItemStack;

public class LootTableEmpty extends LootTable {

    @Override
    public int getMaximumAmount() {
        return 0;
    }

    @Override
    public int getMinimumAmount() {
        return 0;
    }

    @Override
    public ItemStack getLoot() {
        return null;
    }
}
