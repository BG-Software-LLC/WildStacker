package com.bgsoftware.wildstacker.loot.custom;

import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.loot.LootTable;
import me.dragons.stackspawners.events.StackSpawnersAPI;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class LootTableStackSpawners extends LootTableCustom {

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