package com.bgsoftware.wildstacker.api.loot;

import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public interface LootTable {

    List<ItemStack> getDrops(StackedEntity stackedEntity, int lootBonusLevel, int stackAmount);

}
