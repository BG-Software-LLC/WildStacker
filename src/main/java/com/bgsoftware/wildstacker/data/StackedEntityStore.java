package com.bgsoftware.wildstacker.data;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.enums.SpawnCause;
import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.objects.WStackedEntity;
import com.bgsoftware.wildstacker.utils.data.DataSerializer;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class StackedEntityStore extends AbstractEntityDataStore<StackedEntity> {

    private static final WildStackerPlugin plugin = WildStackerPlugin.getPlugin();

    private final Set<UUID> deadEntities = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public boolean isDead(UUID uuid, boolean remove) {
        return remove ? this.deadEntities.remove(uuid) : this.deadEntities.contains(uuid);
    }

    public void setDead(UUID uuid) {
        this.deadEntities.add(uuid);
    }

    @Override
    protected boolean loadUnloadedInternal(UUID uuid, StackedEntity stackedEntity) {
        boolean unloadedFromDb = super.loadUnloadedInternal(uuid, stackedEntity);
        if (unloadedFromDb)
            return true;

        try {
            ((WStackedEntity) stackedEntity).setSaveData(false);
            loadUnloadedFromCache(stackedEntity);
        } finally {
            ((WStackedEntity) stackedEntity).setSaveData(true);
        }

        return true;
    }

    private void loadUnloadedFromCache(StackedEntity stackedEntity) {
        String cachedData = DataSerializer.deserializeData(stackedEntity.getCustomName());
        if (!cachedData.isEmpty()) {
            String[] dataSections = cachedData.split("-");
            try {
                stackedEntity.setStackAmount(Integer.parseInt(dataSections[0]), false);
            } catch (Exception ignored) {
            }
            try {
                stackedEntity.setSpawnCause(SpawnCause.valueOf(Integer.parseInt(dataSections[1])));
            } catch (Exception ignored) {
            }
            try {
                if (dataSections[2].equals("1"))
                    ((WStackedEntity) stackedEntity).setNameTag();
            } catch (Exception ignored) {
            }

            stackedEntity.setCustomName(DataSerializer.stripData(stackedEntity.getCustomName()));
        } else {
            plugin.getSystemManager().getDataSerializer().loadEntity(stackedEntity);
        }
    }

    public static class Unloaded implements AbstractEntityDataStore.Unloaded<StackedEntity> {

        private final int stackAmount;
        private final SpawnCause spawnCause;

        public Unloaded(int stackAmount, SpawnCause spawnCause) {
            this.stackAmount = stackAmount;
            this.spawnCause = spawnCause;
        }

        @Override
        public void load(StackedEntity object) {
            object.setStackAmount(this.stackAmount, false);
            object.setSpawnCause(this.spawnCause);
        }

    }

}
