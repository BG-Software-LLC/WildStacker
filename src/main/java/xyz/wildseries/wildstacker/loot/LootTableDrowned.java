package xyz.wildseries.wildstacker.loot;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class LootTableDrowned extends LootTable {

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
        return new ItemStack(Material.ROTTEN_FLESH);
    }

    @Override
    public List<ItemStack> getDeathLoot(int lootBonusLevel) {
        List<ItemStack> deathLoot = super.getDeathLoot(lootBonusLevel);

        ItemStack loot = new ItemStack(Material.GOLD_INGOT);
        int amount = 0;

        for (int i = 0; i < getStackAmount(); i++) {
            if(this.random.nextDouble(2) <= (0.05 + (0.01 * lootBonusLevel))){
                amount += 1;
            }
        }

        if(amount > 0){
            loot.setAmount(amount);
            deathLoot.add(loot);
        }
        return deathLoot;
    }
}
