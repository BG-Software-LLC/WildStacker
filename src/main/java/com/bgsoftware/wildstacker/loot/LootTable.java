package com.bgsoftware.wildstacker.loot;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.loot.LootEntityAttributes;
import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.loot.entity.LivingLootEntityAttributes;
import com.bgsoftware.wildstacker.utils.Random;
import com.bgsoftware.wildstacker.utils.entity.EntityUtils;
import com.bgsoftware.wildstacker.utils.json.JsonUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

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

//    @Nullable
//    static Entity getEntityKiller(StackedEntity stackedEntity) {
//        Entity entityKillerCached = stackedEntity.getFlag(EntityFlag.CACHED_KILLER);
//        if (entityKillerCached != null)
//            return entityKillerCached;
//
//        return EntityUtils.getDamagerFromEvent(stackedEntity.getLivingEntity().getLastDamageCause(), false);
//    }
//
//    static boolean isKilledByPlayer(StackedEntity stackedEntity) {
//        return getKiller(stackedEntity) != null;
//    }
//
//    static Player getKiller(StackedEntity stackedEntity) {
//        return stackedEntity.getLivingEntity().getKiller();
//    }
//
//    @Nullable
//    static EntityDamageEvent.DamageCause getDeathCause(Entity entity) {
//        EntityDamageEvent lastCause = entity.getLastDamageCause();
//        return lastCause == null ? null : lastCause.getCause();
//    }

    public static LootTable fromJson(JSONObject jsonObject, String lootTableName) {
        boolean dropEquipment = (boolean) jsonObject.getOrDefault("dropEquipment", true);
        boolean alwaysDropsExp = false;
        int min = JsonUtils.getInt(jsonObject, "min", -1);
        int max = JsonUtils.getInt(jsonObject, "max", -1);
        int minExp = -1, maxExp = -1;

        if (jsonObject.containsKey("exp")) {
            JSONObject expObject = (JSONObject) jsonObject.get("exp");
            minExp = JsonUtils.getInt(expObject, "min", -1);
            maxExp = JsonUtils.getInt(expObject, "max", -1);
            alwaysDropsExp = (boolean) expObject.getOrDefault("always-drop", false);
        }

        List<LootPair> lootPairs = new ArrayList<>();
        if (jsonObject.containsKey("pairs")) {
            ((JSONArray) jsonObject.get("pairs")).forEach(element -> lootPairs.add(LootPair.fromJson(((JSONObject) element), lootTableName)));
        }

        return new LootTable(lootPairs, min, max, minExp, maxExp, dropEquipment, alwaysDropsExp);
    }

    @Override
    public List<ItemStack> getDrops(StackedEntity stackedEntity, int lootBonusLevel, int stackAmount) {
        return getDrops(LootEntityAttributes.newBuilder(stackedEntity).build(), lootBonusLevel, stackAmount);
    }

    @Override
    public List<ItemStack> getDrops(LootEntityAttributes lootEntityAttributes, int lootBonusLevel, int stackAmount) {
        List<ItemStack> drops = new LinkedList<>();

        LootEntityAttributes directKillerEntityData = lootEntityAttributes.getKiller();

        List<LootPair> filteredPairs = lootPairs.stream().filter(lootPair ->
                lootPair.checkKiller(directKillerEntityData) && lootPair.checkEntity(lootEntityAttributes)
        ).collect(Collectors.toList());

        int amountOfDifferentPairs = max == -1 || min == -1 ? stackAmount : max == min ? max * stackAmount :
                Random.nextInt(min, max, stackAmount);

        for (LootPair lootPair : filteredPairs) {
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
