package xyz.wildseries.wildstacker.loot;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import xyz.wildseries.wildstacker.utils.legacy.Materials;

import java.util.List;

public class LootTableGhast extends LootTable {

    @Override
    protected int getMaximumAmount() {
        return 1;
    }

    @Override
    protected int getMinimumAmount() {
        return 0;
    }

    @Override
    protected ItemStack getLoot() {
        return new ItemStack(Material.GHAST_TEAR);
    }

    @Override
    public List<ItemStack> getDeathLoot(int lootBonusLevel) {
        List<ItemStack> deathLoot = super.getDeathLoot(lootBonusLevel);

        ItemStack loot = Materials.GUNPOWDER.toBukkitItem();
        int amount = 0;

        for (int i = 0; i < getStackAmount(); i++) {
            int lootAmount = this.random.nextInt(3);

            if (lootBonusLevel > 0) {
                lootAmount += this.random.nextInt(lootBonusLevel + 1);
            }

            amount += lootAmount;
        }

        if(amount > 0){
            loot.setAmount(amount);
            deathLoot.add(loot);
        }

        return deathLoot;
    }
}
