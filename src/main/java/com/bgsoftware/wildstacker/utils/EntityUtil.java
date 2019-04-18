package com.bgsoftware.wildstacker.utils;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.key.Key;
import org.bukkit.ChatColor;
import org.bukkit.entity.LivingEntity;

import java.lang.reflect.Method;
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

    public static int getEntityExp(LivingEntity livingEntity){
//        EntityInsentient entityInsentient = (EntityInsentient) ((CraftLivingEntity) livingEntity).getHandle();
//        try{
//            Bukkit.broadcastMessage(entityInsentient.killer + "");
//            Method method = EntityLiving.class.getDeclaredMethod("getExpValue", EntityHuman.class);
//            method.setAccessible(true);
//            int exp = (int) method.invoke(entityInsentient, entityInsentient.killer);
//            method.setAccessible(false);
//            return exp;
//        }catch(Exception ex){
//            ex.printStackTrace();
//            return 0;
//        }
        int exp = 0;

        try{
            Class entityInsentientClass = ReflectionUtil.getNMSClass("EntityInsentient");
            Class entityLivingClass = ReflectionUtil.getNMSClass("EntityLiving");
            Class entityHumanClass = ReflectionUtil.getNMSClass("EntityHuman");
            Object entityInsentient = entityInsentientClass.cast(livingEntity.getClass().getMethod("getHandle").invoke(livingEntity));
            //noinspection unchecked
            Method method = entityLivingClass.getDeclaredMethod("getExpValue", entityHumanClass);
            method.setAccessible(true);
            exp = (int) method.invoke(entityInsentient, entityInsentientClass.getField("killer").get(entityInsentient));
            method.setAccessible(false);
        }catch(Exception ex){
            ex.printStackTrace();
        }

        return exp;
    }

}
