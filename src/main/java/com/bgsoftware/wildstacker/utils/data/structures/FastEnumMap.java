package com.bgsoftware.wildstacker.utils.data.structures;

import org.bukkit.configuration.ConfigurationSection;

public final class FastEnumMap<E extends Enum<E>, V> {

    private final V[] arr;

    private V globalValue = null;
    private int size = 0;

    public FastEnumMap(Class<E> keyType){
        this(FastEnumUtils.getEnumValues(keyType).length);
    }

    FastEnumMap(int initSize){
        //noinspection unchecked
        arr = (V[]) new Object[initSize];
    }

    public static <E extends Enum<E>> FastEnumMap<E, Integer> fromSection(ConfigurationSection section, Class<E> keyType){
        FastEnumMap<E, Integer> fastEnumIntMap = new FastEnumMap<>(keyType);
        for(String _key : section.getKeys(false)){
            if(_key.equalsIgnoreCase("ALL")) {
                fastEnumIntMap.globalValue = section.getInt(_key);
                fastEnumIntMap.size++;
            }
            else {
                E key = FastEnumUtils.getEnum(keyType, _key.toUpperCase());
                if (key != null)
                    fastEnumIntMap.put(key, section.getInt(_key));
                else
                    fastEnumIntMap.size++;
            }
        }
        return fastEnumIntMap;
    }

    public V put(E e, V value){
        V originalValue = arr[e.ordinal()];
        arr[e.ordinal()] = value;

        if(originalValue == null)
            size++;

        return originalValue;
    }

    public V getOrDefault(E e, V def){
        V value = get(e);
        return value == null ? def : value;
    }

    public V get(E e){
        V value = arr[e.ordinal()];
        return value == null ? globalValue : value;
    }

    public int size() {
        return size;
    }

}
