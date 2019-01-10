package xyz.wildseries.wildstacker.loot;

import org.bukkit.Material;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.ChestedHorse;
import org.bukkit.entity.Horse;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class LootTableHorse extends LootTable {

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
        return new ItemStack(Material.LEATHER);
    }

    @Override
    public List<ItemStack> getDeathLoot(int lootBonusLevel) {
        List<ItemStack> deathLoot = super.getDeathLoot(lootBonusLevel);

        if(livingEntity instanceof Horse){
            ItemStack armor = ((Horse) livingEntity).getInventory().getArmor();
            if(armor != null)
                deathLoot.add(armor);
        }
        else if(livingEntity instanceof ChestedHorse){
            if(((ChestedHorse) livingEntity).isCarryingChest()) {
                ((ChestedHorse) livingEntity).getInventory().forEach(itemStack -> {
                    if(itemStack != null)
                        deathLoot.add(itemStack);
                });
            }
        }else if(livingEntity instanceof AbstractHorse){
            ItemStack saddle = ((AbstractHorse) livingEntity).getInventory().getSaddle();
            if(saddle != null)
                deathLoot.add(saddle);
        }

        return deathLoot;
    }
}
