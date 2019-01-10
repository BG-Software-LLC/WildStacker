package xyz.wildseries.wildstacker.loot;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import xyz.wildseries.wildstacker.utils.legacy.Materials;

import java.util.List;

public class LootTableZombiePigman extends LootTableZombieHorse {

    @Override
    protected int getMaximumAmount() {
        return 1;
    }

    @Override
    public List<ItemStack> getDeathLoot(int lootBonusLevel) {
        List<ItemStack> deathLoot = super.getDeathLoot(lootBonusLevel);

        if(!isBaby()) {
            int nuggetAmount = 0, goldAmount = 0;

            for (int i = 0; i < getStackAmount(); i++) {
                int _nuggetAmount = this.random.nextInt(2);

                if (lootBonusLevel > 0) {
                    _nuggetAmount = this.random.nextInt(lootBonusLevel + 1);
                }

                if (isKilledByPlayer()) {
                    if (this.random.nextDouble(2) <= (0.025 + (0.01 * lootBonusLevel))) {
                        goldAmount++;
                    }
                }

                if (_nuggetAmount > 0)
                    nuggetAmount += _nuggetAmount;
            }

            if (nuggetAmount > 0) {
                ItemStack nugget = new ItemStack(Material.GOLD_NUGGET);
                nugget.setAmount(nuggetAmount);
                deathLoot.add(nugget);
            }

            if (goldAmount > 0) {
                ItemStack gold = new ItemStack(Material.GOLD_INGOT);
                gold.setAmount(goldAmount);
                deathLoot.add(gold);
            }
        }

        return deathLoot;
    }

    @Override
    protected ItemStack getEquipment(int lootBonusLevel){
        ItemStack goldenSword = Materials.GOLDEN_SWORD.toBukkitItem();
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
