package com.bgsoftware.wildstacker.hooks;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.objects.StackedItem;
import com.bgsoftware.wildstacker.utils.threads.Executor;
import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;
import net.coreprotect.config.ConfigHandler;
import net.coreprotect.consumer.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings("deprecation")
public final class CoreProtectHook {

    private static final WildStackerPlugin plugin = WildStackerPlugin.getPlugin();

    private static Plugin coreProtect;
    private static boolean pickupSupport = false;

    public static void setEnabled(boolean enabled) {
        coreProtect = enabled ? Bukkit.getPluginManager().getPlugin("CoreProtect") : null;
        try {
            Map<?, ?> map = ConfigHandler.itemsPickup;
            pickupSupport = true;
        } catch (Throwable ignored) {
        }
    }

    public static void recordBlockChange(OfflinePlayer offlinePlayer, Block block, boolean place) {
        recordBlockChange(offlinePlayer, block.getLocation(), block.getType(), block.getData(), place);
    }

    public static void recordBlockChange(OfflinePlayer offlinePlayer, Location location, Material type, byte data, boolean place) {
        if (coreProtect == null)
            return;

        if (!Bukkit.isPrimaryThread()) {
            Executor.sync(() -> recordBlockChange(offlinePlayer, location, type, data, place));
            return;
        }

        CoreProtectAPI coreProtectAPI = ((CoreProtect) coreProtect).getAPI();

        if (coreProtectAPI.APIVersion() == 5) {
            if (!place)
                coreProtectAPI.logRemoval(offlinePlayer.getName(), location, type, data);
            else
                coreProtectAPI.logPlacement(offlinePlayer.getName(), location, type, data);
        } else if (coreProtectAPI.APIVersion() == 6) {
            if (!place)
                coreProtectAPI.logRemoval(offlinePlayer.getName(), location, type,
                        (org.bukkit.block.data.BlockData) plugin.getNMSAdapter().getBlockData(type, data));
            else
                coreProtectAPI.logPlacement(offlinePlayer.getName(), location, type,
                        (org.bukkit.block.data.BlockData) plugin.getNMSAdapter().getBlockData(type, data));
        }
    }

    public static void recordItemPickup(OfflinePlayer offlinePlayer, StackedItem stackedItem, int amount) {
        if (coreProtect == null || !pickupSupport)
            return;

        if (!Bukkit.isPrimaryThread()) {
            Executor.sync(() -> recordItemPickup(offlinePlayer, stackedItem, amount));
            return;
        }

        Location location = stackedItem.getLocation();
        ItemStack itemStack = stackedItem.getItemStack().clone();
        itemStack.setAmount(amount);

        String loggingItemId = offlinePlayer.getName().toLowerCase() + "." + location.getBlockX() + "." + location.getBlockY() + "." + location.getBlockZ();
        int itemId = getItemId(loggingItemId);
        List<ItemStack> list = ConfigHandler.itemsPickup.getOrDefault(loggingItemId, new ArrayList<>());
        list.add(itemStack);
        ConfigHandler.itemsPickup.put(loggingItemId, list);
        int time = (int) (System.currentTimeMillis() / 1000L) + 1;
        queueItemTransaction(offlinePlayer.getName(), location, time, itemId);
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
