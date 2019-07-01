package com.bgsoftware.wildstacker.loot;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

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

        Map<LootPair, Integer> lootPairs = getLootPairs(stackedEntity, stackAmount);

        for(LootPair lootPair : lootPairs.keySet()) {
            drops.addAll(lootPair.getItems(stackedEntity, lootBonusLevel, lootPairs.get(lootPair)));
            if(isKilledByPlayer(stackedEntity))
                lootPair.executeCommands(getKiller(stackedEntity), lootBonusLevel);
        }

//        for(int i = 0; i < stackAmount; i++) {
//            List<LootPair> lootPairs = getLootPairs(stackedEntity);
//
//            for(LootPair lootPair : lootPairs) {
//                drops.addAll(lootPair.getItems(stackedEntity, lootBonusLevel));
//                if(isKilledByPlayer(stackedEntity))
//                    lootPair.executeCommands(getKiller(stackedEntity), lootBonusLevel);
//            }
//        }

        if(dropEquipment) {
            drops.addAll(plugin.getNMSAdapter().getEquipment(stackedEntity.getLivingEntity()));
            clearEquipment(stackedEntity.getLivingEntity().getEquipment());
        }

        return drops;
    }

    @SuppressWarnings("JavaReflectionMemberAccess")
    private void clearEquipment(EntityEquipment entityEquipment){
        if(Bukkit.getBukkitVersion().contains("1.8")) {
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
    @Deprecated
    public int getExp(int stackAmount, int defaultExp){
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int exp = defaultExp * stackAmount;

        if(minExp >= 0 && maxExp >= 0){
            exp = 0;
            for(int i = 0; i < stackAmount; i++){
                exp += random.nextInt(maxExp - minExp + 1) + minExp;
            }
        }

        return exp;
    }

    @Override
    public int getExp(StackedEntity stackedEntity, int stackAmount) {
        Random random = plugin.getNMSAdapter().getWorldRandom(stackedEntity.getLivingEntity().getWorld());
        int exp = 0;

        if(minExp >= 0 && maxExp >= 0){
            for(int i = 0; i < stackAmount; i++)
                exp += random.nextInt(maxExp - minExp + 1) + minExp;
        }
        else{
            for(int i = 0; i < stackAmount; i++)
                exp += plugin.getNMSAdapter().getEntityExp(stackedEntity.getLivingEntity());
        }

        return exp;
    }

    private Map<LootPair, Integer> getLootPairs(StackedEntity stackedEntity, int iterations){
        Random random = plugin.getNMSAdapter().getWorldRandom(stackedEntity.getLivingEntity().getWorld());
        Map<LootPair, Integer> lootPairs = new HashMap<>();

        //Collections.shuffle(this.lootPairs, random);

        int sum = (max - min + 1) * (min + max) / 2;
        int iteration = iterations / (max - min + 1) * sum;
        if(max < 0 || min < 0)
            iteration = iterations;

        for(LootPair lootPair : this.lootPairs){
            lootPairs.put(lootPair, (int) ((lootPair.getChance() * iteration) / 100));
        }

//        for(LootPair lootPair : this.lootPairs){
//            if(max != -1 && lootPairs.size() >= max)
//                break;
//            if(!lootPair.getKiller().isEmpty() && !lootPair.getKiller().contains(getEntityKiller(stackedEntity).name()))
//                continue;
//            if(random.nextInt(100) < lootPair.getChance()) {
//                lootPairs.put(lootPair, lootPairs.getOrDefault(lootPair, 0) + 1);
//            }
//        }
//
//        if(min != -1 && lootPairs.size() < min) {
//            combineMaps(lootPairs, getLootPairs(stackedEntity));
//        }

        return lootPairs;
    }

    static <T> void combineMaps(Map<T, Integer> original, Map<T, Integer> toAdd){
        for(T object : toAdd.keySet()){
            original.put(object, original.getOrDefault(object, 0) + toAdd.get(object));
        }
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

    private static boolean isKilledByPlayer(StackedEntity stackedEntity){
        return getKiller(stackedEntity) != null;
    }

    private static Player getKiller(StackedEntity stackedEntity){
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
