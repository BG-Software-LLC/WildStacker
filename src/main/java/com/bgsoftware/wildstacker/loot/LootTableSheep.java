package com.bgsoftware.wildstacker.loot;

import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.utils.legacy.Materials;
import org.bukkit.entity.Sheep;
import org.bukkit.inventory.ItemStack;

import java.util.Iterator;
import java.util.List;

public class LootTableSheep extends LootTable {

    public LootTableSheep(List<LootPair> lootPairs, int min, int max, int minExp, int maxExp, boolean dropEquipment, boolean alwaysDropsExp) {
        super(lootPairs, min, max, minExp, maxExp, dropEquipment, alwaysDropsExp);
    }

    @Override
    public List<ItemStack> getDrops(StackedEntity stackedEntity, int lootBonusLevel, int stackAmount) {
        List<ItemStack> drops = super.getDrops(stackedEntity, lootBonusLevel, stackAmount);

        if (stackedEntity.getLivingEntity() instanceof Sheep) {
            Sheep sheep = (Sheep) stackedEntity.getLivingEntity();

            if (sheep.isSheared()) {
                drops.removeIf(Materials::isWool);
            } else {
                Iterator<ItemStack> dropsIterator = drops.iterator();
                int woolCount = 0;

                while (dropsIterator.hasNext()) {
                    ItemStack itemStack = dropsIterator.next();
                    if (Materials.isWool(itemStack)) {
                        woolCount += itemStack.getAmount();
                        dropsIterator.remove();
                    }
                }

                if (woolCount > 0) {
                    ItemStack wool = Materials.getWool(sheep.getColor());
                    wool.setAmount(woolCount);
                    drops.add(wool);
                }
            }
        }

        return drops;
    }

}
