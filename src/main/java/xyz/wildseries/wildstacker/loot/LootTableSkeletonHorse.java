package xyz.wildseries.wildstacker.loot;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class LootTableSkeletonHorse extends LootTable {

    @Override
    protected int getMaximumAmount() {
        return 2;
    }

    @Override
    protected int getMinimumAmount() {
        return 0;
    }

    @Override
    protected ItemStack getLoot() {
        return new ItemStack(Material.BONE);
    }
}
