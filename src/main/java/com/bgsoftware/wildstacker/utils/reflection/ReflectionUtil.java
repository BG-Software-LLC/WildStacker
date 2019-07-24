package com.bgsoftware.wildstacker.utils.reflection;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public final class ReflectionUtil {

    private static String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];

    static Map<Fields, Field> fieldMap = new HashMap<>();
    static Map<Methods, Method> methodMap = new HashMap<>();

    @SuppressWarnings("unchecked")
    public static void init(){
        try{
            Class entityLivingClass = getNMSClass("EntityLiving"),
                    pathfinderClass = getNMSClass("PathfinderGoalBreed"),
                    craftWorldClass = getBukkitClass("CraftWorld"),
                    entityClass = getNMSClass("Entity"),
                    craftEntityClass = getBukkitClass("entity.CraftEntity"),
                    nmsTagClass = getNMSClass("NBTTagCompound"),
                    entityInsentientClass = getNMSClass("EntityInsentient");

            fieldMap.put(Fields.ENTITY_LAST_DAMAGE_BY_PLAYER_TIME, entityLivingClass.getDeclaredField("lastDamageByPlayerTime"));
            fieldMap.put(Fields.ENTITY_EXP, entityInsentientClass.getDeclaredField(version.contains("1_14") ? "f" : "b_"));
            fieldMap.put(Fields.PATHFINDER_GOAL_BREED_B, pathfinderClass.getDeclaredField("b"));
            fieldMap.put(Fields.PATHFINDER_GOAL_BREED_ANIMAL, pathfinderClass.getDeclaredField("animal"));
            fieldMap.put(Fields.PATHFINDER_GOAL_BREED_PARTNER, pathfinderClass.getDeclaredField("partner"));
            fieldMap.put(Fields.ENTITY_KILLER, entityLivingClass.getDeclaredField("killer"));
            fieldMap.put(Fields.NBT_TAG_MAP, nmsTagClass.getDeclaredField("map"));

            methodMap.put(Methods.WORLD_CREATE_ENTITY, craftWorldClass.getMethod("createEntity", Location.class, Class.class));
            methodMap.put(Methods.ENTITY_GET_BUKKIT_ENTITY, entityClass.getMethod("getBukkitEntity"));
            methodMap.put(Methods.WORLD_ADD_ENTITY, craftWorldClass.getMethod("addEntity", entityClass, CreatureSpawnEvent.SpawnReason.class));
            methodMap.put(Methods.ENTITY_GET_HANDLE, craftEntityClass.getMethod("getHandle"));

            try{
                methodMap.put(Methods.BLOCK_GET_BY_COMBINED_ID, getNMSClass("Block").getMethod("getByCombinedId", int.class));
                try {
                    methodMap.put(Methods.MAGIC_GET_BLOCK, getBukkitClass("util.CraftMagicNumbers").getMethod("getBlock", Material.class, byte.class));
                }catch (Throwable ignored) {
                    methodMap.put(Methods.BLOCK_DATA_FROM_DATA, getBukkitClass("block.data.CraftBlockData").getMethod("fromData", getNMSClass("IBlockData")));
                }
            }catch(Throwable ignored){}

            fieldMap.values().forEach(field -> field.setAccessible(true));
            methodMap.values().forEach(method -> method.setAccessible(true));
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

    public static boolean isPluginEnabled(String classPath){
        try{
            Class.forName(classPath);
            return true;
        }catch(ClassNotFoundException ex){
            return false;
        }
    }

    private static Class getNMSClass(String className){
        try{
            return Class.forName("net.minecraft.server." + version + "." + className);
        }catch(ClassNotFoundException ex){
            throw new NullPointerException(ex.getMessage());
        }
    }

    private static Class getBukkitClass(String className){
        try{
            return Class.forName("org.bukkit.craftbukkit." + version + "." + className);
        }catch(ClassNotFoundException ex){
            throw new NullPointerException(ex.getMessage());
        }
    }

}
