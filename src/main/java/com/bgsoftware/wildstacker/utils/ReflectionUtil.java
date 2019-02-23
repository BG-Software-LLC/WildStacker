package com.bgsoftware.wildstacker.utils;

import org.bukkit.Bukkit;

import java.lang.reflect.Field;

public final class ReflectionUtil {

    private static String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];

    public static boolean isPluginEnabled(String classPath){
        try{
            Class.forName(classPath);
            return true;
        }catch(ClassNotFoundException ex){
            return false;
        }
    }

    public static Field getField(String fieldName, Class clazz){
        try{
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field;
        }catch(Exception ex){
            throw new NullPointerException(ex.getMessage());
        }
    }

    public static Class getNMSClass(String className){
        try{
            return Class.forName("net.minecraft.server." + version + "." + className);
        }catch(ClassNotFoundException ex){
            throw new NullPointerException(ex.getMessage());
        }
    }

    public static Class getBukkitClass(String className){
        try{
            return Class.forName("org.bukkit.craftbukkit." + version + "." + className);
        }catch(ClassNotFoundException ex){
            throw new NullPointerException(ex.getMessage());
        }
    }

}
