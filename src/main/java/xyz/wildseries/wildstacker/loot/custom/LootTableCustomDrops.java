package xyz.wildseries.wildstacker.loot.custom;

import net.aminecraftdev.customdrops.CustomDropsAPI;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import xyz.wildseries.wildstacker.api.objects.StackedEntity;
import xyz.wildseries.wildstacker.loot.LootTable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LootTableCustomDrops extends LootTableCustom {

    @Override
    public List<ItemStack> getDrops(LootTable lootTable, StackedEntity stackedEntity, int lootBonusLevel) {
        List<ItemStack> drops = new ArrayList<>();

        if (CustomDropsAPI.getNaturalDrops(stackedEntity.getType()))
            drops.addAll(lootTable.getDrops(stackedEntity, lootBonusLevel));

        for(int i = 0; i < stackedEntity.getStackAmount() - 1; i++) {
            drops.addAll(CustomDropsAPI.getCustomDrops(stackedEntity.getType()).stream()
                    .filter(itemStack -> itemStack != null && itemStack.getType() != Material.AIR)
                    .collect(Collectors.toList()));
        }

        //CustomDrops doesn't take items from the event, we need to drop the items.
        Location entityLocation = stackedEntity.getLivingEntity().getLocation();
        drops.forEach(itemStack -> entityLocation.getWorld().dropItemNaturally(entityLocation, itemStack));

        return drops;
    }


}
