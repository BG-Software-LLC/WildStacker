package com.bgsoftware.wildstacker.api.loot;

import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public interface LootTable {

    /**
     * Get the vanilla drops of an stacked entity using a fortune level and a stack size.
     * @param stackedEntity the stacked entity
     * @param lootBonusLevel the fortune level
     * @param stackAmount the stack size
     * @return The drops of the entity
     */
    List<ItemStack> getDrops(StackedEntity stackedEntity, int lootBonusLevel, int stackAmount);

    /**
     * Get the vanilla exp of a the entity using a stack size.
     * @param stackAmount the stack size
     * @param defaultExp the default exp to return if no exp was found
     * @return The exp of the entity
     */
    int getExp(int stackAmount, int defaultExp);

}
