package com.bgsoftware.wildstacker.hooks;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.api.objects.StackedItem;
import com.bgsoftware.wildstacker.utils.threads.Executor;

public final class DataSerializer_Default implements IDataSerializer {

    private final WildStackerPlugin plugin;

    public DataSerializer_Default(WildStackerPlugin plugin){
        this.plugin = plugin;
    }

    @Override
    public void saveEntity(StackedEntity stackedEntity) {
        Executor.sync(() -> plugin.getNMSAdapter().saveEntity(stackedEntity));
    }

    @Override
    public void loadEntity(StackedEntity stackedEntity) {
        Executor.sync(() -> plugin.getNMSAdapter().loadEntity(stackedEntity));
    }

    @Override
    public void saveItem(StackedItem stackedItem) {
        plugin.getNMSAdapter().saveItem(stackedItem);
    }

    @Override
    public void loadItem(StackedItem stackedItem) {
        plugin.getNMSAdapter().loadItem(stackedItem);
    }

}
