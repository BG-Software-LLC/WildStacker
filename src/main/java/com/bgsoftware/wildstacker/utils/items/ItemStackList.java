package com.bgsoftware.wildstacker.utils.items;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public final class ItemStackList {

    private final Map<ItemStack, Integer> map = new HashMap<>();

    public ItemStackList() {
    }

    public int size() {
        return map.size();
    }

    public boolean add(ItemStack itemStack) {
        if (itemStack == null)
            return false;

        //If the item is AIR, we don't add it but we're considering it as a "successful" operation.
        if (itemStack.getType() == Material.AIR)
            return true;

        itemStack = itemStack.clone();
        int amount = itemStack.getAmount();
        itemStack.setAmount(1);
        map.put(itemStack, map.getOrDefault(itemStack, 0) + amount);

        return true;
    }

    public boolean addAll(Collection<? extends ItemStack> collection) {
        boolean bool = true;

        for (ItemStack itemStack : collection)
            bool &= add(itemStack);

        return bool;
    }

    public List<ItemStack> toList() {
        List<ItemStack> list = new LinkedList<>();

        ItemStack itemStack;

        for (ItemStack _itemStack : map.keySet()) {
            itemStack = _itemStack.clone();
            itemStack.setAmount(map.get(_itemStack));
            list.add(itemStack);
        }

        return list;
    }

}
