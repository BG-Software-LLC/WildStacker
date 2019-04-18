package com.bgsoftware.wildstacker.utils;

import org.bukkit.inventory.ItemStack;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ItemStackList extends AbstractList<ItemStack> {

    private Map<ItemStack, Integer> map = new HashMap<>();

    public ItemStackList(){ }

    public ItemStackList(List<ItemStack> drops){
        addAll(drops);
    }

    @Override
    public ItemStack get(int index) {
        throw new UnsupportedOperationException("You cannot use get(index) from ItemStackList.");
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean add(ItemStack itemStack) {
        itemStack = itemStack.clone();
        int amount = itemStack.getAmount();
        itemStack.setAmount(1);
        map.put(itemStack, map.getOrDefault(itemStack, 0) + amount);

        return true;
    }

    @Override
    public boolean addAll(Collection<? extends ItemStack> collection) {
        boolean bool = true;

        for(ItemStack itemStack : collection)
            bool &= add(itemStack);

        return bool;
    }

    public List<ItemStack> toList(){
        List<ItemStack> list = new ArrayList<>();

        ItemStack itemStack;

        for(ItemStack _itemStack : map.keySet()){
            itemStack = _itemStack.clone();
            itemStack.setAmount(map.get(_itemStack));
            list.add(itemStack);
        }

        return list;
    }

}
