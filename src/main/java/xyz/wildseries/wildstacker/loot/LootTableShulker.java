package xyz.wildseries.wildstacker.loot;

import org.bukkit.inventory.ItemStack;
import xyz.wildseries.wildstacker.utils.legacy.Materials;

import java.util.ArrayList;
import java.util.List;

public class LootTableShulker extends LootTable {

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
        return null;
    }

    @Override
    public List<ItemStack> getDeathLoot(int lootBonusLevel) {
        List<ItemStack> deathLoot = new ArrayList<>();

        if(this.random.nextDouble(2) <= (0.5 + (0.0625 * lootBonusLevel))){
            deathLoot.add(Materials.SHULKER_SHELL.toBukkitItem());
        }

        return deathLoot;
    }
}
