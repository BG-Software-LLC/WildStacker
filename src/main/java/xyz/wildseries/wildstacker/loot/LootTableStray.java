package xyz.wildseries.wildstacker.loot;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;
import xyz.wildseries.wildstacker.utils.legacy.Materials;

import java.util.List;

public class LootTableStray extends LootTable {

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
        return new ItemStack(Material.ARROW);
    }

    @Override
    public List<ItemStack> getDeathLoot(int lootBonusLevel) {
        List<ItemStack> deathLoot = super.getDeathLoot(lootBonusLevel);

        int boneAmount = 0, tippedArrow = 0;

        for (int i = 0; i < getStackAmount(); i++) {
            int _boneAmount = this.random.nextInt(3);
            int _tippedArrow = isKilledByPlayer() ? this.random.nextInt(2) : 0;

            if (lootBonusLevel > 0) {
                _boneAmount += this.random.nextInt(lootBonusLevel + 1);
                _tippedArrow += isKilledByPlayer() ? this.random.nextInt(lootBonusLevel + 1) : 0;
            }

            if(_boneAmount > 0)
                boneAmount += _boneAmount;
            if(_tippedArrow > 0)
                tippedArrow += _tippedArrow;
        }

        if(boneAmount > 0){
            ItemStack bone = new ItemStack(Material.BONE);
            bone.setAmount(boneAmount);
            deathLoot.add(bone);
        }

        if(tippedArrow > 0){
            ItemStack arrow = Materials.TIPPED_ARROW.toBukkitItem();
            PotionMeta potionMeta = (PotionMeta) arrow.getItemMeta();
            try {
                //noinspection JavaReflectionMemberAccess
                PotionMeta.class.getMethod("setBasePotionData", PotionData.class).invoke(potionMeta, new PotionData(PotionType.SLOWNESS));
            }catch(Exception ignored){}
            arrow.setItemMeta(potionMeta);
            arrow.setAmount(tippedArrow);
            deathLoot.add(arrow);
        }

        return deathLoot;
    }
}
