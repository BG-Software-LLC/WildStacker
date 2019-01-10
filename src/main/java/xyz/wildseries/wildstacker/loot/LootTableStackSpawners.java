package xyz.wildseries.wildstacker.loot;

import me.dragons.stackspawners.events.StackSpawnersAPI;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LootTableStackSpawners extends LootTable {

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
        List<ItemStack> deathLoot = StackSpawnersAPI.getEntityDrops(livingEntity);

        if(deathLoot == null)
            deathLoot = new ArrayList<>();

        deathLoot = deathLoot.stream().filter(itemStack -> itemStack != null && itemStack.getType() != Material.AIR)
                .collect(Collectors.toList());

        return deathLoot.isEmpty() ? LootTable.forNaturalEntity(livingEntity).getDeathLoot(lootBonusLevel) : deathLoot;
    }

    public static void register(){
        try {
            registerCustomLootTable(new LootTableStackSpawners());
        }catch(RuntimeException ignored){}
    }

}
