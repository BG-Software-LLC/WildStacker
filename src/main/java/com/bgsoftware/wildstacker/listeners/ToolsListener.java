package com.bgsoftware.wildstacker.listeners;

import com.bgsoftware.wildstacker.Locale;
import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.enums.StackCheckResult;
import com.bgsoftware.wildstacker.api.objects.StackedBarrel;
import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.api.objects.StackedObject;
import com.bgsoftware.wildstacker.api.objects.StackedSpawner;
import com.bgsoftware.wildstacker.objects.WStackedBarrel;
import com.bgsoftware.wildstacker.objects.WStackedEntity;
import com.bgsoftware.wildstacker.objects.WStackedSpawner;
import com.bgsoftware.wildstacker.utils.entity.EntityUtils;
import com.bgsoftware.wildstacker.utils.items.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("unused")
public final class ToolsListener implements Listener {

    private final Map<UUID, StackedObject> simulateObjects = new HashMap<>();
    private final WildStackerPlugin plugin;

    public ToolsListener(WildStackerPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityInspect(PlayerInteractEntityEvent e) {
        if (ItemUtils.isOffHand(e) || e.getPlayer().getItemInHand() == null || !e.getPlayer().getItemInHand().isSimilar(plugin.getSettings().getGlobal().getInspectTool()) ||
                !EntityUtils.isStackable(e.getRightClicked()))
            return;

        e.setCancelled(true);

        StackedEntity stackedEntity = WStackedEntity.of(e.getRightClicked());

        Locale.ENTITY_INFO_HEADER.send(e.getPlayer());
        Locale.ENTITY_INFO_UUID.send(e.getPlayer(), stackedEntity.getUniqueId());
        Locale.ENTITY_INFO_TYPE.send(e.getPlayer(), EntityUtils.getFormattedType(stackedEntity.getType().name()));
        Locale.ENTITY_INFO_AMOUNT.send(e.getPlayer(), stackedEntity.getStackAmount());
        Locale.ENTITY_INFO_SPAWN_REASON.send(e.getPlayer(), stackedEntity.getSpawnCause().name());
        Locale.ENTITY_INFO_NERFED.send(e.getPlayer(), stackedEntity.isNerfed() ? "True" : "False");
        Locale.ENTITY_INFO_UPGRADE.send(e.getPlayer(), stackedEntity.getUpgrade().getName());
        Locale.ENTITY_INFO_FOOTER.send(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBarrelInspect(PlayerInteractEvent e) {
        if (e.getItem() == null || !e.getItem().isSimilar(plugin.getSettings().getGlobal().getInspectTool()) || !plugin.getSystemManager().isStackedBarrel(e.getClickedBlock()))
            return;

        e.setCancelled(true);

        StackedBarrel stackedBarrel = WStackedBarrel.of(e.getClickedBlock());

        Locale.BARREL_INFO_HEADER.send(e.getPlayer());
        Locale.BARREL_INFO_TYPE.send(e.getPlayer(), ItemUtils.getFormattedType(stackedBarrel.getBarrelItem(1)));
        Locale.BARREL_INFO_AMOUNT.send(e.getPlayer(), stackedBarrel.getStackAmount());
        Locale.BARREL_INFO_FOOTER.send(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onSpawnerInspect(PlayerInteractEvent e) {
        if (e.getItem() == null || !e.getItem().isSimilar(plugin.getSettings().getGlobal().getInspectTool()) || !plugin.getSystemManager().isStackedSpawner(e.getClickedBlock()))
            return;

        e.setCancelled(true);

        StackedSpawner stackedSpawner = WStackedSpawner.of(e.getClickedBlock());

        Locale.SPAWNER_INFO_HEADER.send(e.getPlayer());
        Locale.SPAWNER_INFO_TYPE.send(e.getPlayer(), EntityUtils.getFormattedType(stackedSpawner.getSpawnedType().name()));
        Locale.SPAWNER_INFO_AMOUNT.send(e.getPlayer(), stackedSpawner.getStackAmount());
        Locale.SPAWNER_INFO_UPGRADE.send(e.getPlayer(), stackedSpawner.getUpgrade().getName());
        Locale.SPAWNER_INFO_FOOTER.send(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntitySimulate(PlayerInteractEntityEvent e) {
        if (ItemUtils.isOffHand(e) || e.getPlayer().getItemInHand() == null || !e.getPlayer().getItemInHand().isSimilar(plugin.getSettings().getGlobal().getSimulateTool()))
            return;

        e.setCancelled(true);

        handleSimulate(e.getPlayer(), WStackedEntity.of(e.getRightClicked()));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockSimulate(PlayerInteractEvent e) {
        if (ItemUtils.isOffHand(e) || e.getPlayer().getItemInHand() == null || !e.getPlayer().getItemInHand().isSimilar(plugin.getSettings().getGlobal().getSimulateTool()) || e.getClickedBlock() == null)
            return;

        e.setCancelled(true);

        if (plugin.getSystemManager().isStackedSpawner(e.getClickedBlock()))
            handleSimulate(e.getPlayer(), WStackedSpawner.of(e.getClickedBlock()));
        else if (plugin.getSystemManager().isStackedBarrel(e.getClickedBlock()))
            handleSimulate(e.getPlayer(), WStackedBarrel.of(e.getClickedBlock()));
    }

    private void handleSimulate(Player player, StackedObject stackedObject) {
        StackedObject firstObject = simulateObjects.get(player.getUniqueId());

        if (firstObject == null) {
            simulateObjects.put(player.getUniqueId(), stackedObject);
            Bukkit.getScheduler().runTaskLater(plugin, () -> simulateObjects.remove(player.getUniqueId()), 1200L);
            Locale.OBJECT_SIMULATE_CHOOSE_SECOND.send(player);
        } else if (stackedObject != firstObject) {
            simulateObjects.remove(player.getUniqueId());

            StackCheckResult stackCheckResult = firstObject.runStackCheck(stackedObject);

            if (stackCheckResult == StackCheckResult.SUCCESS)
                Locale.OBJECT_SIMULATE_SUCCESS_RESULT.send(player);
            else
                Locale.OBJECT_SIMULATE_FAIL_RESULT.send(player, stackCheckResult.name());
        }
    }

}
