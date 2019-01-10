package xyz.wildseries.wildstacker.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.LivingEntity;
import xyz.wildseries.wildstacker.WildStackerPlugin;
import xyz.wildseries.wildstacker.key.Key;
import xyz.wildseries.wildstacker.objects.WStackedEntity;

import java.util.List;
import java.util.regex.Pattern;

import static xyz.wildseries.wildstacker.utils.ReflectionUtil.getField;
import static xyz.wildseries.wildstacker.utils.ReflectionUtil.getNMSClass;

@SuppressWarnings("UnusedReturnValue")
public final class EntityUtil {

    private static WildStackerPlugin plugin = WildStackerPlugin.getPlugin();

    public static boolean areEquals(LivingEntity en1, LivingEntity en2){
        if(en1.getType() != en2.getType())
            return false;

        //EpicSpawners drops data
        if(en1.hasMetadata("ES") != en2.hasMetadata("ES"))
            return false;

        if(isNerfed(en1) != isNerfed(en2))
            return false;

        return EntityData.of(en1).equals(EntityData.of(en2));
    }

    public static void nerfEntity(LivingEntity livingEntity){
        if(plugin.getSettings().entitiesDisabledWorlds.contains(livingEntity.getWorld().getName()))
            return;

        try {
            Class entityInsentientClass = getNMSClass("EntityInsentient");
            Object nmsEntity = entityInsentientClass.cast(livingEntity.getClass().getMethod("getHandle").invoke(livingEntity));

            Class pathfinderGoalClass = getNMSClass("PathfinderGoalSelector");

            Object goalSelector = nmsEntity.getClass().getField("goalSelector").get(nmsEntity);
            Object targetSelector = nmsEntity.getClass().getField("targetSelector").get(nmsEntity);

            Object[] goals = new Object[]{
                    getField("b", pathfinderGoalClass).get(goalSelector),
                    getField("c", pathfinderGoalClass).get(goalSelector),
                    getField("b", pathfinderGoalClass).get(targetSelector),
                    getField("c", pathfinderGoalClass).get(targetSelector)
            };

            for(Object goal : goals)
                goal.getClass().getMethod("clear").invoke(goal);

            Bukkit.getScheduler().runTaskLater(plugin, () -> ((WStackedEntity) WStackedEntity.of(livingEntity)).setNerfed(true), 1L);

        } catch(Exception ex){
            ex.printStackTrace();
        }
    }

    public static boolean isNerfed(LivingEntity livingEntity){
        WStackedEntity stackedEntity = (WStackedEntity) WStackedEntity.of(livingEntity);

        try {
            Class entityInsentientClass = getNMSClass("EntityInsentient");

            Object nmsEntity = entityInsentientClass.cast(livingEntity.getClass().getMethod("getHandle").invoke(livingEntity));

            Class pathfinderGoalClass = getNMSClass("PathfinderGoalSelector");

            Object goalSelector = nmsEntity.getClass().getField("goalSelector").get(nmsEntity);
            Object targetSelector = nmsEntity.getClass().getField("targetSelector").get(nmsEntity);

            Object[] goals = new Object[]{
                    getField("b", pathfinderGoalClass).get(goalSelector),
                    getField("c", pathfinderGoalClass).get(goalSelector),
                    getField("b", pathfinderGoalClass).get(targetSelector),
                    getField("c", pathfinderGoalClass).get(targetSelector)
            };

            for(Object goal : goals) {
                if (!(boolean) goal.getClass().getMethod("isEmpty").invoke(goal)) {
                    stackedEntity.setNerfed(false);
                    return false;
                }
            }

            stackedEntity.setNerfed(true);

            return true;
        } catch(Exception ex){
            ex.printStackTrace();
            stackedEntity.setNerfed(false);
            return false;
        }
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

}
