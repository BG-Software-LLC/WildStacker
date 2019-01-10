package xyz.wildseries.wildstacker.loot;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class LootTableHusk extends LootTable {

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

        int ironAmount = 0, carrotAmount = 0, potatoAmount = 0;

        if(isKilledByPlayer() && this.random.nextDouble(2) <= (0.025 + (0.01 * lootBonusLevel))){
            for(int i = 0; i < getStackAmount(); i++){
                int chance = this.random.nextInt(100);

                if(chance < 33){
                    ironAmount++;
                }
                else if(chance < 66){
                    carrotAmount++;
                }
                else{
                    potatoAmount++;
                }
            }
        }

        if(ironAmount > 0){
            ItemStack iron = new ItemStack(Material.IRON_INGOT);
            iron.setAmount(ironAmount);
            deathLoot.add(iron);
        }

        if(carrotAmount > 0){
            ItemStack carrot = new ItemStack(Material.CARROT);
            carrot.setAmount(carrotAmount);
            deathLoot.add(carrot);
        }

        if(potatoAmount > 0){
            ItemStack potato = new ItemStack(Material.POTATO);
            potato.setAmount(potatoAmount);
            deathLoot.add(potato);
        }

        return deathLoot;
    }
}
