package xyz.wildseries.wildstacker.loot;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class LootTableRabbit extends LootTable {

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
        return new ItemStack(Material.RABBIT_HIDE);
    }

    @Override
    public List<ItemStack> getDeathLoot(int lootBonusLevel) {
        List<ItemStack> deathLoot = super.getDeathLoot(lootBonusLevel);

        int rabbitAmount = 0, footAmount = 0;

        for(int i = 0; i < getStackAmount(); i++){
            int _rabbitAmount = this.random.nextInt(2);

            if (lootBonusLevel > 0) {
                _rabbitAmount += this.random.nextInt(lootBonusLevel + 1);
            }

            rabbitAmount += _rabbitAmount;

            if(isKilledByPlayer() && this.random.nextDouble(2) <= (0.1 + (0.03 * lootBonusLevel))){
                footAmount++;
            }
        }

        if(rabbitAmount > 0){
            ItemStack rabbit = isBurning() ? new ItemStack(Material.COOKED_RABBIT) : new ItemStack(Material.RABBIT);
            rabbit.setAmount(rabbitAmount);
            deathLoot.add(rabbit);
        }

        if(footAmount > 0){
            ItemStack foot = new ItemStack(Material.RABBIT_FOOT);
            foot.setAmount(footAmount);
            deathLoot.add(foot);
        }

        return deathLoot;
    }
}
