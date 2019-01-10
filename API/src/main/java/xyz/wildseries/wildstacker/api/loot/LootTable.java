package xyz.wildseries.wildstacker.api.loot;

import org.bukkit.inventory.ItemStack;

import java.util.List;

public interface LootTable {

    List<ItemStack> getDeathLoot(int lootBonusLevel);

}
