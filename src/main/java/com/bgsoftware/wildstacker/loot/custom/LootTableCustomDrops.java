package com.bgsoftware.wildstacker.loot.custom;

import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.loot.LootTable;
import com.bgsoftware.wildstacker.utils.Executor;
import com.bgsoftware.wildstacker.utils.ItemUtil;
import net.aminecraftdev.customdrops.CustomDropsAPI;
import net.aminecraftdev.customdrops.manager.CustomDropsManager;
import net.aminecraftdev.customdrops.utils.AbstractHolder;
import net.aminecraftdev.customdrops.utils.itemstack.ItemStackUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class LootTableCustomDrops extends LootTableCustom {

    @Override
    public List<ItemStack> getDrops(LootTable lootTable, StackedEntity stackedEntity, int lootBonusLevel, int stackAmount) {
        List<ItemStack> drops = new ArrayList<>();

        if (CustomDropsAPI.getNaturalDrops(stackedEntity.getType()))
            drops.addAll(lootTable.getDrops(stackedEntity, lootBonusLevel, stackAmount));

        for(int i = 0; i < stackAmount - 1; i++) {
            drops.addAll(getCustomDrops(stackedEntity.getType()).stream()
                    .filter(itemStack -> itemStack != null && itemStack.getType() != Material.AIR)
                    .collect(Collectors.toList()));
        }

        //CustomDrops doesn't take items from the event, we need to drop the items.
        Location entityLocation = stackedEntity.getLivingEntity().getLocation();
        Executor.sync(() -> drops.forEach(itemStack -> ItemUtil.dropItem(itemStack, entityLocation)));

        return drops;
    }

    private static CustomDropsManager manager = CustomDropsManager.get();
    private static ThreadLocalRandom random = ThreadLocalRandom.current();

    private List<ItemStack> getCustomDrops(EntityType entityType){
        List<ItemStack> drops = new ArrayList<>();

        AbstractHolder<EntityType> abstractHolder;

        try{
            abstractHolder = CustomDropsManager.getMobData().getAbstractHolder(entityType);
        }catch(Throwable th){
            try {
                //noinspection unchecked,JavaReflectionMemberAccess
                abstractHolder = (AbstractHolder<EntityType>) CustomDropsManager.class.getMethod("getAbstractHolder", EntityType.class)
                        .invoke(manager, entityType);
            }catch(Exception ex){
                ex.printStackTrace();
                return drops;
            }
        }


        if (abstractHolder == null) {
            return drops;
        } else {
            Object dropsObject = abstractHolder.get("drops");
            if (dropsObject != null) {
                //noinspection unchecked
                List<ConfigurationSection> dropSections = (List)dropsObject;
                dropSections.forEach((innerSection) -> {
                    int min = innerSection.getInt("amount.min", 0);
                    int max = innerSection.getInt("amount.max", 0) + 1;
                    int amount = random.nextInt(max - min) + min;
                    if (amount != 0) {
                        if (innerSection.contains("chance")) {
                            double chance = innerSection.getDouble("chance");
                            double randomNumber = (double) random.nextInt(100);
                            randomNumber += random.nextDouble();
                            if (randomNumber > chance) {
                                return;
                            }
                        }

                        //noinspection all
                        drops.add(ItemStackUtils.createItemStack(innerSection, amount, (Map)null));
                    }
                });
            }
        }

        return drops;
    }


}
