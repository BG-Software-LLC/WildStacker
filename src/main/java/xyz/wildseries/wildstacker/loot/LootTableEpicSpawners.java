package xyz.wildseries.wildstacker.loot;

import com.songoda.epicspawners.api.EpicSpawnersAPI;
import com.songoda.epicspawners.api.spawner.SpawnerData;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class LootTableEpicSpawners extends LootTable {

    @Override
    protected int getMaximumAmount() {
        return 0;
    }

    @Override
    protected int getMinimumAmount() {
        return 0;
    }

    @Override
    protected ItemStack getLoot() {
        return null;
    }

    @Override
    public List<ItemStack> getDeathLoot(int lootBonusLevel) {
        List<ItemStack> deathLoot = new ArrayList<>();

        if(livingEntity.hasMetadata("ES") && livingEntity.getMetadata("ES").size() > 0) {
            SpawnerData spawnerData = EpicSpawnersAPI.getSpawnerManager().getSpawnerData((livingEntity.getMetadata("ES").get(0)).asString());
            if(spawnerData != null) {
                deathLoot.addAll(spawnerData.getEntityDroppedItems());

                for(ItemStack itemStack : deathLoot){
                    itemStack.setAmount(itemStack.getAmount() * getStackAmount());
                }
            }
        }

        if(deathLoot.isEmpty())
            deathLoot.addAll(LootTable.forNaturalEntity(livingEntity).getDeathLoot(lootBonusLevel));

        return deathLoot;
    }

    public static void register(){
        try {
            registerCustomLootTable(new LootTableEpicSpawners());
        }catch(RuntimeException ignored){}
    }

}
