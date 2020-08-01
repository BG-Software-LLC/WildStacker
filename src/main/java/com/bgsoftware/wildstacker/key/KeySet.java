package com.bgsoftware.wildstacker.key;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.AbstractSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class KeySet extends AbstractSet<Key> implements Set<Key> {

    private final Set<String> set = new HashSet<>();

    public KeySet(List<String> keys){
        this.set.addAll(keys);
    }

    @Override
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
        return o instanceof Key && (set.contains(o.toString()) || (!((Key) o).getSubKey().isEmpty() && set.contains(((Key) o).getGlobalKey())));
    }

    @Override
    public boolean add(Key key) {
        return set.add(key.toString());
    }

    @Override
    public boolean remove(Object o) {
        return set.remove(o);
    }

    public Set<String> asStringSet(){
        return new HashSet<>(set);
    }

    private Set<Key> asKeySet(){
        return set.stream().map(Key::of).collect(Collectors.toSet());
    }

}
