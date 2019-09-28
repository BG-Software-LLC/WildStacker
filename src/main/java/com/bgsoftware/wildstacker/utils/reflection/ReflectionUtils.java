package com.bgsoftware.wildstacker.utils.reflection;

import com.bgsoftware.wildstacker.utils.ServerVersion;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public final class ReflectionUtils {

    static Map<Fields, Field> fieldMap = new HashMap<>();
    static Map<Methods, Method> methodMap = new HashMap<>();

    @SuppressWarnings("unchecked")
    public static void init(){
        try{
            Class entityLivingClass = getNMSClass("EntityLiving"),
                    craftWorldClass = getBukkitClass("CraftWorld"),
                    entityClass = getNMSClass("Entity"),
                    entityItemClass = getNMSClass("EntityItem"),
                    craftEntityClass = getBukkitClass("entity.CraftEntity"),
                    nmsTagClass = getNMSClass("NBTTagCompound"),
                    entityInsentientClass = getNMSClass("EntityInsentient");

            fieldMap.put(Fields.ENTITY_LAST_DAMAGE_BY_PLAYER_TIME, entityLivingClass.getDeclaredField("lastDamageByPlayerTime"));
            fieldMap.put(Fields.ENTITY_EXP, entityInsentientClass.getDeclaredField(ServerVersion.isEquals(ServerVersion.v1_14) ? "f" : "b_"));
            fieldMap.put(Fields.ENTITY_KILLER, entityLivingClass.getDeclaredField("killer"));
            fieldMap.put(Fields.ENTITY_DEAD, entityClass.getDeclaredField("dead"));
            fieldMap.put(Fields.ITEM_PICKUP_DELAY, entityItemClass.getDeclaredField("pickupDelay"));
            fieldMap.put(Fields.NBT_TAG_MAP, nmsTagClass.getDeclaredField("map"));

            methodMap.put(Methods.WORLD_CREATE_ENTITY, craftWorldClass.getDeclaredMethod("createEntity", Location.class, Class.class));
            methodMap.put(Methods.ENTITY_GET_BUKKIT_ENTITY, entityClass.getDeclaredMethod("getBukkitEntity"));
            methodMap.put(Methods.WORLD_ADD_ENTITY, craftWorldClass.getDeclaredMethod("addEntity", entityClass, CreatureSpawnEvent.SpawnReason.class));
            methodMap.put(Methods.ENTITY_GET_HANDLE, craftEntityClass.getDeclaredMethod("getHandle"));
            methodMap.put(Methods.ENTITY_SOUND_DEATH, entityLivingClass.getDeclaredMethod(
                    ServerVersion.isEquals(ServerVersion.v1_14) ? "getSoundDeath" :
                    ServerVersion.isEquals(ServerVersion.v1_13) ? "cs" :
                    ServerVersion.isEquals(ServerVersion.v1_12) ? "cf" :
                    ServerVersion.isEquals(ServerVersion.v1_11) ? "bX" :
                    ServerVersion.isEquals(ServerVersion.v1_10) ? "bW" :
                    ServerVersion.isEquals(ServerVersion.v1_9) ?
                            ServerVersion.getBukkitVersion().contains("R2") ? "bT" : "bS" :
                            //1.8 Version
                            ServerVersion.getBukkitVersion().contains("R1") ? "bo" : "bp"
            ));
            methodMap.put(Methods.ENTITY_SOUND_VOLUME, entityLivingClass.getDeclaredMethod(
                    ServerVersion.isEquals(ServerVersion.v1_14) ? "getSoundVolume" :
                    ServerVersion.isEquals(ServerVersion.v1_13) ? "cD" :
                    ServerVersion.isEquals(ServerVersion.v1_12) ? "cq" :
                    ServerVersion.isEquals(ServerVersion.v1_11) ? "ci" :
                    ServerVersion.isEquals(ServerVersion.v1_10) ? "ch" :
                    ServerVersion.isEquals(ServerVersion.v1_9) ?
                            ServerVersion.getBukkitVersion().contains("R2") ? "ce" : "cd" :
                            //1.8 Version
                            ServerVersion.getBukkitVersion().contains("R1") ? "bA" : "bB"
            ));
            methodMap.put(Methods.ENTITY_SOUND_PITCH, entityLivingClass.getDeclaredMethod(
                    ServerVersion.isEquals(ServerVersion.v1_14) ? "cV" :
                    ServerVersion.isEquals(ServerVersion.v1_13) ? "cE" :
                    ServerVersion.isEquals(ServerVersion.v1_12) ? "cr" :
                    ServerVersion.isEquals(ServerVersion.v1_11) ? "cj" :
                    ServerVersion.isEquals(ServerVersion.v1_10) ? "ci" :
                    ServerVersion.isEquals(ServerVersion.v1_9) ?
                            ServerVersion.getBukkitVersion().contains("R2") ? "cf" : "ce" :
                            //1.8 Version
                            ServerVersion.getBukkitVersion().contains("R1") ? "bB" : "bC"
            ));
            methodMap.put(Methods.ENTITY_ALWAYS_GIVES_EXP, entityLivingClass.getDeclaredMethod("alwaysGivesExp"));
            methodMap.put(Methods.ENTITY_IS_DROP_EXPERIENCE, entityLivingClass.getDeclaredMethod(
                    ServerVersion.isEquals(ServerVersion.v1_8) ? ServerVersion.getBukkitVersion().contains("R1") ? "aZ" : "ba" : "isDropExperience"
            ));

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
            return Class.forName("net.minecraft.server." + ServerVersion.getBukkitVersion() + "." + className);
        }catch(ClassNotFoundException ex){
            throw new NullPointerException(ex.getMessage());
        }
    }

    private static Class getBukkitClass(String className){
        try{
            return Class.forName("org.bukkit.craftbukkit." + ServerVersion.getBukkitVersion() + "." + className);
        }catch(ClassNotFoundException ex){
            throw new NullPointerException(ex.getMessage());
        }
    }

}
