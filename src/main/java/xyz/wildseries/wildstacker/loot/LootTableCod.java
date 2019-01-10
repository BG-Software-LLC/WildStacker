package xyz.wildseries.wildstacker.loot;

import org.bukkit.inventory.ItemStack;
import xyz.wildseries.wildstacker.utils.legacy.Materials;

import java.util.List;

public class LootTableCod extends LootTable {

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
        return isBurning() ? Materials.COOKED_COD.toBukkitItem() : Materials.COD.toBukkitItem();
    }

    @Override
    public List<ItemStack> getDeathLoot(int lootBonusLevel) {
        List<ItemStack> deathLoot = super.getDeathLoot(lootBonusLevel);

        ItemStack loot = Materials.BONE_MEAL.toBukkitItem();
        int amount = 0;

        for (int i = 0; i < getStackAmount(); i++) {
            if(this.random.nextInt(100) + 1 <= 5){
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
