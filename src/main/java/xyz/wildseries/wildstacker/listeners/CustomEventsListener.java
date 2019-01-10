package xyz.wildseries.wildstacker.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import xyz.wildseries.wildstacker.WildStackerPlugin;
import xyz.wildseries.wildstacker.listeners.events.EntityBreedEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class CustomEventsListener implements Listener {

    private WildStackerPlugin plugin;

    public CustomEventsListener(WildStackerPlugin plugin){
        this.plugin = plugin;
    }

    private Map<UUID, Set<UUID>> playerBreedCache = new HashMap<>();

    @EventHandler
    public void onEntityFeedByPlayer(PlayerInteractAtEntityEvent e){
        if(!(e.getRightClicked() instanceof LivingEntity))
            return;

        if(Bukkit.getBukkitVersion().contains("1.13"))
            return;

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if(!plugin.getNMSAdapter().isInLove(e.getRightClicked()))
                return;

            if (!playerBreedCache.containsKey(e.getPlayer().getUniqueId())) {
                playerBreedCache.put(e.getPlayer().getUniqueId(), new HashSet<>());
            }

            Set<UUID> bredEntities = playerBreedCache.get(e.getPlayer().getUniqueId());

            for(Entity entity : e.getRightClicked().getNearbyEntities(7, 7, 7)){
                if(bredEntities.contains(entity.getUniqueId())){
                    bredEntities.remove(entity.getUniqueId());
                    if(bredEntities.isEmpty())
                        playerBreedCache.remove(e.getPlayer().getUniqueId());
                    EntityBreedEvent entityBreedEvent = new EntityBreedEvent((LivingEntity) e.getRightClicked(), (LivingEntity) entity);
                    Bukkit.getPluginManager().callEvent(entityBreedEvent);
                }
            }

            bredEntities.add(e.getRightClicked().getUniqueId());

        }, 1L);
    }

}
