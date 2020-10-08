package com.bgsoftware.wildstacker.utils.data.structures;

import org.bukkit.configuration.ConfigurationSection;

public final class Fast2EnumsMap<E extends Enum<E>, T extends Enum<T>, V> {

    private final FastEnumMap<E, V> firstKeyArray;
    private final FastEnumMap<T, V> secondKeyArray;
    private final V[] combinedKeyArray;
    private final int secondKeyLength;

    private V globalValue;
    private int size = 0;

    public Fast2EnumsMap(Class<E> firstKeyType, Class<T> secondKeyType){
        int firstKeyTypeLength = FastEnumUtils.getEnumValues(firstKeyType).length;
        this.secondKeyLength = FastEnumUtils.getEnumValues(secondKeyType).length;

        this.firstKeyArray = new FastEnumMap<>(firstKeyTypeLength);
        this.secondKeyArray =  new FastEnumMap<>(secondKeyLength);

        //noinspection unchecked
        this.combinedKeyArray = (V[]) new Object[firstKeyTypeLength * secondKeyLength];
    }

    public static <E extends Enum<E>, T extends Enum<T>> Fast2EnumsMap<E, T, Integer> fromSectionToInt(
            ConfigurationSection section, Class<E> firstType, Class<T> secondType){
        Fast2EnumsMap<E, T, Integer> fast2EnumsIntMap = new Fast2EnumsMap<>(firstType, secondType);
        if(section != null) {
            for (String key : section.getKeys(false))
                fast2EnumsIntMap.put(key, section.getInt(key), firstType, secondType);
        }
        return fast2EnumsIntMap;
    }

    private void put(String raw, V value, Class<E> firstType, Class<T> secondType){
        if(raw.equalsIgnoreCase("ALL")){
            globalValue = value;
            size++;
        }
        else {
            String[] sections = raw.split(":");
            E first = FastEnumUtils.getEnum(firstType, sections[0]);

            if (sections.length == 2) {
                T second = FastEnumUtils.getEnum(secondType, sections[1]);
                if (first != null && second != null)
                    put(first, second, value);
                else
                    size++;
            } else if (first != null) {
                putFirst(first, value);
            } else {
                T second = FastEnumUtils.getEnum(secondType, sections[0]);
                if (second != null)
                    putSecond(second, value);
                else
                    size++;
            }
        }
    }

    public V putFirst(E e, V value) {
        V originalValue = firstKeyArray.put(e, value);
        if(originalValue == null)
            size++;
        return originalValue;
    }

    public V putSecond(T t, V value) {
        V originalValue = secondKeyArray.put(t, value);
        if(originalValue == null)
            size++;
        return originalValue;
    }

    public V put(E e, T t, V value) {
        int index = (secondKeyLength * t.ordinal()) + e.ordinal();
        V originalValue = combinedKeyArray[index];
        combinedKeyArray[index] = value;

        if(originalValue == null)
            size++;

        return originalValue;
    }

    public V getOrDefault(E e, T t, V def){
        V value = get(e, t);
        return value == null ? def : value;
    }

    public V get(E e, T t){
        V value = firstKeyArray.get(e);

        if(value == null) {
            value = secondKeyArray.get(t);
            if(value == null) {
                value = combinedKeyArray[(secondKeyLength * t.ordinal()) + e.ordinal()];
                if(value == null){
                    value = globalValue;
                }
            }
        }

        return value;
    }

    public int size() {
        return size;
    }

}
