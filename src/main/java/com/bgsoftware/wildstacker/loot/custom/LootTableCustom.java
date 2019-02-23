package com.bgsoftware.wildstacker.loot.custom;

import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.loot.LootTable;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public abstract class LootTableCustom {

    public abstract List<ItemStack> getDrops(LootTable lootTable, StackedEntity stackedEntity, int lootBonusLevel, int stackAmount);

}
