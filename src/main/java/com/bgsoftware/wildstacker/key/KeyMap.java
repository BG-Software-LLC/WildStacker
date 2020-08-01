package com.bgsoftware.wildstacker.key;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class KeyMap<V> extends AbstractMap<Key, V> implements Map<Key, V> {

    private final Map<String, V> map;

    public KeyMap(){
        this.map = new HashMap<>();
    }

    @Override
    public Set<Entry<Key, V>> entrySet() {
        return asKeyMap().entrySet();
    }

    @Override
    public int size() {
        return map.size();
    }

    public boolean containsKey(String key) {
        return containsKey(Key.of(key));
    }

    @Override
    public boolean containsKey(Object o) {
        return get(o) != null;
    }

    public V put(String key, V value) {
        return put(Key.of(key), value);
    }

    @Override
    public V put(Key key, V value) {
        return map.put(key.toString(), value);
    }

    @Override
    public V remove(Object key) {
        return map.remove(key);
    }

    public V get(ItemStack itemStack) {
        return get(Key.of(itemStack));
    }

    public V get(Material material, short data) {
        return get(Key.of(material, data));
    }

    public V get(String key) {
        return get(Key.of(key));
    }

    @Override
    public V get(Object o) {
        if(o instanceof Key){
            V returnValue = map.get(o.toString());
            return returnValue == null && !((Key) o).getSubKey().isEmpty() ? map.get(((Key) o).getGlobalKey()) : returnValue;
        }

        return null;
    }

    public V getOrDefault(ItemStack itemStack, V defaultValue) {
        return getOrDefault(Key.of(itemStack), defaultValue);
    }

    public V getOrDefault(String key, V defaultValue) {
        return getOrDefault(Key.of(key), defaultValue);
    }

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        return key instanceof Key ? containsKey(key) ? get(key) : defaultValue : super.getOrDefault(key, defaultValue);
    }

    @Override
    public void clear() {
        map.clear();
    }

    private Map<Key, V> asKeyMap(){
        Map<Key, V> map = new HashMap<>();
        this.map.forEach((key, value) -> map.put(Key.of(key), value));
        return map;
    }

}
