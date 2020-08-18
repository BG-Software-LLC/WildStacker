package com.bgsoftware.wildstacker.loot;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.utils.GeneralUtils;
import com.bgsoftware.wildstacker.utils.Random;
import com.bgsoftware.wildstacker.utils.entity.EntityUtils;
import com.bgsoftware.wildstacker.utils.json.JsonUtils;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings({"WeakerAccess", "unchecked"})
public class LootTable implements com.bgsoftware.wildstacker.api.loot.LootTable {

    private static final WildStackerPlugin plugin = WildStackerPlugin.getPlugin();

    private final List<LootPair> lootPairs = new ArrayList<>();
    private final int min, max, minExp, maxExp;
    private final boolean dropEquipment, alwaysDropsExp;

    public LootTable(List<LootPair> lootPairs, int min, int max, int minExp, int maxExp, boolean dropEquipment, boolean alwaysDropsExp){
        this.lootPairs.addAll(lootPairs);
        this.min = min;
        this.max = max;
        this.minExp = minExp;
        this.maxExp = maxExp;
        this.dropEquipment = dropEquipment;
        this.alwaysDropsExp = alwaysDropsExp;
    }

    @Override
    public List<ItemStack> getDrops(StackedEntity stackedEntity, int lootBonusLevel, int stackAmount){
        List<ItemStack> drops = new ArrayList<>();

        List<LootPair> filteredPairs = lootPairs.stream().filter(lootPair ->
            (lootPair.getKiller().isEmpty() || lootPair.getKiller().contains(getEntityKiller(stackedEntity))) &&
            (lootPair.getRequiredPermission().isEmpty() || !isKilledByPlayer(stackedEntity) || getKiller(stackedEntity).hasPermission(lootPair.getRequiredPermission())) &&
            GeneralUtils.containsOrEmpty(lootPair.getSpawnCauseFilter(), stackedEntity.getSpawnCause().name()) &&
            GeneralUtils.containsOrEmpty(lootPair.getDeathCauseFilter(), getDeathCause(stackedEntity))
        ).collect(Collectors.toList());

        int amountOfDifferentPairs = max == -1 || min == -1 ? -1 : max == min ? max : Random.nextInt(max - min + 1) + min;
        int chosenPairs = 0;

        do{
            for (LootPair lootPair : filteredPairs) {
                if(chosenPairs == amountOfDifferentPairs)
                    break;

                int amountOfPairs = (int) (lootPair.getChance() * stackAmount / 100);

                if (amountOfPairs == 0) {
                    amountOfPairs = Random.nextChance(lootPair.getChance(), stackAmount);
                }

                if (amountOfPairs > 0)
                    chosenPairs++;

                drops.addAll(lootPair.getItems(stackedEntity, amountOfPairs, lootBonusLevel));
                if (isKilledByPlayer(stackedEntity))
                    lootPair.executeCommands(getKiller(stackedEntity), amountOfPairs, lootBonusLevel);
            }
        }while(chosenPairs != amountOfDifferentPairs && amountOfDifferentPairs != -1);

        if(dropEquipment) {
            drops.addAll(EntityUtils.getEquipment(stackedEntity.getLivingEntity(), lootBonusLevel));
        }

        EntityUtils.clearEquipment(stackedEntity.getLivingEntity());

        return drops;
    }

    @Override
    public int getExp(StackedEntity stackedEntity, int stackAmount) {
        int exp = 0;

        if(minExp >= 0 && maxExp >= 0){
            if(alwaysDropsExp || plugin.getNMSAdapter().canDropExp(stackedEntity.getLivingEntity())) {
                for (int i = 0; i < stackAmount; i++)
                    exp += Random.nextInt(maxExp - minExp + 1) + minExp;
            }
        }
        else{
            exp = stackAmount * plugin.getNMSAdapter().getEntityExp(stackedEntity.getLivingEntity());
        }

        return exp;
    }

    @Override
    public String toString() {
        return "LootTable{pairs=" + lootPairs + "}";
    }

    static boolean isBurning(StackedEntity stackedEntity){
        return stackedEntity.getLivingEntity().getFireTicks() > 0;
    }

    static String getEntityKiller(StackedEntity stackedEntity){
        EntityDamageEvent damageEvent = stackedEntity.getLivingEntity().getLastDamageCause();
        String returnType = "UNKNOWN";

        if(damageEvent instanceof EntityDamageByEntityEvent){
            EntityDamageByEntityEvent entityDamageByEntityEvent = (EntityDamageByEntityEvent) damageEvent;
            Entity damager = entityDamageByEntityEvent.getDamager();
            if(damager instanceof Projectile) {
                Projectile projectile = (Projectile) damager;
                if(projectile.getShooter() instanceof Entity)
                    returnType = ((Entity) projectile.getShooter()).getType().name();
                else
                    returnType = projectile.getType().name();
            }else{
                if(damager instanceof Creeper && ((Creeper) damager).isPowered())
                    returnType = "CHARGED_CREEPER";
                else
                    returnType = damager.getType().name();
            }
        }

        return returnType;
    }

    static boolean isKilledByPlayer(StackedEntity stackedEntity){
        return getKiller(stackedEntity) != null;
    }

    static Player getKiller(StackedEntity stackedEntity){
        return stackedEntity.getLivingEntity().getKiller();
    }

    static String getDeathCause(StackedEntity stackedEntity){
        EntityDamageEvent lastCause = stackedEntity.getLivingEntity().getLastDamageCause();
        return lastCause == null ? "" : lastCause.getCause().name();
    }

    public static LootTable fromJson(JSONObject jsonObject, String lootTableName){
        boolean dropEquipment = (boolean) jsonObject.getOrDefault("dropEquipment", true);
        boolean alwaysDropsExp = false;
        int min = JsonUtils.getInt(jsonObject, "min", -1);
        int max = JsonUtils.getInt(jsonObject, "max", -1);
        int minExp = -1, maxExp = -1;

        if(jsonObject.containsKey("exp")){
            JSONObject expObject = (JSONObject) jsonObject.get("exp");
            minExp = JsonUtils.getInt(expObject, "min", -1);
            maxExp = JsonUtils.getInt(expObject, "max", -1);
            alwaysDropsExp = (boolean) expObject.getOrDefault("always-drop", false);
        }

        List<LootPair> lootPairs = new ArrayList<>();
        if(jsonObject.containsKey("pairs")){
            ((JSONArray) jsonObject.get("pairs")).forEach(element -> lootPairs.add(LootPair.fromJson(((JSONObject) element), lootTableName)));
        }

        return new LootTable(lootPairs, min, max, minExp, maxExp, dropEquipment, alwaysDropsExp);
    }

}
