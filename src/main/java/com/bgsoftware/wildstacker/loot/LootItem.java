package com.bgsoftware.wildstacker.loot;

import com.bgsoftware.wildstacker.api.loot.LootEntityAttributes;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class LootItem extends FilteredLoot {

    private final List<ItemModifiers.ItemModifierFunction> itemModifiers = new LinkedList<>();
    private final ItemStack itemStack, burnableItem;
    private final double chance;
    private final Looting looting;

    public LootItem(ItemStack itemStack, @Nullable ItemStack burnableItem, double chance, Looting looting,
                    List<ItemModifiers.ItemModifierFunction> itemModifiers,
                    List<Predicate<LootEntityAttributes>> entityFilters,
                    List<Predicate<LootEntityAttributes>> killerFilters,
                    List<Predicate<LootEntityAttributes>> vehicleFilters) {
        super(entityFilters, killerFilters, vehicleFilters);
        this.itemStack = itemStack;
        this.burnableItem = burnableItem;
        this.chance = chance;
        this.looting = looting;
        this.itemModifiers.addAll(itemModifiers);
    }

    public Optional<Looting> getLooting() {
        return Optional.ofNullable(this.looting);
    }

    public double getChance(int lootBonusLevel, double lootMultiplier) {
        return chance + (lootBonusLevel * lootMultiplier);
    }

    public ItemStack getItemStack(LootEntityAttributes lootEntityAttributes, int amountOfItems, int lootBonusLevel) {
        ItemStack itemStack = lootEntityAttributes.isBurning() && this.burnableItem != null ?
                this.burnableItem.clone() : this.itemStack.clone();

        if (!this.itemModifiers.isEmpty()) {
            ItemMeta itemMeta = itemStack.getItemMeta();

            for (ItemModifiers.ItemModifierFunction function : this.itemModifiers) {
                if (!function.apply(this, itemStack, itemMeta, amountOfItems, lootBonusLevel))
                    return null;
            }

            if (itemMeta != null)
                itemStack.setItemMeta(itemMeta);
        }

        return itemStack;
    }

    @Override
    public String toString() {
        return "LootItem{item=" + itemStack + ",burnable=" + burnableItem + "}";
    }

    public static class Looting {

        public static final Looting DEFAULT = new Looting(0, 1);

        private final int min;
        private final int max;

        public Looting(int min, int max) {
            this.min = min;
            this.max = max;
        }

        public int getMin() {
            return min;
        }

        public int getMax() {
            return max;
        }

    }

}
