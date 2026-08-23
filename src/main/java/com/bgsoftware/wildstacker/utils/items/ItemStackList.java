package com.bgsoftware.wildstacker.utils.items;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public final class ItemStackList {

    private final Map<ItemStack, Counter> map = new HashMap<>();

    public ItemStackList() {
    }

    public int size() {
        return map.size();
    }

    public boolean add(ItemStack itemStack) {
        if (itemStack == null)
            return false;

        int amount = itemStack.getAmount();

        //If the item is AIR, we don't add it but we're considering it as a "successful" operation.
        if (itemStack.getType() == Material.AIR || amount <= 0)
            return true;

        itemStack = itemStack.clone();
        itemStack.setAmount(1);
        Counter counter = map.computeIfAbsent(itemStack, i -> new Counter());
        counter.value += amount;

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

        this.map.forEach((itemTemplate, counter) -> {
            ItemStack itemStack = itemTemplate.clone();
            itemStack.setAmount(counter.value);
            list.add(itemStack);
        });

        return list;
    }

    private static class Counter {

        private int value = 0;

    }

}
