package com.bgsoftware.wildstacker.api.loot;

import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public interface LootTable {

    /**
     * Get the vanilla drops of an stacked entity using a fortune level and a stack size.
     *
     * @param stackedEntity  the stacked entity
     * @param lootBonusLevel the fortune level
     * @param stackAmount    the stack size
     * @return The drops of the entity
     */
    List<ItemStack> getDrops(StackedEntity stackedEntity, int lootBonusLevel, int stackAmount);

    /**
     * Get the drops of a loot table using a fortune level and a stack size.
     *
     * @param lootBonusLevel the fortune level
     * @param stackAmount    the stack size
     * @return The drops of the loot table
     */
    List<ItemStack> getDrops(int lootBonusLevel, int stackAmount);

    /**
     * Get the vanilla exp of the entity using a stack size.
     * In 1.19 or later, this method must be called synchronized if there's no custom exp set
     * in the entity's loot table. Therefore, in case of async call, -1 will be returned.
     *
     * @param stackedEntity The stacked entity object
     * @param stackAmount   the stack size
     * @return The exp of the entity
     */
    int getExp(StackedEntity stackedEntity, int stackAmount);

}
