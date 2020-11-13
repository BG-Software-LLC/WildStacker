package com.bgsoftware.wildstacker.utils.reflection;

import com.bgsoftware.wildstacker.utils.ServerVersion;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public final class ReflectionUtils {

    static Map<Fields, Field> fieldMap = new HashMap<>();
    static Map<Methods, Method> methodMap = new HashMap<>();

    @SuppressWarnings("unchecked")
    public static boolean init(){
        try{
            Class entityLivingClass = getNMSClass("EntityLiving"),
                    entityClass = getNMSClass("Entity"),
                    entityInsentientClass = getNMSClass("EntityInsentient"),
                    worldClass = getNMSClass("World"),
                    chunkClass = getNMSClass("Chunk"),
                    tileEntitySpawnerClass = getNMSClass("TileEntityMobSpawner"),
                    mobSpawnerAbstractClass = getNMSClass("MobSpawnerAbstract");

            fieldMap.put(Fields.ENTITY_LAST_DAMAGE_BY_PLAYER_TIME, entityLivingClass.getDeclaredField("lastDamageByPlayerTime"));
            fieldMap.put(Fields.ENTITY_EXP, entityInsentientClass.getDeclaredField(ServerVersion.isAtLeast(ServerVersion.v1_14) ? "f" :
                    ServerVersion.isEquals(ServerVersion.v1_7) ? "b" : "b_"));

            Field spawnerAbstractField = getFinalField(tileEntitySpawnerClass, "a");
            if(spawnerAbstractField != null)
                fieldMap.put(Fields.TILE_ENTITY_SPAWNER_ABSTRACT_SPAWNER, spawnerAbstractField);

            try{ fieldMap.put(Fields.ENTITY_SPAWNED_VIA_MOB_SPAWNER, entityClass.getField("spawnedViaMobSpawner")); }catch(Throwable ignored){}
            try{ fieldMap.put(Fields.ENTITY_FROM_MOB_SPAWNER, entityClass.getField("fromMobSpawner")); }catch(Throwable ignored){}

            try{
                Class<?> entityStriderClass = getNMSClass("EntityStrider");
                fieldMap.put(Fields.STRIDER_SADDLE_STORAGE, entityStriderClass.getDeclaredField("bA"));
            }catch (Throwable ignored){}

            if(ServerVersion.isEquals(ServerVersion.v1_13))
                fieldMap.put(Fields.ABSTRACT_SPAWNER_MOBS, mobSpawnerAbstractClass.getDeclaredField("mobs"));

            if(ServerVersion.isAtLeast(ServerVersion.v1_16))
                fieldMap.put(Fields.CHUNK_ENTITY_SLICES, chunkClass.getField("entitySlices"));

            methodMap.put(Methods.ENTITY_SOUND_DEATH, entityLivingClass.getDeclaredMethod(
                    ServerVersion.isAtLeast(ServerVersion.v1_14) ? "getSoundDeath" :
                    ServerVersion.isEquals(ServerVersion.v1_13) ? "cs" :
                    ServerVersion.isEquals(ServerVersion.v1_12) ? "cf" :
                    ServerVersion.isEquals(ServerVersion.v1_11) ? "bX" :
                    ServerVersion.isEquals(ServerVersion.v1_10) ? "bW" :
                    ServerVersion.isEquals(ServerVersion.v1_9) ?
                        ServerVersion.getBukkitVersion().contains("R2") ? "bT" : "bS" :
                    ServerVersion.isEquals(ServerVersion.v1_8) ?
                        ServerVersion.getBukkitVersion().contains("R1") ? "bo" : "bp" :
                    //Version 1.7
                    ServerVersion.getBukkitVersion().contains("R3") ? "aT" : "aU"
            ));
            methodMap.put(Methods.ENTITY_SOUND_VOLUME, entityLivingClass.getDeclaredMethod(
                    ServerVersion.isAtLeast(ServerVersion.v1_14) ? getValidMethod(entityLivingClass,"getSoundVolume", "cU") :
                    ServerVersion.isEquals(ServerVersion.v1_13) ? "cD" :
                    ServerVersion.isEquals(ServerVersion.v1_12) ? "cq" :
                    ServerVersion.isEquals(ServerVersion.v1_11) ? "ci" :
                    ServerVersion.isEquals(ServerVersion.v1_10) ? "ch" :
                    ServerVersion.isEquals(ServerVersion.v1_9) ?
                        ServerVersion.getBukkitVersion().contains("R2") ? "ce" : "cd" :
                    ServerVersion.isEquals(ServerVersion.v1_8) ?
                        ServerVersion.getBukkitVersion().contains("R1") ? "bA" : "bB" :
                    //1.7 Version
                    ServerVersion.getBukkitVersion().contains("R3") ? "be" : "bf"
            ));
            methodMap.put(Methods.ENTITY_SOUND_PITCH, entityLivingClass.getDeclaredMethod(
                    ServerVersion.isEquals(ServerVersion.v1_16) ?
                            ServerVersion.getBukkitVersion().contains("R3") ? "dH" : "dG" :
                    ServerVersion.isEquals(ServerVersion.v1_15) ? "dn" :
                    ServerVersion.isEquals(ServerVersion.v1_14) ? "cV" :
                    ServerVersion.isEquals(ServerVersion.v1_13) ? "cE" :
                    ServerVersion.isEquals(ServerVersion.v1_12) ? "cr" :
                    ServerVersion.isEquals(ServerVersion.v1_11) ? "cj" :
                    ServerVersion.isEquals(ServerVersion.v1_10) ? "ci" :
                    ServerVersion.isEquals(ServerVersion.v1_9) ?
                        ServerVersion.getBukkitVersion().contains("R2") ? "cf" : "ce" :
                    ServerVersion.isEquals(ServerVersion.v1_8) ?
                        ServerVersion.getBukkitVersion().contains("R1") ? "bB" : "bC" :
                    //1.7 Version
                            ServerVersion.getBukkitVersion().contains("R3") ? "bf" : "bg"
            ));
            methodMap.put(Methods.ENTITY_ALWAYS_GIVES_EXP, entityLivingClass.getDeclaredMethod("alwaysGivesExp"));
            methodMap.put(Methods.ENTITY_IS_DROP_EXPERIENCE, entityLivingClass.getDeclaredMethod(
                    ServerVersion.isEquals(ServerVersion.v1_7) ?
                        ServerVersion.getBukkitVersion().contains("R3") ? "aF" : "aG" :
                    ServerVersion.isEquals(ServerVersion.v1_8) ?
                        ServerVersion.getBukkitVersion().contains("R1") ? "aZ" : "ba" :
                    //1.9 and above
                    "isDropExperience"
            ));

            try{
                methodMap.put(Methods.WORLD_GET_CHUNK_IF_LOADED_PAPER, worldClass.getMethod("getChunkIfLoaded",int.class, int.class));
            }catch (Throwable ignored){}

            fieldMap.values().forEach(field -> field.setAccessible(true));
            methodMap.values().forEach(method -> method.setAccessible(true));

            return true;
        }catch(Exception ex){
            ex.printStackTrace();
            return false;
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

    @SuppressWarnings("SameParameterValue")
    private static String getValidMethod(Class clazz, String methodName1, String methodName2){
        try{
            //noinspection unchecked
            clazz.getDeclaredMethod(methodName1);
            return methodName1;
        }catch(Throwable ex){
            return methodName2;
        }
    }

    @SuppressWarnings("SameParameterValue")
    private static Field getFinalField(Class<?> clazz, String fieldName) throws Exception{
        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);

        try {
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        }catch (Throwable ex){
            field = null;
        }

        return field;
    }

}
