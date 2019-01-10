package xyz.wildseries.wildstacker.loot;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import xyz.wildseries.wildstacker.utils.legacy.Materials;

import java.util.List;

public class LootTableCow extends LootTable {

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
        return new ItemStack(Material.LEATHER);
    }

    @Override
    public List<ItemStack> getDeathLoot(int lootBonusLevel) {
        List<ItemStack> deathLoot = super.getDeathLoot(lootBonusLevel);

        if(!isBaby()){
            ItemStack loot = isBurning() ? new ItemStack(Material.COOKED_BEEF) : Materials.BEEF.toBukkitItem();
            int amount = 0;

            for (int i = 0; i < getStackAmount(); i++) {
                int lootAmount = this.random.nextInt(3) + 1;

                if (lootBonusLevel > 0) {
                    lootAmount += this.random.nextInt(lootBonusLevel + 1);
                }

                amount += lootAmount;
            }

            loot.setAmount(amount);
            deathLoot.add(loot);
        }

        return deathLoot;
    }

}
