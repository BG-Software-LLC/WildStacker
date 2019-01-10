package xyz.wildseries.wildstacker.loot;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class LootTableZombie extends LootTableZombieHorse {

    @Override
    public List<ItemStack> getDeathLoot(int lootBonusLevel) {
        List<ItemStack> deathLoot = super.getDeathLoot(lootBonusLevel);

        if(isKilledByPlayer()){
            int ironAmount = 0, carrotAmount = 0, potatoAmount = 0;

            for(int i = 0; i < getStackAmount(); i++){
                if(this.random.nextDouble(2) <= (0.025 + (0.01 * lootBonusLevel))){
                    double chance = this.random.nextDouble(100);
                    if(chance < 33.334){
                        ironAmount++;
                    }else if(chance < 66.667){
                        carrotAmount++;
                    }else{
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

        }

        return deathLoot;
    }
}
