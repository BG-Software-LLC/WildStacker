package xyz.wildseries.wildstacker.loot;

import org.bukkit.inventory.ItemStack;
import xyz.wildseries.wildstacker.utils.legacy.Materials;

import java.util.ArrayList;
import java.util.List;

public class LootTablePolarBear extends LootTable {

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
        return null;
    }

    @Override
    public List<ItemStack> getDeathLoot(int lootBonusLevel) {
        List<ItemStack> deathLoot = new ArrayList<>();

        int codAmount = 0, salmonAmount = 0;

        //25% salmon
        //75% cod

        for (int i = 0; i < getStackAmount(); i++) {
            int lootAmount = this.random.nextInt(getMaximumAmount() - getMinimumAmount() + 1) + getMinimumAmount();

            if (lootBonusLevel > 0) {
                lootAmount += this.random.nextInt(lootBonusLevel + 1);
            }

            if(lootAmount > 0) {
                if (this.random.nextInt(100) < 25) {
                    salmonAmount += lootAmount;
                } else {
                    codAmount += lootAmount;
                }
            }
        }

        if(codAmount > 0){
            ItemStack cod = Materials.COD.toBukkitItem();
            cod.setAmount(codAmount);
            deathLoot.add(cod);
        }

        if(salmonAmount > 0){
            ItemStack salmon = Materials.SALMON.toBukkitItem();
            salmon.setAmount(salmonAmount);
            deathLoot.add(salmon);
        }

        return deathLoot;
    }
}
