package xyz.wildseries.wildstacker.loot;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import xyz.wildseries.wildstacker.utils.legacy.Materials;

import java.util.ArrayList;
import java.util.List;

public class LootTableWitch extends LootTable {

    @Override
    protected int getMaximumAmount() {
        return 0;
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

        int amountOfItems = this.random.nextInt(3) + 1;
        int bottleAmount = 0, glowstoneAmount = 0, gunpowderAmount = 0, redstoneAmount = 0, spiderAmount = 0,
                sugarAmount = 0, stickAmount = 0;

        for(int i = 0; i < getStackAmount(); i++) {
            for (int j = 0; j < amountOfItems; j++) {
                int lootAmount = this.random.nextInt(3);

                if(lootBonusLevel > 0)
                    lootAmount += this.random.nextInt(lootBonusLevel + 1);

                double chance = this.random.nextDouble(100);
                if(chance < 12.5){
                    glowstoneAmount += lootAmount;
                }else if(chance < 25){
                    sugarAmount += lootAmount;
                }else if(chance < 37.5){
                    redstoneAmount += lootAmount;
                }else if(chance < 50){
                    spiderAmount += lootAmount;
                }else if(chance < 62.5){
                    bottleAmount += lootAmount;
                }else if(chance < 75){
                    gunpowderAmount += lootAmount;
                }else{
                    stickAmount += lootAmount;
                }
            }
        }

        if(glowstoneAmount > 0){
            ItemStack glowstone = new ItemStack(Material.GLOWSTONE_DUST);
            glowstone.setAmount(glowstoneAmount);
            deathLoot.add(glowstone);
        }

        if(sugarAmount > 0){
            ItemStack sugar = new ItemStack(Material.SUGAR);
            sugar.setAmount(sugarAmount);
            deathLoot.add(sugar);
        }

        if(redstoneAmount > 0){
            ItemStack redstone = new ItemStack(Material.REDSTONE);
            redstone.setAmount(redstoneAmount);
            deathLoot.add(redstone);
        }

        if(spiderAmount > 0){
            ItemStack spider = new ItemStack(Material.SPIDER_EYE);
            spider.setAmount(spiderAmount);
            deathLoot.add(spider);
        }

        if(bottleAmount > 0){
            ItemStack bottle = new ItemStack(Material.GLASS_BOTTLE);
            bottle.setAmount(bottleAmount);
            deathLoot.add(bottle);
        }

        if(gunpowderAmount > 0){
            ItemStack gunpowder = Materials.GUNPOWDER.toBukkitItem();
            gunpowder.setAmount(gunpowderAmount);
            deathLoot.add(gunpowder);
        }

        if(stickAmount > 0){
            ItemStack stick = new ItemStack(Material.STICK);
            stick.setAmount(stickAmount);
            deathLoot.add(stick);
        }

        return deathLoot;
    }
}
