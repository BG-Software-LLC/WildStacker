package xyz.wildseries.wildstacker.loot.custom;

import com.songoda.epicspawners.api.EpicSpawnersAPI;
import com.songoda.epicspawners.api.spawner.SpawnerData;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import xyz.wildseries.wildstacker.api.objects.StackedEntity;
import xyz.wildseries.wildstacker.loot.LootTable;

import java.util.ArrayList;
import java.util.List;

public class LootTableEpicSpawners extends LootTableCustom {

    @Override
    public List<ItemStack> getDrops(LootTable lootTable, StackedEntity stackedEntity, int lootBonusLevel) {
        List<ItemStack> drops = new ArrayList<>();

        LivingEntity livingEntity = stackedEntity.getLivingEntity();

        if(livingEntity.hasMetadata("ES") && livingEntity.getMetadata("ES").size() > 0) {
            SpawnerData spawnerData = EpicSpawnersAPI.getSpawnerManager().getSpawnerData((livingEntity.getMetadata("ES").get(0)).asString());
            if(spawnerData != null) {
                drops.addAll(spawnerData.getEntityDroppedItems());

                for(ItemStack itemStack : drops){
                    itemStack.setAmount(itemStack.getAmount() * stackedEntity.getStackAmount());
                }
            }
        }

        if(drops.isEmpty())
            drops.addAll(lootTable.getDrops(stackedEntity, lootBonusLevel));

        return drops;
    }

}
