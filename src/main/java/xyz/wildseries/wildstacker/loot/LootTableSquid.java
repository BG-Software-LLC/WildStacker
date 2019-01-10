package xyz.wildseries.wildstacker.loot;

import org.bukkit.inventory.ItemStack;
import xyz.wildseries.wildstacker.utils.legacy.Materials;

public class LootTableSquid extends LootTable {

    @Override
    protected int getMaximumAmount() {
        return 3;
    }

    @Override
    protected int getMinimumAmount() {
        return 1;
    }

    @Override
    protected ItemStack getLoot() {
        return Materials.INK_SAC.toBukkitItem();
    }
}
