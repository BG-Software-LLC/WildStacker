package xyz.wildseries.wildstacker.loot;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class LootTableMagmaCube extends LootTable {

    @Override
    protected int getMaximumAmount() {
        return 1;
    }

    @Override
    protected int getMinimumAmount() {
        return -2;
    }

    @Override
    protected ItemStack getLoot() {
        return new ItemStack(Material.MAGMA_CREAM);
    }
}
