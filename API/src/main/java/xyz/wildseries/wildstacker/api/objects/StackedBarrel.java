package xyz.wildseries.wildstacker.api.objects;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.ItemStack;

public interface StackedBarrel extends StackedObject<Block> {

    Block getBlock();

    Material getType();

    short getData();

    Location getLocation();

    void createDisplayBlock();

    void removeDisplayBlock();

    ArmorStand getDisplayBlock();

    ItemStack getBarrelItem(int amount);

}
