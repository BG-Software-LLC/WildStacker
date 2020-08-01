package com.bgsoftware.wildstacker.key;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public final class Key {

    private static final Key ALL_KEY = new Key("all");

    private final String globalKey;
    private final String subKey;

    private Key(String key){
        String[] keySections = key.split(":");
        this.globalKey = keySections[0];
        this.subKey = keySections.length == 2 ? keySections[1] : "";
    }

    public String getGlobalKey() {
        return globalKey;
    }

    public String getSubKey() {
        return subKey;
    }

    @Override
    public String toString() {
        return globalKey + ":" + subKey;
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
        return of(material + ":" + data);
    }

    public static Key of(String key){
        return key.equalsIgnoreCase("all") ? ALL_KEY : new Key(key.replace(";", ":"));
    }

}
