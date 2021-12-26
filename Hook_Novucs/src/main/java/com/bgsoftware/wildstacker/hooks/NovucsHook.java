package com.bgsoftware.wildstacker.hooks;

import com.bgsoftware.common.reflection.ReflectField;
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
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;

@SuppressWarnings("unused")
public final class NovucsHook {

    private static WildStackerPlugin plugin;
    private static FactionsTopPlugin novucs;

    public static void register(WildStackerPlugin plugin) {
        NovucsHook.plugin = plugin;
        novucs = JavaPlugin.getPlugin(FactionsTopPlugin.class);
        new ReflectField<>(FactionsTopPlugin.class, SpawnerStackerHook.class,
                "spawnerStackerHook").set(novucs, new WildStackerHook());
        plugin.getServer().getPluginManager().registerEvents(new NovucsListener(), plugin);
    }

    private static final class WildStackerHook implements SpawnerStackerHook {

        @Override
        public void initialize() {
        }

        @Override
        public EntityType getSpawnedType(ItemStack itemStack) {
            return plugin.getProviders().getSpawnersProvider().getSpawnerType(itemStack);
        }

        @Override
        public int getStackSize(ItemStack itemStack) {
            return ItemUtils.getSpawnerItemAmount(itemStack);
        }

        @Override
        public int getStackSize(CreatureSpawner creatureSpawner) {
            return WStackedSpawner.of(creatureSpawner).getStackAmount();
        }

    }

    private static final class NovucsListener implements Listener {

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void updateWorth(SpawnerStackEvent e) {
            updateWorth(e.getSpawner().getLocation().getBlock(), RecalculateReason.PLACE, e.getTarget().getStackAmount());
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void updateWorth(SpawnerUnstackEvent e) {
            updateWorth(e.getSpawner().getLocation().getBlock(), RecalculateReason.BREAK, e.getAmount());
        }

        private void updateWorth(Block block, RecalculateReason reason, int amount) {
            String factionId = novucs.getFactionsHook().getFactionAt(block);

            if (novucs.getSettings().getIgnoredFactionIds().contains(factionId))
                return;

            int multiplier = reason == RecalculateReason.BREAK ? -amount : amount;

            double price = multiplier * novucs.getSettings().getBlockPrice(block.getType());

            novucs.getWorthManager().add(block.getChunk(), reason, WorthType.BLOCK, price,
                    ImmutableMap.of(block.getType(), multiplier), new HashMap<>());

            CreatureSpawner creatureSpawner = (CreatureSpawner) block.getState();
            EntityType spawnedType = creatureSpawner.getSpawnedType();
            price = multiplier * novucs.getSettings().getSpawnerPrice(spawnedType);

            novucs.getWorthManager().add(block.getChunk(), reason, WorthType.SPAWNER, price,
                    new HashMap<>(), ImmutableMap.of(spawnedType, multiplier));
        }
    }

}
