package xyz.wildseries.wildstacker.loot;

import org.bukkit.inventory.ItemStack;
import xyz.wildseries.wildstacker.utils.legacy.Materials;

public class LootTableDolphin extends LootTable {

    @Override
    public int getMaximumAmount() {
        return 1;
    }

    @Override
    public int getMinimumAmount() {
        return 0;
    }

    @Override
    public ItemStack getLoot() {
        return isBurning() ? Materials.COD.toBukkitItem() : Materials.COOKED_COD.toBukkitItem();
    }
}
