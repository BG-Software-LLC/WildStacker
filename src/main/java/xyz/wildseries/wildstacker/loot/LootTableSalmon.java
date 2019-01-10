package xyz.wildseries.wildstacker.loot;

import org.bukkit.inventory.ItemStack;
import xyz.wildseries.wildstacker.utils.legacy.Materials;

public class LootTableSalmon extends LootTableCod {

    @Override
    protected int getMaximumAmount() {
        return 1;
    }

    @Override
    protected int getMinimumAmount() {
        return 1;
    }

    @Override
    protected ItemStack getLoot() {
        return Materials.SALMON.toBukkitItem();
    }
}
