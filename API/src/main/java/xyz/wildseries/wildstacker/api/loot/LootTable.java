package xyz.wildseries.wildstacker.api.loot;

import org.bukkit.inventory.ItemStack;
import xyz.wildseries.wildstacker.api.objects.StackedEntity;

import java.util.List;

public interface LootTable {

    List<ItemStack> getDrops(StackedEntity stackedEntity, int lootBonusLevel, int stackAmount);

}
