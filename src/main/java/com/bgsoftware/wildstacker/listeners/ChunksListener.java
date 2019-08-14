package com.bgsoftware.wildstacker.listeners;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.enums.SpawnCause;
import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.objects.WStackedEntity;
import com.bgsoftware.wildstacker.utils.Executor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public final class ChunksListener implements Listener {

    private WildStackerPlugin plugin;

    public ChunksListener(WildStackerPlugin plugin){
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChunkUnload(ChunkUnloadEvent e){
        List<StackedEntity> stackedEntities = Arrays.stream(e.getChunk().getEntities())
                .filter(entity -> entity instanceof LivingEntity).map(WStackedEntity::of).collect(Collectors.toList());

        Executor.async(() -> stackedEntities.stream()
                .filter(stackedEntity -> stackedEntity.getStackAmount() > 1 || hasValidSpawnCause(stackedEntity.getSpawnCause()))
                .forEach(stackedEntity -> {
                    plugin.getDataHandler().CACHED_AMOUNT_ENTITIES.put(stackedEntity.getUniqueId(), stackedEntity.getStackAmount());
                    plugin.getDataHandler().CACHED_SPAWN_CAUSE_ENTITIES.put(stackedEntity.getUniqueId(), stackedEntity.getSpawnCause());
                    plugin.getSystemManager().removeStackObject(stackedEntity);
                }));
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent e){
        //Trying to remove all the corrupted stacked blocks
        Arrays.stream(e.getChunk().getEntities())
                .filter(entity -> entity instanceof ArmorStand && entity.getCustomName() != null &&
                        entity.getCustomName().equals("BlockDisplay") && !plugin.getSystemManager().isStackedBarrel(entity.getLocation().getBlock()))
                .forEach(entity -> {
                    Block block = entity.getLocation().getBlock();
                    Bukkit.broadcastMessage(block.getType() + "");
                    if (block.getType() == Material.CAULDRON)
                        block.setType(Material.AIR);
                    entity.remove();
                });
    }

    private boolean hasValidSpawnCause(SpawnCause spawnCause){
        return spawnCause != SpawnCause.CHUNK_GEN && spawnCause != SpawnCause.NATURAL;
    }

}
