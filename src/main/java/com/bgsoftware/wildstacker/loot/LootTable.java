package com.bgsoftware.wildstacker.loot;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.loot.LootEntityAttributes;
import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.loot.entity.LivingLootEntityAttributes;
import com.bgsoftware.wildstacker.utils.Random;
import com.bgsoftware.wildstacker.utils.entity.EntityUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;

public class LootTable implements com.bgsoftware.wildstacker.api.loot.LootTable {

    private static final WildStackerPlugin plugin = WildStackerPlugin.getPlugin();

    private final List<LootPair> lootPairs = new LinkedList<>();
    private final int min, max, minExp, maxExp;
    private final boolean dropEquipment, alwaysDropsExp;

    public LootTable(List<LootPair> lootPairs, int min, int max, int minExp, int maxExp, boolean dropEquipment, boolean alwaysDropsExp) {
        this.lootPairs.addAll(lootPairs);
        this.min = min;
        this.max = max;
        this.minExp = minExp;
        this.maxExp = maxExp;
        this.dropEquipment = dropEquipment;
        this.alwaysDropsExp = alwaysDropsExp;
    }

    @Override
    public List<ItemStack> getDrops(StackedEntity stackedEntity, int lootBonusLevel, int stackAmount) {
        return getDrops(LootEntityAttributes.newBuilder(stackedEntity).build(), lootBonusLevel, stackAmount);
    }

    @Override
    public List<ItemStack> getDrops(LootEntityAttributes lootEntityAttributes, int lootBonusLevel, int stackAmount) {
        List<ItemStack> drops = new LinkedList<>();

        LootEntityAttributes directKillerEntityData = lootEntityAttributes.getKiller();
        LootEntityAttributes vehicleEntityData = lootEntityAttributes.getVehicle();

        int amountOfDifferentPairs = max == -1 || min == -1 ? stackAmount : max == min ? max * stackAmount :
                Random.nextInt(min, max, stackAmount);

        for (LootPair lootPair : this.lootPairs) {
            if (!lootPair.checkEntity(lootEntityAttributes) ||
                    (!lootEntityAttributes.isIgnoreEntityKiller() && !lootPair.checkKiller(directKillerEntityData)) ||
                    (!lootEntityAttributes.isIgnoreEntityVehicle() && !lootPair.checkVehicle(vehicleEntityData)))
                continue;

            int amountOfPairs = (int) (lootPair.getChance() * amountOfDifferentPairs / 100);

            if (amountOfPairs == 0) {
                amountOfPairs = Random.nextChance(lootPair.getChance(), amountOfDifferentPairs);
            }

            drops.addAll(lootPair.getItems(lootEntityAttributes, amountOfPairs, lootBonusLevel));
            LootEntityAttributes sourceKillerEntityData = getKillerSourceEntityData(directKillerEntityData);
            LootEntityAttributes killerEntityDataToCheck = sourceKillerEntityData == null ? directKillerEntityData : sourceKillerEntityData;
            if (killerEntityDataToCheck instanceof LivingLootEntityAttributes &&
                    killerEntityDataToCheck.getEntityType() == EntityType.PLAYER) {
                lootPair.executeCommands((Player) ((LivingLootEntityAttributes) killerEntityDataToCheck).getEntity(), amountOfPairs, lootBonusLevel);
            }
        }

        if (lootEntityAttributes instanceof LivingLootEntityAttributes) {
            Entity entity = ((LivingLootEntityAttributes) lootEntityAttributes).getEntity();
            if (entity instanceof LivingEntity) {
                if (dropEquipment) {
                    drops.addAll(EntityUtils.getEquipment((LivingEntity) entity, lootBonusLevel));
                }

                EntityUtils.clearEquipment((LivingEntity) entity);
            }
        }

        return drops;
    }

    @Override
    public int getExp(StackedEntity stackedEntity, int stackAmount) {
        return getExp(LootEntityAttributes.newBuilder(stackedEntity).build(), stackAmount);
    }

    @Override
    public int getExp(LootEntityAttributes lootEntityAttributes, int stackAmount) {
        int exp = 0;

        if (minExp >= 0 && maxExp >= 0) {
            if (alwaysDropsExp) {
                Entity entity = lootEntityAttributes instanceof LivingLootEntityAttributes ?
                        ((LivingLootEntityAttributes) lootEntityAttributes).getEntity() : null;
                if (entity == null || (entity instanceof LivingEntity &&
                        plugin.getNMSEntities().canDropExp((LivingEntity) entity))) {
                    for (int i = 0; i < stackAmount; i++)
                        exp += Random.nextInt(maxExp - minExp + 1) + minExp;
                }
            }
        } else if (lootEntityAttributes instanceof LivingLootEntityAttributes) {
            Entity entity = ((LivingLootEntityAttributes) lootEntityAttributes).getEntity();
            if (entity instanceof LivingEntity) {
                exp = plugin.getNMSEntities().getEntityExp((LivingEntity) entity);

                if (exp < 0)
                    return exp;

                exp *= stackAmount;
            }
        }

        return exp;
    }

    @Override
    public String toString() {
        return "LootTable{pairs=" + lootPairs + "}";
    }

    @Nullable
    private static LootEntityAttributes getKillerSourceEntityData(LootEntityAttributes killerEntityData) {
        if (!(killerEntityData instanceof LivingLootEntityAttributes))
            return null;

        Entity directKiller = ((LivingLootEntityAttributes) killerEntityData).getEntity();
        Entity sourceKiller = EntityUtils.getSourceDamager(directKiller, true);

        return sourceKiller == directKiller ? null : LootEntityAttributes.newBuilder(sourceKiller).build();
    }

}
