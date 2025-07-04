package com.bgsoftware.wildstacker.objects;

import com.bgsoftware.wildstacker.api.objects.StackedBarrel;
import com.bgsoftware.wildstacker.api.objects.UnloadedStackedBarrel;
import com.bgsoftware.wildstacker.database.Query;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

public final class WUnloadedStackedBarrel extends WUnloadedStackedObject implements UnloadedStackedBarrel {

    private final ItemStack barrelItem;

    public WUnloadedStackedBarrel(StackedBarrel stackedBarrel) {
        this(stackedBarrel.getLocation(), stackedBarrel.getStackAmount(), stackedBarrel.getBarrelItem(1));
    }

    public WUnloadedStackedBarrel(Location location, int stackAmount, ItemStack barrelItem) {
        this(location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ(),
                stackAmount, barrelItem);
    }

    public WUnloadedStackedBarrel(String worldName, int locX, int locY, int locZ, int stackAmount, ItemStack barrelItem) {
        super(worldName, locX, locY, locZ, stackAmount);
        this.barrelItem = barrelItem;
    }

    @Override
    public ItemStack getBarrelItem(int amount) {
        ItemStack barrelItem = this.barrelItem.clone();
        barrelItem.setAmount(amount);
        return barrelItem;
    }

    @Override
    public void remove() {
        plugin.getDataHandler().CACHED_BARRELS_RAW.remove(this);

        Query.BARREL_DELETE.getStatementHolder()
                .setLocation(this)
                .execute(true);
    }

}
