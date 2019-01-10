package xyz.wildseries.wildstacker.loot;

import net.aminecraftdev.customdrops.CustomDropsAPI;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LootTableCustomDrops extends LootTable {

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

        if (CustomDropsAPI.getNaturalDrops(livingEntity.getType()))
            deathLoot.addAll(LootTable.forNaturalEntity(livingEntity).getDeathLoot(lootBonusLevel));

        for(int i = 0; i < getStackAmount(); i++) {
            deathLoot.addAll(CustomDropsAPI.getCustomDrops(livingEntity.getType()).stream()
                    .filter(itemStack -> itemStack != null && itemStack.getType() != Material.AIR)
                    .collect(Collectors.toList()));

        }

        return deathLoot;
    }

    public static void register(){
        try {
            registerCustomLootTable(new LootTableCustomDrops());
        }catch(RuntimeException ignored){}
    }

}
