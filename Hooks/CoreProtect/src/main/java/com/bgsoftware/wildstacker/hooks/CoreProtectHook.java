package com.bgsoftware.wildstacker.hooks;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.hooks.listeners.IStackedBlockListener;
import com.bgsoftware.wildstacker.utils.threads.Executor;
import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;
import net.coreprotect.config.ConfigHandler;
import net.coreprotect.consumer.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"deprecation", "unused"})
public final class CoreProtectHook {

    private static WildStackerPlugin plugin;

    private static Plugin coreProtect;
    private static boolean pickupSupport = false;
    private static boolean warningDisplayed = false;

    public static void register(WildStackerPlugin plugin) {
        CoreProtectHook.plugin = plugin;
        coreProtect = Bukkit.getPluginManager().getPlugin("CoreProtect");
        try {
            Map<?, ?> map = ConfigHandler.itemsPickup;
            pickupSupport = true;
        } catch (Throwable ignored) {
        }
        plugin.getProviders().registerStackedBlockListener(CoreProtectHook::recordBlockChange);
        plugin.getProviders().registerStackedItemListener(CoreProtectHook::recordItemPickup);
    }

    public static void recordBlockChange(OfflinePlayer offlinePlayer, Location location, Material type, byte data,
                                         IStackedBlockListener.Action action) {
        if (!Bukkit.isPrimaryThread()) {
            Executor.sync(() -> recordBlockChange(offlinePlayer, location, type, data, action));
            return;
        }

        CoreProtectAPI coreProtectAPI = ((CoreProtect) coreProtect).getAPI();

        if (coreProtectAPI.APIVersion() <= 5) {
            switch (action) {
                case BLOCK_BREAK:
                    coreProtectAPI.logRemoval(offlinePlayer.getName(), location, type, data);
                    break;
                case BLOCK_PLACE:
                    coreProtectAPI.logPlacement(offlinePlayer.getName(), location, type, data);
                    break;
            }
        } else if (coreProtectAPI.APIVersion() <= 10) {
            switch (action) {
                case BLOCK_BREAK:
                    coreProtectAPI.logRemoval(offlinePlayer.getName(), location, type,
                            (org.bukkit.block.data.BlockData) plugin.getNMSWorld().getBlockData(type, data));
                    break;
                case BLOCK_PLACE:
                    coreProtectAPI.logPlacement(offlinePlayer.getName(), location, type,
                            (org.bukkit.block.data.BlockData) plugin.getNMSWorld().getBlockData(type, data));
                    break;
            }
        } else if (!warningDisplayed) {
            warningDisplayed = true;
            WildStackerPlugin.log("&cDetected an API version of CoreProtect that is not supported: " + coreProtectAPI.APIVersion());
            WildStackerPlugin.log("&cOpen an issue on github regarding this!");
        }
    }

    public static void recordItemPickup(OfflinePlayer offlinePlayer, Item item, int amount) {
        if (!pickupSupport)
            return;

        String playerName = offlinePlayer.getName();

        if (playerName == null)
            return;

        if (!Bukkit.isPrimaryThread()) {
            Executor.sync(() -> recordItemPickup(offlinePlayer, item, amount));
            return;
        }

        Location location = item.getLocation();
        ItemStack itemStack = item.getItemStack().clone();
        itemStack.setAmount(amount);

        String loggingItemId = playerName.toLowerCase() + "." + location.getBlockX() + "." +
                location.getBlockY() + "." + location.getBlockZ();
        int itemId = getItemId(loggingItemId);
        List<ItemStack> list = ConfigHandler.itemsPickup.getOrDefault(loggingItemId, new ArrayList<>());
        list.add(itemStack);
        ConfigHandler.itemsPickup.put(loggingItemId, list);
        int time = (int) (System.currentTimeMillis() / 1000L) + 1;
        queueItemTransaction(playerName, location, time, itemId);
    }

    private static int getItemId(final String id) {
        final int chestId = ConfigHandler.loggingItem.getOrDefault(id, -1) + 1;
        ConfigHandler.loggingItem.put(id, chestId);
        return chestId;
    }

    private static void queueItemTransaction(String user, Location location, int time, int itemId) {
        int currentConsumer = Consumer.currentConsumer;
        int consumerId = newConsumerId(currentConsumer);
        addConsumer(currentConsumer, new Object[]{consumerId, 26, null, 0, null, time, itemId, null});
        queueStandardData(consumerId, currentConsumer, new String[]{user, null}, location);
    }

    private static int newConsumerId(int consumer) {
        int id = Consumer.consumer_id.get(consumer)[0];
        Consumer.consumer_id.put(consumer, new Integer[]{id + 1, 1});
        return id;
    }

    private static void addConsumer(int currentConsumer, Object[] data) {
        Consumer.consumer.get(currentConsumer).add(data);
    }

    private static void queueStandardData(int consumerId, int currentConsumer, String[] user, Object object) {
        Consumer.consumerUsers.get(currentConsumer).put(consumerId, user);
        Consumer.consumerObjects.get(currentConsumer).put(consumerId, object);
        Consumer.consumer_id.put(currentConsumer, new Integer[]{Consumer.consumer_id.get(currentConsumer)[0], 0});
    }

}
