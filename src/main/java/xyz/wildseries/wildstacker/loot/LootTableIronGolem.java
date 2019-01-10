package xyz.wildseries.wildstacker.loot;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import xyz.wildseries.wildstacker.utils.legacy.Materials;

import java.util.List;

public class LootTableIronGolem extends LootTable {

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
        return Materials.POPPY.toBukkitItem();
    }

    @Override
    public List<ItemStack> getDeathLoot(int lootBonusLevel) {
        List<ItemStack> deathLoot = super.getDeathLoot(0);

        ItemStack loot = new ItemStack(Material.IRON_INGOT);
        int amount = 0;

        for (int i = 0; i < getStackAmount(); i++) {
            amount += this.random.nextInt(3) + 3;
        }

        if(amount > 0){
            loot.setAmount(amount);
            deathLoot.add(loot);
        }

        return deathLoot;
    }
}
