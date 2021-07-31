package com.bgsoftware.wildstacker.utils.data.structures;

public final class FastEnumUtils {

    private FastEnumUtils() {

    }

    public static <T extends Enum<T>> T getEnum(Class<T> enumType, String name) {
        try {
            return Enum.valueOf(enumType, name);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    public static Enum[] getEnumValues(Class<?> clazz) {
        return (Enum[]) clazz.getEnumConstants();
    }

}
