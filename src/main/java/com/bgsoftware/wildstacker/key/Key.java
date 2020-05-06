package com.bgsoftware.wildstacker.key;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public final class Key {

    private final String key;

    private Key(String key){
        this.key = key;
    }

    @Override
    public String toString() {
        return key;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Key key1 = (Key) o;
        return key.equals(key1.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }

    public static Key of(EntityType entityType){
        return of(entityType.name());
    }

    public static Key of(ItemStack itemStack){
        return of(itemStack.getType(), itemStack.getDurability());
    }

    public static Key of(Material material, short data){
        return of(material + ":" + data);
    }

    public static Key of(String key){
        return new Key(key);
    }

}
