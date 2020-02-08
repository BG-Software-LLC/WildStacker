package com.bgsoftware.wildstacker.utils.entity;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.enums.SpawnCause;
import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.hooks.MythicMobsHook;
import com.bgsoftware.wildstacker.key.Key;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Parrot;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@SuppressWarnings("UnusedReturnValue")
public final class EntityUtils {

    private static WildStackerPlugin plugin = WildStackerPlugin.getPlugin();

    public static boolean areEquals(StackedEntity en1, StackedEntity en2){
        if(en1.getType() != en2.getType())
            return false;

        if(!MythicMobsHook.areSimilar(en1.getUniqueId(), en2.getUniqueId()))
            return false;

        //EpicSpawners drops data
        if(EntityStorage.hasMetadata(en1.getLivingEntity(), "ES") !=
                EntityStorage.hasMetadata(en2.getLivingEntity(), "ES"))
            return false;

        return EntityData.of(en1).equals(EntityData.of(en2));
    }

    public static String getFormattedType(String typeName) {
        if(typeName.contains(String.valueOf(ChatColor.COLOR_CHAR)))
            return typeName;

        if(plugin.getSettings().customNames.containsKey(Key.of(typeName)))
            return plugin.getSettings().customNames.get(Key.of(typeName));

        StringBuilder name = new StringBuilder();

        typeName = typeName.replace(" ", "_");

        for (String section : typeName.split("_")) {
            name.append(section.substring(0, 1).toUpperCase()).append(section.substring(1).toLowerCase()).append(" ");
        }

        return name.substring(0, name.length() - 1);
    }

    public static boolean isNameBlacklisted(String name){
        if(name == null)
            return false;

        if(ChatColor.getLastColors(name).isEmpty())
            name = ChatColor.WHITE + name;

        List<String> blacklistedNames = plugin.getSettings().blacklistedEntitiesNames;

        for(String _name : blacklistedNames){
            if(Pattern.compile(ChatColor.translateAlternateColorCodes('&', _name)).matcher(name).matches())
                return true;
        }

        return false;
    }

    public static void setKiller(LivingEntity livingEntity, Player killer){
        plugin.getNMSAdapter().setKiller(livingEntity, killer);
        if(killer != null)
            plugin.getNMSAdapter().updateLastDamageTime(livingEntity);
    }

    @SuppressWarnings({"JavaReflectionMemberAccess", "JavaReflectionInvocation"})
    public static void removeParrotIfShoulder(Parrot parrot){
        List<Entity> nearbyPlayers = plugin.getNMSAdapter().getNearbyEntities(parrot, 1, en -> en instanceof Player);

        try {
            for (Entity entity : nearbyPlayers) {
                if(parrot.equals(HumanEntity.class.getMethod("getShoulderEntityRight").invoke(entity))){
                    HumanEntity.class.getMethod("setShoulderEntityRight", Entity.class).invoke(entity, (Object) null);
                    break;
                }
                if(parrot.equals(HumanEntity.class.getMethod("getShoulderEntityLeft").invoke(entity))){
                    HumanEntity.class.getMethod("setShoulderEntityLeft", Entity.class).invoke(entity, (Object) null);
                    break;
                }
            }
        }catch(Exception ignored){}
    }

    public static boolean isStackable(Entity entity){
        return MythicMobsHook.isMythicMob(entity) ||
                (entity instanceof LivingEntity && !(entity instanceof ArmorStand) && !(entity instanceof Player) && !entity.hasMetadata("NPC"));
    }

    public static void spawnExp(Location location, int amount){
        Optional<Entity> closestOrb = location.getWorld().getNearbyEntities(location, 2, 2 ,2)
                .stream().filter(entity -> entity instanceof ExperienceOrb).findFirst();

        ExperienceOrb experienceOrb;

        if(closestOrb.isPresent()){
            experienceOrb = (ExperienceOrb) closestOrb.get();
            experienceOrb.setExperience(experienceOrb.getExperience() + amount);
        }
        else{
            experienceOrb = location.getWorld().spawn(location, ExperienceOrb.class);
            experienceOrb.setExperience(amount);
        }
    }

    public static String getEntityName(StackedEntity stackedEntity){
        int stackAmount = stackedEntity.getStackAmount();

        if(stackedEntity.getSpawnCause() == SpawnCause.MYTHIC_MOBS && stackedEntity.getLivingEntity().getCustomName() != null) {
            return MythicMobsHook.getMythicName(stackedEntity.getLivingEntity()).replace("{}", String.valueOf(stackAmount));
        }

        String customName = plugin.getSettings().entitiesCustomName;

        if (customName.isEmpty())
            throw new NullPointerException();

        String newName = "";

        if(stackAmount > 1) {
            newName = customName
                    .replace("{0}", Integer.toString(stackAmount))
                    .replace("{1}", EntityUtils.getFormattedType(stackedEntity.getType().name()))
                    .replace("{2}", EntityUtils.getFormattedType(stackedEntity.getType().name()).toUpperCase());
        }

        return newName;
    }

    public static String getEntityNameRegex(StackedEntity stackedEntity){
        String customName;

        if(stackedEntity.getSpawnCause() == SpawnCause.MYTHIC_MOBS && stackedEntity.getLivingEntity().getCustomName() != null) {
            customName = stackedEntity.getLivingEntity().getCustomName().replace("{}", "\n");
        }

        else {
            customName = plugin.getSettings().entitiesCustomName;

            if (customName.isEmpty())
                throw new NullPointerException();

            if (stackedEntity.getStackAmount() > 1) {
                customName = customName
                        .replace("{0}", "\n")
                        .replace("{1}", EntityUtils.getFormattedType(stackedEntity.getType().name()))
                        .replace("{2}", EntityUtils.getFormattedType(stackedEntity.getType().name()).toUpperCase());
            }
        }

        String[] nameSections = customName.split("\n");

        return "(.*)" + (nameSections.length == 1 ? nameSections[0] : Pattern.quote(nameSections[0]) + "([0-9]+)" + Pattern.quote(nameSections[1])) + "(.*)";
    }

    public static int getBadOmenAmplifier(Player player){
        int amplifier = 0;

        for(PotionEffect potionEffect : player.getActivePotionEffects()){
            if(potionEffect.getType().getName().equals("BAD_OMEN")) {
                amplifier = potionEffect.getAmplifier() + 1;
                break;
            }
        }

        if(amplifier >= 0 && amplifier <= 5)
            player.removePotionEffect(PotionEffectType.getByName("BAD_OMEN"));

        return Math.max(0, Math.min(5, amplifier));
    }

    public static boolean killedByZombie(LivingEntity livingEntity){
        EntityDamageEvent entityDamageEvent = livingEntity.getLastDamageCause();
        return entityDamageEvent instanceof EntityDamageByEntityEvent && ((EntityDamageByEntityEvent) entityDamageEvent).getDamager() instanceof Zombie;
    }

}
