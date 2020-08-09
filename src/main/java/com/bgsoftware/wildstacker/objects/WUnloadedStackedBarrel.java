package com.bgsoftware.wildstacker.objects;

import com.bgsoftware.wildstacker.api.objects.StackedBarrel;
import com.bgsoftware.wildstacker.api.objects.UnloadedStackedBarrel;
import com.bgsoftware.wildstacker.database.Query;
import com.bgsoftware.wildstacker.utils.chunks.ChunkPosition;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public final class WUnloadedStackedBarrel extends WUnloadedStackedObject implements UnloadedStackedBarrel {

    private final ItemStack barrelItem;

    public WUnloadedStackedBarrel(StackedBarrel stackedBarrel){
        this(stackedBarrel.getLocation(), stackedBarrel.getStackAmount(), stackedBarrel.getBarrelItem(1));
    }

    public WUnloadedStackedBarrel(Location location, int stackAmount, ItemStack barrelItem){
        super(location, stackAmount);
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
        Location location = getLocation();
        Map<Location, UnloadedStackedBarrel> cachedBarrels = plugin.getDataHandler().CACHED_BARRELS_RAW.get(new ChunkPosition(location));
        if(cachedBarrels != null)
            cachedBarrels.remove(location);

        Query.BARREL_DELETE.getStatementHolder()
                .setLocation(getLocation())
                .execute(true);
    }

}
