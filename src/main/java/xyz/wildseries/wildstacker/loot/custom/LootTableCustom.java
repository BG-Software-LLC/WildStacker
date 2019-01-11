package xyz.wildseries.wildstacker.loot.custom;

import org.bukkit.inventory.ItemStack;
import xyz.wildseries.wildstacker.api.objects.StackedEntity;
import xyz.wildseries.wildstacker.loot.LootTable;

import java.util.List;

public abstract class LootTableCustom {

    public abstract List<ItemStack> getDrops(LootTable lootTable, StackedEntity stackedEntity, int lootBonusLevel);

}
