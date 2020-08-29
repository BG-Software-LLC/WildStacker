package com.bgsoftware.wildstacker.key;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public final class Key {

    private static final Key ALL_KEY = new Key(new String[] { "all" });

    private final String globalKey;
    private final String subKey;

    private Key(String[] keys){
        this.globalKey = keys[0];
        this.subKey = keys.length == 2 ? keys[1] : "";
    }

    public String getGlobalKey() {
        return globalKey;
    }

    public String getSubKey() {
        return subKey;
    }

    @Override
    public String toString() {
        return globalKey + (subKey.isEmpty() ? "" : ":" + subKey);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Key key1 = (Key) o;
        return toString().equals(key1.toString());
    }

    @Override
    public int hashCode() {
        return Objects.hash(globalKey, subKey);
    }

    public static Key of(EntityType entityType){
        return of(entityType.name());
    }

    public static Key of(ItemStack itemStack){
        return of(itemStack.getType(), itemStack.getDurability());
    }

    public static Key of(Material material, short data){
        return of(new String[] {material + "", data + ""});
    }

    public static Key of(String key){
        return key.contains(":") ? new Key(key.split(":")) : new Key(new String[]{ key });
    }

    public static Key of(String[] keys){
        return keys[0].equalsIgnoreCase("all") ? ALL_KEY : new Key(keys);
    }

}
