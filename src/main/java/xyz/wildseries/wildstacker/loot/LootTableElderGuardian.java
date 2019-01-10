package xyz.wildseries.wildstacker.loot;

import org.bukkit.inventory.ItemStack;
import xyz.wildseries.wildstacker.utils.legacy.Materials;

import java.util.List;

public class LootTableElderGuardian extends LootTableGuardian {

    @Override
    public List<ItemStack> getDeathLoot(int lootBonusLevel) {
        List<ItemStack> deathLoot = super.getDeathLoot(lootBonusLevel);

        if(isKilledByPlayer()){
            deathLoot.add(Materials.WET_SPONGE.toBukkitItem(getStackAmount()));
        }

        return deathLoot;
    }
}
