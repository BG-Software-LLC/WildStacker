package com.bgsoftware.wildstacker.loot;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.utils.Random;
import com.bgsoftware.wildstacker.utils.entity.EntityUtils;
import com.bgsoftware.wildstacker.utils.json.JsonUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings({"WeakerAccess", "unchecked"})
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

    static boolean isBurning(StackedEntity stackedEntity) {
        return stackedEntity.getLivingEntity().getFireTicks() > 0;
    }

    @Nullable
    static Entity getEntityKiller(StackedEntity stackedEntity) {
        EntityDamageEvent damageEvent = stackedEntity.getLivingEntity().getLastDamageCause();

        if (damageEvent instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent entityDamageByEntityEvent = (EntityDamageByEntityEvent) damageEvent;
            Entity damager = entityDamageByEntityEvent.getDamager();
            if (damager instanceof Projectile) {
                Projectile projectile = (Projectile) damager;
                return projectile.getShooter() instanceof Entity ? (Entity) projectile.getShooter() : projectile;
            } else {
                return damager;
            }
        }

        return null;
    }

    static boolean isKilledByPlayer(StackedEntity stackedEntity) {
        return getKiller(stackedEntity) != null;
    }

    static Player getKiller(StackedEntity stackedEntity) {
        return stackedEntity.getLivingEntity().getKiller();
    }

    @Nullable
    static EntityDamageEvent.DamageCause getDeathCause(Entity entity) {
        EntityDamageEvent lastCause = entity.getLastDamageCause();
        return lastCause == null ? null : lastCause.getCause();
    }

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
        List<ItemStack> drops = new ArrayList<>();

        List<LootPair> filteredPairs = lootPairs.stream().filter(lootPair ->
                lootPair.checkKiller(getEntityKiller(stackedEntity)) && lootPair.checkEntity(stackedEntity.getLivingEntity())
        ).collect(Collectors.toList());

        int amountOfDifferentPairs = max == -1 || min == -1 ? stackAmount : max == min ? max * stackAmount :
                Random.nextInt(min, max, stackAmount);

        for (LootPair lootPair : filteredPairs) {
            int amountOfPairs = (int) (lootPair.getChance() * amountOfDifferentPairs / 100);

            if (amountOfPairs == 0) {
                amountOfPairs = Random.nextChance(lootPair.getChance(), amountOfDifferentPairs);
            }

            drops.addAll(lootPair.getItems(stackedEntity, amountOfPairs, lootBonusLevel));
            if (isKilledByPlayer(stackedEntity))
                lootPair.executeCommands(getKiller(stackedEntity), amountOfPairs, lootBonusLevel);
        }

        if (dropEquipment) {
            drops.addAll(EntityUtils.getEquipment(stackedEntity.getLivingEntity(), lootBonusLevel));
        }

        EntityUtils.clearEquipment(stackedEntity.getLivingEntity());

        return drops;
    }

    @Override
    public int getExp(StackedEntity stackedEntity, int stackAmount) {
        int exp = 0;

        if (minExp >= 0 && maxExp >= 0) {
            if (alwaysDropsExp || plugin.getNMSAdapter().canDropExp(stackedEntity.getLivingEntity())) {
                for (int i = 0; i < stackAmount; i++)
                    exp += Random.nextInt(maxExp - minExp + 1) + minExp;
            }
        } else {
            exp = plugin.getNMSAdapter().getEntityExp(stackedEntity.getLivingEntity());

            if (exp < 0)
                return exp;

            exp *= stackAmount;
        }

        return exp;
    }

    @Override
    public String toString() {
        return "LootTable{pairs=" + lootPairs + "}";
    }

}
