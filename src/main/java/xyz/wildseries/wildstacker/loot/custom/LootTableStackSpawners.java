package xyz.wildseries.wildstacker.loot.custom;

import me.dragons.stackspawners.events.StackSpawnersAPI;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import xyz.wildseries.wildstacker.api.objects.StackedEntity;
import xyz.wildseries.wildstacker.loot.LootTable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LootTableStackSpawners extends LootTableCustom {

    @Override
    public List<ItemStack> getDrops(LootTable lootTable, StackedEntity stackedEntity, int lootBonusLevel, int stackAmount) {
        List<ItemStack> deathLoot = StackSpawnersAPI.getEntityDrops(stackedEntity.getLivingEntity());

        if(deathLoot == null)
            deathLoot = new ArrayList<>();

        deathLoot = deathLoot.stream().filter(itemStack -> itemStack != null && itemStack.getType() != Material.AIR)
                .collect(Collectors.toList());

        return deathLoot.isEmpty() ? lootTable.getDrops(stackedEntity, lootBonusLevel, stackAmount) : deathLoot;
    }

}
