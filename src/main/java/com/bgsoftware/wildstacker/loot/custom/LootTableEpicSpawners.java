package com.bgsoftware.wildstacker.loot.custom;

import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.loot.LootTable;
import com.bgsoftware.wildstacker.utils.entity.EntityStorage;
import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.spawners.spawner.SpawnerData;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class LootTableEpicSpawners extends LootTableCustom {

    @Override
    public List<ItemStack> getDrops(LootTable lootTable, StackedEntity stackedEntity, int lootBonusLevel, int stackAmount) {
        List<ItemStack> drops = new ArrayList<>();

        LivingEntity livingEntity = stackedEntity.getLivingEntity();

        if(EntityStorage.hasMetadata(livingEntity, "ES")){
            SpawnerData spawnerData = EpicSpawners.getInstance().getSpawnerManager().getSpawnerData(
                    EntityStorage.getMetadata(livingEntity, "ES", Object.class).toString());
            if(spawnerData != null) {
                drops.addAll(spawnerData.getEntityDroppedItems());

                for(ItemStack itemStack : drops){
                    itemStack.setAmount(itemStack.getAmount() * stackAmount);
                }
            }
        }

        if(drops.isEmpty())
            drops.addAll(lootTable.getDrops(stackedEntity, lootBonusLevel, stackAmount));

        return drops;
    }

}