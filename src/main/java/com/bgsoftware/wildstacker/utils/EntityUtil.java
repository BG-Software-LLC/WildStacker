package com.bgsoftware.wildstacker.utils;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.key.Key;
import com.bgsoftware.wildstacker.utils.reflection.Fields;
import com.bgsoftware.wildstacker.utils.reflection.Methods;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Parrot;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.regex.Pattern;

@SuppressWarnings("UnusedReturnValue")
public final class EntityUtil {

    private static WildStackerPlugin plugin = WildStackerPlugin.getPlugin();

    public static boolean areEquals(StackedEntity en1, StackedEntity en2){
        if(en1.getType() != en2.getType())
            return false;

        //EpicSpawners drops data
        if(en1.getLivingEntity().hasMetadata("ES") != en2.getLivingEntity().hasMetadata("ES"))
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
        Object entityLiving = Methods.ENTITY_GET_HANDLE.invoke(livingEntity);
        Object entityHuman = Methods.ENTITY_GET_HANDLE.invoke(killer);
        Fields.ENTITY_KILLER.set(entityLiving, entityHuman);
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

}
