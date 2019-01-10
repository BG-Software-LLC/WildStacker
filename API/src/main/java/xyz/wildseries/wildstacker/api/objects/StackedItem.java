package xyz.wildseries.wildstacker.api.objects;

import org.bukkit.entity.Item;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public interface StackedItem extends StackedObject<Item> {

    Item getItem();

    UUID getUniqueId();

    void setItemStack(ItemStack itemStack);

    ItemStack getItemStack();

    void giveItemStack(Inventory inventory);

}
