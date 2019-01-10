package xyz.wildseries.wildstacker.loot;

import org.bukkit.inventory.ItemStack;
import xyz.wildseries.wildstacker.utils.legacy.Materials;

public class LootTablePig extends LootTable {

    @Override
    public int getMaximumAmount() {
        return 3;
    }

    @Override
    public int getMinimumAmount() {
        return 1;
    }

    @Override
    public ItemStack getLoot() {
        return isBurning() ? Materials.COOKED_PORKCHOP.toBukkitItem() : Materials.PORKCHOP.toBukkitItem();
    }
}
