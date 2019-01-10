package xyz.wildseries.wildstacker.loot;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import xyz.wildseries.wildstacker.utils.legacy.Materials;

import java.util.List;

public class LootTableGuardian extends LootTable {

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
        return new ItemStack(Material.PRISMARINE_SHARD);
    }

    @Override
    public List<ItemStack> getDeathLoot(int lootBonusLevel) {
        List<ItemStack> deathLoot = super.getDeathLoot(lootBonusLevel);

        //50% - cod
        //33% - prismarine crystal
        //17% - nothing

        //60% raw fish
        //25% raw salmon
        //13% pufferfish
        //2% tropical fish

        int codAmount = 0, prismarineAmount = 0, tropicalAmount = 0, pufferAmount = 0, salmonAmount = 0;

        for (int i = 0; i < getStackAmount(); i++) {
            int chance = this.random.nextInt(100);

            //Cod
            if(chance < 50){
                int _codAmount = 1;

                if (lootBonusLevel > 0) {
                    _codAmount += this.random.nextInt(lootBonusLevel + 1);
                }

                codAmount += _codAmount;
            }
            //Prismarine Crystals
            else if(chance < 83){
                int _prismarineAmount = 1;

                if (lootBonusLevel > 0) {
                    _prismarineAmount += this.random.nextInt(lootBonusLevel + 1);
                }

                prismarineAmount += _prismarineAmount;
            }

            if(this.random.nextDouble(2) <= (0.025 + (0.01 * lootBonusLevel))){
                chance = this.random.nextInt(100);

                if(chance < 2){
                    tropicalAmount++;
                }
                else if(chance < 15){
                    pufferAmount++;
                }
                else if(chance < 40){
                    salmonAmount++;
                }
                else{
                    codAmount++;
                }
            }
        }

        if(codAmount > 0){
            ItemStack cod = isBurning() ? Materials.COOKED_COD.toBukkitItem() : Materials.COD.toBukkitItem();
            cod.setAmount(codAmount);
            deathLoot.add(cod);
        }

        if(prismarineAmount > 0){
            ItemStack prismarineCrystals = new ItemStack(Material.PRISMARINE_CRYSTALS);
            prismarineCrystals.setAmount(prismarineAmount);
            deathLoot.add(prismarineCrystals);
        }

        if(tropicalAmount > 0){
            ItemStack tropical = Materials.TROPICAL_FISH.toBukkitItem();
            tropical.setAmount(codAmount);
            deathLoot.add(tropical);
        }

        if(pufferAmount > 0){
            ItemStack puffer = Materials.PUFFERFISH.toBukkitItem();
            puffer.setAmount(codAmount);
            deathLoot.add(puffer);
        }

        if(salmonAmount > 0){
            ItemStack salmon = Materials.SALMON.toBukkitItem();
            salmon.setAmount(codAmount);
            deathLoot.add(salmon);
        }

        return deathLoot;
    }

}
