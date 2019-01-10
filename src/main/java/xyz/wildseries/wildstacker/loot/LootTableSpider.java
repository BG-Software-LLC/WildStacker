package xyz.wildseries.wildstacker.loot;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class LootTableSpider extends LootTable {

    @Override
    public int getMaximumAmount() {
        return 2;
    }

    @Override
    public int getMinimumAmount() {
        return 0;
    }

    @Override
    public ItemStack getLoot() {
        return new ItemStack(Material.STRING);
    }

    @Override
    public List<ItemStack> getDeathLoot(int lootBonusLevel) {
        List<ItemStack> deathLoot = super.getDeathLoot(lootBonusLevel);

        if(isKilledByPlayer()){
            int amount = 0;

            for (int i = 0; i < getStackAmount(); i++) {
                int lootAmount = this.random.nextInt(3) - 1;

                if (lootBonusLevel > 0) {
                    lootAmount += this.random.nextInt(lootBonusLevel + 1);
                }

                if(lootAmount > 0)
                    amount += lootAmount;
            }

            if(amount > 0){
                ItemStack spiderEye = new ItemStack(Material.SPIDER_EYE);
                spiderEye.setAmount(amount);
                deathLoot.add(spiderEye);
            }
        }

        return deathLoot;
    }
}
