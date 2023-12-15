package com.bgsoftware.wildstacker.api.loot;

import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public interface LootTable {

    /**
     * Get the vanilla drops of a stacked entity using a fortune level and a stack size.
     *
     * @param stackedEntity  the stacked entity
     * @param lootBonusLevel the fortune level
     * @param stackAmount    the stack size
     * @return The drops of the entity
     */
    List<ItemStack> getDrops(StackedEntity stackedEntity, int lootBonusLevel, int stackAmount);

    /**
     * Get the vanilla drops for the given {@link LootEntityAttributes}.
     *
     * @param lootEntityAttributes The loot entity data
     * @param lootBonusLevel the fortune level
     * @param stackAmount    the stack size
     * @return The drops for the context.
     */
    List<ItemStack> getDrops(LootEntityAttributes lootEntityAttributes, int lootBonusLevel, int stackAmount);

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

    /**
     * Get the vanilla exp of the given {@link LootEntityAttributes}.
     * In 1.19 or later, this method must be called synchronized if there's no custom exp set
     * in the entity's loot table. Therefore, in case of async call, -1 will be returned.
     *
     * @param lootEntityAttributes The loot entity data
     * @param stackAmount    the stack size
     * @return The exp for the context.
     */
    int getExp(LootEntityAttributes lootEntityAttributes, int stackAmount);

}
