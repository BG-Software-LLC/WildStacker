package xyz.wildseries.wildstacker.utils;

import org.bukkit.inventory.ItemStack;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ItemStackList extends AbstractList<ItemStack> {

    private Map<ItemStack, Integer> map = new HashMap<>();

    public ItemStackList(List<ItemStack> list){
        ItemStack itemStack;
        int amount;
        for(ItemStack _itemStack : list){
            itemStack = _itemStack.clone();
            amount = _itemStack.getAmount();
            itemStack.setAmount(1);
            map.put(itemStack, amount);
        }
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
        if(map.containsKey(itemStack))
            amount += map.get(itemStack);

        map.put(itemStack, amount);

        return true;
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
