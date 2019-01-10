package xyz.wildseries.wildstacker.loot;

import org.bukkit.Material;
import org.bukkit.entity.Sheep;
import org.bukkit.inventory.ItemStack;
import xyz.wildseries.wildstacker.utils.legacy.Materials;

import java.util.List;

public class LootTableSheep extends LootTable {

    @Override
    protected int getMaximumAmount() {
        return 2;
    }

    @Override
    protected int getMinimumAmount() {
        return 1;
    }

    @Override
    protected ItemStack getLoot() {
        return isBurning() ? new ItemStack(Material.COOKED_MUTTON) : new ItemStack(Material.MUTTON);
    }

    @Override
    public List<ItemStack> getDeathLoot(int lootBonusLevel) {
        List<ItemStack> deathLoot = super.getDeathLoot(lootBonusLevel);

        deathLoot.add(Materials.getWool(((Sheep) livingEntity).getColor()));

        return deathLoot;
    }
}
