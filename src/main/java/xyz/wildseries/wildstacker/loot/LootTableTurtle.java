package xyz.wildseries.wildstacker.loot;

import org.bukkit.inventory.ItemStack;
import xyz.wildseries.wildstacker.utils.legacy.Materials;

public class LootTableTurtle extends LootTable {

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
        return Materials.SEAGRASS.toBukkitItem();
    }
}
