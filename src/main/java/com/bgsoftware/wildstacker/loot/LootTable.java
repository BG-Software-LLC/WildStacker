package com.bgsoftware.wildstacker.loot;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.utils.Random;
import com.bgsoftware.wildstacker.utils.ServerVersion;
import com.google.gson.JsonObject;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("WeakerAccess")
public class LootTable implements com.bgsoftware.wildstacker.api.loot.LootTable {

    private static WildStackerPlugin plugin = WildStackerPlugin.getPlugin();

    private final List<LootPair> lootPairs = new ArrayList<>();
    private final int min, max, minExp, maxExp;
    private final boolean dropEquipment;

    public LootTable(List<LootPair> lootPairs, int min, int max, int minExp, int maxExp, boolean dropEquipment){
        this.lootPairs.addAll(lootPairs);
        this.min = min;
        this.max = max;
        this.minExp = minExp;
        this.maxExp = maxExp;
        this.dropEquipment = dropEquipment;
    }

    @Override
    public List<ItemStack> getDrops(StackedEntity stackedEntity, int lootBonusLevel, int stackAmount){
        List<ItemStack> drops = new ArrayList<>();

        List<LootPair> filteredPairs = lootPairs.stream().filter(lootPair ->
            (lootPair.getKiller().isEmpty() || lootPair.getKiller().contains(getEntityKiller(stackedEntity).name())) &&
            (lootPair.getRequiredPermission().isEmpty() || !isKilledByPlayer(stackedEntity) || getKiller(stackedEntity).hasPermission(lootPair.getRequiredPermission())) &&
            (lootPair.getSpawnCauseFilter().isEmpty() || stackedEntity.getSpawnCause().name().equals(lootPair.getSpawnCauseFilter()))
        ).collect(Collectors.toList());

        int amountOfDifferentPairs = max == -1 || min == -1 ? -1 : stackAmount < 10 ?
                Random.nextInt((max * stackAmount) - (min * stackAmount) + 1) + (min * stackAmount) :
                Random.nextInt(min * stackAmount, max * stackAmount);
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
            drops.addAll(plugin.getNMSAdapter().getEquipment(stackedEntity.getLivingEntity()));
            clearEquipment(stackedEntity.getLivingEntity().getEquipment());
        }

        return drops;
    }

    @SuppressWarnings("JavaReflectionMemberAccess")
    private void clearEquipment(EntityEquipment entityEquipment){
        if(ServerVersion.isEquals(ServerVersion.v1_8)) {
            if (entityEquipment.getItemInHandDropChance() >= 2.0F)
                entityEquipment.setItemInHand(new ItemStack(Material.AIR));
        }else{
            try{
                if((Float) EntityEquipment.class.getMethod("getItemInMainHandDropChance").invoke(entityEquipment) >= 2.0F)
                    EntityEquipment.class.getMethod("setItemInMainHand", ItemStack.class).invoke(entityEquipment, new ItemStack(Material.AIR));
                if((Float) EntityEquipment.class.getMethod("getItemInOffHandDropChance").invoke(entityEquipment) >= 2.0F)
                    EntityEquipment.class.getMethod("setItemInOffHand", ItemStack.class).invoke(entityEquipment, new ItemStack(Material.AIR));
            }catch(Exception ex){
                ex.printStackTrace();
            }
        }
        if(entityEquipment.getHelmetDropChance() >= 2.0F)
            entityEquipment.setHelmet(new ItemStack(Material.AIR));
        if(entityEquipment.getChestplateDropChance() >= 2.0F)
            entityEquipment.setChestplate(new ItemStack(Material.AIR));
        if(entityEquipment.getLeggingsDropChance() >= 2.0F)
            entityEquipment.setLeggings(new ItemStack(Material.AIR));
        if(entityEquipment.getBootsDropChance() >= 2.0F)
            entityEquipment.setBoots(new ItemStack(Material.AIR));
    }

    @Override
    public int getExp(StackedEntity stackedEntity, int stackAmount) {
        int exp = 0;

        if(minExp >= 0 && maxExp >= 0){
            for(int i = 0; i < stackAmount; i++)
                exp += Random.nextInt(maxExp - minExp + 1) + minExp;
        }
        else{
            for(int i = 0; i < stackAmount; i++)
                exp += plugin.getNMSAdapter().getEntityExp(stackedEntity.getLivingEntity());
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

    static EntityType getEntityKiller(StackedEntity stackedEntity){
        EntityDamageEvent damageEvent = stackedEntity.getLivingEntity().getLastDamageCause();
        EntityType returnType = EntityType.UNKNOWN;

        if(damageEvent instanceof EntityDamageByEntityEvent){
            EntityDamageByEntityEvent entityDamageByEntityEvent = (EntityDamageByEntityEvent) damageEvent;
            if(entityDamageByEntityEvent.getDamager() instanceof Projectile) {
                Projectile projectile = (Projectile) entityDamageByEntityEvent.getDamager();
                if(projectile.getShooter() instanceof Entity)
                    returnType = ((Entity) projectile.getShooter()).getType();
                else
                    returnType = projectile.getType();
            }else{
                returnType = entityDamageByEntityEvent.getDamager().getType();
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

    public static LootTable fromJson(JsonObject jsonObject){
        boolean dropEquipment = !jsonObject.has("dropEquipment") || jsonObject.get("dropEquipment").getAsBoolean();
        int min = jsonObject.has("min") ? jsonObject.get("min").getAsInt() : -1;
        int max = jsonObject.has("max") ? jsonObject.get("max").getAsInt() : -1;
        int minExp = -1, maxExp = -1;

        if(jsonObject.has("exp")){
            JsonObject expObject = jsonObject.getAsJsonObject("exp");
            minExp = expObject.get("min").getAsInt();
            maxExp = expObject.get("max").getAsInt();
        }

        List<LootPair> lootPairs = new ArrayList<>();
        if(jsonObject.has("pairs")){
            jsonObject.get("pairs").getAsJsonArray().forEach(element -> lootPairs.add(LootPair.fromJson(element.getAsJsonObject())));
        }

        return new LootTable(lootPairs, min, max, minExp, maxExp, dropEquipment);
    }

}
