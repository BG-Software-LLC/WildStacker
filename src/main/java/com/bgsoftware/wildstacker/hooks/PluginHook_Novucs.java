package com.bgsoftware.wildstacker.hooks;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.events.SpawnerStackEvent;
import com.bgsoftware.wildstacker.api.events.SpawnerUnstackEvent;
import com.bgsoftware.wildstacker.objects.WStackedSpawner;
import com.bgsoftware.wildstacker.utils.items.ItemUtils;
import com.google.common.collect.ImmutableMap;
import net.novucs.ftop.FactionsTopPlugin;
import net.novucs.ftop.RecalculateReason;
import net.novucs.ftop.WorthType;
import net.novucs.ftop.hook.SpawnerStackerHook;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.util.HashMap;

@SuppressWarnings("unused")
public final class PluginHook_Novucs implements SpawnerStackerHook, Listener {

    private final WildStackerPlugin instance;
    private final FactionsTopPlugin plugin;

    private PluginHook_Novucs(WildStackerPlugin instance) {
        this.instance = instance;
        this.plugin = JavaPlugin.getPlugin(FactionsTopPlugin.class);
        Bukkit.getPluginManager().registerEvents(this, instance);
    }

    public static void setEnabled(WildStackerPlugin plugin) {
        try {
            Field field = FactionsTopPlugin.class.getDeclaredField("spawnerStackerHook");
            field.setAccessible(true);
            field.set(JavaPlugin.getPlugin(FactionsTopPlugin.class), new PluginHook_Novucs(plugin));
        } catch (NoClassDefFoundError | Exception ignored) {
        }
    }

    @Override
    public void initialize() {
    }

    @Override
    public EntityType getSpawnedType(ItemStack itemStack) {
        return instance.getProviders().getSpawnerType(itemStack);
    }

    @Override
    public int getStackSize(ItemStack itemStack) {
        return ItemUtils.getSpawnerItemAmount(itemStack);
    }

    @Override
    public int getStackSize(CreatureSpawner creatureSpawner) {
        return WStackedSpawner.of(creatureSpawner).getStackAmount();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void updateWorth(SpawnerStackEvent e) {
        updateWorth(e.getSpawner().getLocation().getBlock(), RecalculateReason.PLACE, e.getTarget().getStackAmount());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void updateWorth(SpawnerUnstackEvent e) {
        updateWorth(e.getSpawner().getLocation().getBlock(), RecalculateReason.BREAK, e.getAmount());
    }

    private void updateWorth(Block block, RecalculateReason reason, int amount) {
        String factionId = plugin.getFactionsHook().getFactionAt(block);

        if (plugin.getSettings().getIgnoredFactionIds().contains(factionId))
            return;

        int multiplier = reason == RecalculateReason.BREAK ? -amount : amount;

        double price = multiplier * plugin.getSettings().getBlockPrice(block.getType());

        plugin.getWorthManager().add(block.getChunk(), reason, WorthType.BLOCK, price,
                ImmutableMap.of(block.getType(), multiplier), new HashMap<>());

        CreatureSpawner creatureSpawner = (CreatureSpawner) block.getState();
        EntityType spawnedType = creatureSpawner.getSpawnedType();
        price = multiplier * plugin.getSettings().getSpawnerPrice(spawnedType);

        plugin.getWorthManager().add(block.getChunk(), reason, WorthType.SPAWNER, price, new HashMap<>(), ImmutableMap.of(spawnedType, multiplier));
    }

}
