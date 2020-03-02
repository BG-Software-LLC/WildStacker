package com.bgsoftware.wildstacker.utils;

import java.util.Map;

public final class Pair<K, V> implements Map.Entry<K, V> {

    private K key;
    private V value;

    public Pair(K key, V value){
        this.key = key;
        this.value = value;
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }


    @Override
    public V setValue(V value) {
        throw new UnsupportedOperationException("Cannot use the setValue method of Pair");
    }

}
