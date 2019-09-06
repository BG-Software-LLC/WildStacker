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
     * @param stackedEntity The stacked entity object
     * @param stackAmount the stack size
     * @return The exp of the entity
     */
    int getExp(StackedEntity stackedEntity, int stackAmount);

}
