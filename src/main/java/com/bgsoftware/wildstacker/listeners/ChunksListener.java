package com.bgsoftware.wildstacker.listeners;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.enums.SpawnCause;
import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.objects.WStackedEntity;
import com.bgsoftware.wildstacker.utils.ServerVersion;
import com.bgsoftware.wildstacker.utils.entity.EntityUtils;
import com.bgsoftware.wildstacker.utils.threads.Executor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

@SuppressWarnings("unused")
public final class ChunksListener implements Listener {

    private final WildStackerPlugin plugin;

    public static boolean loadedData = false;

    public ChunksListener(WildStackerPlugin plugin){
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChunkUnload(ChunkUnloadEvent e){
        Entity[] entityList = e.getChunk().getEntities();
        Stream<StackedEntity> entityStream = Arrays.stream(entityList)
                .filter(EntityUtils::isStackable).map(WStackedEntity::of);

        Executor.async(() -> {
            //Arrays.stream(entityList).forEach(entity -> EntityData.uncache(entity.getUniqueId()));

            entityStream.forEach(stackedEntity -> {
                if(stackedEntity.getStackAmount() > 1) {
                    plugin.getDataHandler().CACHED_AMOUNT_ENTITIES.put(stackedEntity.getUniqueId(), stackedEntity.getStackAmount());
                }
                if(hasValidSpawnCause(stackedEntity.getSpawnCause())){
                    plugin.getDataHandler().CACHED_SPAWN_CAUSE_ENTITIES.put(stackedEntity.getUniqueId(), stackedEntity.getSpawnCause());
                }
                plugin.getSystemManager().removeStackObject(stackedEntity);
            });
        });
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent e){
        List<Entity> chunkEntities = Arrays.asList(e.getChunk().getEntities());

        if(loadedData && ServerVersion.isAtLeast(ServerVersion.v1_8)) {
            //Trying to remove all the corrupted stacked blocks
            Executor.async(() -> {
                Stream<Entity> entityStream = chunkEntities.stream()
                        .filter(entity -> entity instanceof ArmorStand && entity.getCustomName() != null &&
                                entity.getCustomName().equals("BlockDisplay") && !plugin.getSystemManager().isStackedBarrel(entity.getLocation().getBlock()));
                Executor.sync(() -> entityStream.forEach(entity -> {
                    Block block = entity.getLocation().getBlock();
                    if (block.getType() == Material.CAULDRON)
                        block.setType(Material.AIR);
                    entity.remove();
                }));
            });
        }

        //Update all nerf status to all entities
        Executor.async(() -> chunkEntities.stream().filter(EntityUtils::isStackable)
                .forEach(entity -> WStackedEntity.of(entity).updateNerfed()));
    }

    private boolean hasValidSpawnCause(SpawnCause spawnCause){
        return spawnCause != SpawnCause.CHUNK_GEN && spawnCause != SpawnCause.NATURAL;
    }

}
