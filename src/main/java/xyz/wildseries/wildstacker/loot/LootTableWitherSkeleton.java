package xyz.wildseries.wildstacker.loot;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import xyz.wildseries.wildstacker.utils.legacy.Materials;

import java.util.List;

public class LootTableWitherSkeleton extends LootTable {

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
        return new ItemStack(Material.BONE);
    }

    @Override
    public List<ItemStack> getDeathLoot(int lootBonusLevel) {
        List<ItemStack> deathLoot = super.getDeathLoot(lootBonusLevel);

        int coalAmount = 0, skullAmount = 0;

        for(int i = 0; i < getStackAmount(); i++){
            int _skullAmount = 0, _coalAmount = this.random.nextInt(3) - 1;

            if(isKilledByPlayer() && this.random.nextDouble(2) <= (0.025 + (0.01 * lootBonusLevel)))
                _skullAmount = 1;

            if(lootBonusLevel > 0){
                _coalAmount += this.random.nextInt(lootBonusLevel + 1);
            }

            if(_coalAmount > 0)
                coalAmount += _coalAmount;
            if(_skullAmount > 0)
                skullAmount += _skullAmount;
        }

        if(coalAmount > 0){
            ItemStack coal = new ItemStack(Material.COAL);
            coal.setAmount(coalAmount);
            deathLoot.add(coal);
        }

        if(skullAmount > 0){
            ItemStack skull = Materials.WITHER_SKELETON_SKULL.toBukkitItem();
            skull.setAmount(skullAmount);
            deathLoot.add(skull);
        }

        return deathLoot;
    }

    @Override
    protected ItemStack getEquipment(int lootBonusLevel) {
        ItemStack goldenSword = new ItemStack(Material.STONE_SWORD);
        boolean dropSword = this.random.nextFloat() < 0.085;

        if(isKilledByPlayer() && dropSword && this.random.nextFloat() - (float) lootBonusLevel * 0.01F < 0.085F){
            int maxDurability = Math.max(goldenSword.getType().getMaxDurability() - 25, 1);
            int durability = goldenSword.getType().getMaxDurability() - this.random.nextInt(this.random.nextInt(maxDurability) + 1);

            if(durability > maxDurability)
                durability = maxDurability;

            if(durability < 1)
                durability = 1;

            goldenSword.setDurability((short) durability);

            return goldenSword;
        }

        return null;
    }
}
