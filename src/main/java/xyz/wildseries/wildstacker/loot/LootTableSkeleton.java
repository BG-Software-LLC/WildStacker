package xyz.wildseries.wildstacker.loot;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class LootTableSkeleton extends LootTable {

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

    @Override
    public List<ItemStack> getDeathLoot(int lootBonusLevel) {
        List<ItemStack> deathLoot = super.getDeathLoot(lootBonusLevel);

        int amount = 0;

        for (int i = 0; i < getStackAmount(); i++) {
            int lootAmount = this.random.nextInt(3);

            if (lootBonusLevel > 0) {
                lootAmount += this.random.nextInt(lootBonusLevel + 1);
            }

            if(lootAmount > 0)
                amount += lootAmount;
        }

        if(amount > 0){
            ItemStack arrow = new ItemStack(Material.ARROW);
            arrow.setAmount(amount);
            deathLoot.add(arrow);
        }

        return deathLoot;
    }



}
