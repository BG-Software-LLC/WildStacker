package com.bgsoftware.wildstacker.objects;

import com.bgsoftware.wildstacker.api.objects.StackedSpawner;
import com.bgsoftware.wildstacker.api.objects.UnloadedStackedSpawner;
import com.bgsoftware.wildstacker.database.Query;
import com.bgsoftware.wildstacker.utils.chunks.ChunkPosition;
import org.bukkit.Location;

import java.util.Map;

public final class WUnloadedStackedSpawner extends WUnloadedStackedObject implements UnloadedStackedSpawner {

    public WUnloadedStackedSpawner(StackedSpawner stackedSpawner){
        this(stackedSpawner.getLocation(), stackedSpawner.getStackAmount());
    }

    public WUnloadedStackedSpawner(Location location, int stackAmount){
        super(location, stackAmount);
    }

    @Override
    public void remove() {
        Location location = getLocation();
        Map<Location, UnloadedStackedSpawner> cachedSpawners = plugin.getDataHandler().CACHED_SPAWNERS_RAW.get(new ChunkPosition(location));
        if(cachedSpawners != null)
            cachedSpawners.remove(location);

        Query.SPAWNER_DELETE.getStatementHolder()
                .setLocation(getLocation())
                .execute(true);
    }

}
