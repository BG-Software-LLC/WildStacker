package com.bgsoftware.wildstacker.key;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.AbstractSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class KeySet extends AbstractSet<Key> implements Set<Key> {

    private final Set<String> set = new HashSet<>();
    private final Set<Key> keySet = new HashSet<>();

    public KeySet(List<String> keys){
        this.set.addAll(keys);
        keys.forEach(key -> keySet.add(Key.of(key)));
    }

    @Override
    @SuppressWarnings("NullableProblems")
    public Iterator<Key> iterator() {
        return asKeySet().iterator();
    }

    @Override
    public int size() {
        return set.size();
    }

    public boolean contains(ItemStack itemStack) {
        return contains(Key.of(itemStack));
    }

    public boolean contains(Material material, short data) {
        return contains(Key.of(material, data));
    }

    public boolean contains(String key) {
        return contains(Key.of(key));
    }

    @Override
    public boolean contains(Object o) {
        if(o instanceof Key){
            String key = o.toString();
            if(set.contains(key))
                return true;
            else if(key.contains(":") && set.contains(key.split(":")[0]))
                return true;
            else if(key.contains(";") && set.contains(key.split(";")[0]))
                return true;
            else if(set.contains("all") || set.contains("ALL"))
                return true;
        }
        return super.contains(o);
    }

    @Override
    public boolean add(Key key) {
        keySet.add(key);
        return set.add(key.toString());
    }

    @Override
    public boolean remove(Object o) {
        keySet.remove(o);
        return set.remove(o);
    }

    public Set<String> asStringSet(){
        return new HashSet<>(set);
    }

    private Set<Key> asKeySet(){
        return new HashSet<>(keySet);
    }

}
