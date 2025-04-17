package com.bgsoftware.wildstacker.hooks;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.objects.StackedSpawner;
import com.bgsoftware.wildstacker.api.upgrades.SpawnerUpgrade;
import com.bgsoftware.wildstacker.utils.events.EventsCaller;
import com.bgsoftware.wildstacker.utils.items.ItemUtils;
import com.dnyferguson.mineablespawners.MineableSpawners;
import com.dnyferguson.mineablespawners.listeners.SpawnerMineListener;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public final class SpawnersProvider_MineableSpawners implements SpawnersProvider {

    private final WildStackerPlugin plugin;
    private final MineableSpawners mineableSpawners;

    private Map<String, Double> permissionChances = new HashMap<>();
    private Map<EntityType, Double> prices = new HashMap<>();
    private boolean allSamePrice = false;
    private double globalPrice = 0.0;

    public SpawnersProvider_MineableSpawners(WildStackerPlugin plugin) {
        this.plugin = plugin;
        mineableSpawners = JavaPlugin.getPlugin(MineableSpawners.class);
        Listener spawnerMineListener = Arrays.stream(BlockBreakEvent.getHandlerList().getRegisteredListeners())
                .filter(registeredListener -> registeredListener.getPlugin() == mineableSpawners)
                .map(RegisteredListener::getListener).findFirst().orElse(null);

        try {
            if (spawnerMineListener == null)
                throw new NullPointerException("Error while trying to find the listener instance.");

            /* Permission Chances */
            {
                Field permissionChancesField = SpawnerMineListener.class.getDeclaredField("permissionChances");
                permissionChancesField.setAccessible(true);
                //noinspection unchecked
                permissionChances = (Map<String, Double>) permissionChancesField.get(spawnerMineListener);
            }

            /* Prices */
            {
                Field pricesField = SpawnerMineListener.class.getDeclaredField("prices");
                pricesField.setAccessible(true);
                //noinspection unchecked
                prices = (Map<EntityType, Double>) pricesField.get(spawnerMineListener);
            }

            /* Same Price */
            {
                Field allSamePriceField = SpawnerMineListener.class.getDeclaredField("allSamePrice");
                allSamePriceField.setAccessible(true);
                allSamePrice = (Boolean) allSamePriceField.get(spawnerMineListener);
            }

            /* Global Price */
            {
                Field globalPriceField = SpawnerMineListener.class.getDeclaredField("globalPrice");
                globalPriceField.setAccessible(true);
                globalPrice = (Double) globalPriceField.get(spawnerMineListener);
            }
        } catch (Throwable ex) {
            WildStackerPlugin.log("&cError while hooking into MS - can cause conflicts / unintended bypasses.");
            ex.printStackTrace();
            return;
        }

        WildStackerPlugin.log(" - Using MineableSpawners as SpawnersProvider.");
    }

    @Override
    public ItemStack getSpawnerItem(EntityType entityType, int amount, @Nullable SpawnerUpgrade spawnerUpgrade) {
        ItemStack itemStack = MineableSpawners.getApi().getSpawnerFromEntityType(entityType);
        itemStack.setAmount(amount);
        return spawnerUpgrade == null || spawnerUpgrade.isDefault() ? itemStack :
                ItemUtils.setSpawnerUpgrade(itemStack, spawnerUpgrade.getId());
    }

    @Override
    public EntityType getSpawnerType(ItemStack itemStack) {
        return MineableSpawners.getApi().getEntityTypeFromItemStack(itemStack);
    }

    @Override
    public void handleSpawnerExplode(StackedSpawner stackedSpawner, Entity entity, Player ignite, int brokenAmount) {
        if (!mineableSpawners.getConfigurationHandler().getBoolean("explode", "drop"))
            return;

        double dropChance = mineableSpawners.getConfigurationHandler().getDouble("explode", "chance") / 100.0;

        if (dropChance != 1.0) {
            double random = Math.random();
            if (random >= dropChance)
                return;
        }

        dropSpawner(stackedSpawner, ignite, brokenAmount);
    }

    @Override
    public void handleSpawnerBreak(StackedSpawner stackedSpawner, Player player, int brokenAmount, boolean breakMenu) {
        EntityType entityType = stackedSpawner.getSpawnedType();
        boolean bypassing = breakMenu || player.getGameMode().equals(GameMode.CREATIVE) || player.hasPermission("mineablespawners.bypass");

        if (!bypassing) {
            if (mineableSpawners.getConfigurationHandler().getList("mining", "blacklisted-worlds").contains(player.getWorld().getName()))
                return;

            if (mineableSpawners.getConfigurationHandler().getBoolean("mining", "require-permission") && !player.hasPermission("mineablespawners.mine"))
                return;

            if (mineableSpawners.getConfigurationHandler().getBoolean("mining", "require-individual-permission") && !player.hasPermission("mineablespawners.mine." + entityType.name().toLowerCase()))
                return;

            ItemStack heldItem = player.getItemInHand();

            if (heldItem != null && !mineableSpawners.getConfigurationHandler().getList("mining", "tools").contains(heldItem.getType().name()))
                return;

            if (mineableSpawners.getConfigurationHandler().getBoolean("mining", "require-silktouch") && !player.hasPermission("mineablespawners.nosilk")) {
                int silkTouchLevel = 0;

                if (heldItem != null && heldItem.containsEnchantment(Enchantment.SILK_TOUCH))
                    silkTouchLevel = heldItem.getEnchantmentLevel(Enchantment.SILK_TOUCH);

                if (this.mineableSpawners.getConfigurationHandler().getBoolean("mining", "require-silktouch-level")) {
                    int requiredLevel = mineableSpawners.getConfigurationHandler().getInteger("mining", "required-level");
                    if (silkTouchLevel < requiredLevel && !plugin.getProviders().hasEnchantmentLevel(heldItem,
                            Enchantment.SILK_TOUCH, requiredLevel))
                        return;
                } else if (silkTouchLevel < 1)
                    return;
            }

            if (mineableSpawners.getEcon() != null && mineableSpawners.getConfigurationHandler().getBoolean("mining", "charge")) {
                double cost = !allSamePrice ? globalPrice : prices.getOrDefault(entityType, globalPrice);

                if (!mineableSpawners.getEcon().withdrawPlayer(player, cost).transactionSuccess())
                    return;
            }

            double dropChance;

            if (mineableSpawners.getConfigurationHandler().getBoolean("mining", "use-perm-based-chances") && permissionChances.size() > 0) {
                dropChance = 0;
                for (String perm : permissionChances.keySet()) {
                    if (player.hasPermission(perm)) {
                        dropChance = Math.max(dropChance, permissionChances.get(perm) / 100.0);
                    }
                }
            } else {
                dropChance = mineableSpawners.getConfigurationHandler().getDouble("mining", "chance") / 100.0;
            }

            if (dropChance != 1.0) {
                double random = Math.random();
                if (random >= dropChance)
                    return;
            }
        }

        dropSpawner(stackedSpawner, player, brokenAmount);
    }

    @Override
    public void handleSpawnerPlace(CreatureSpawner creatureSpawner, ItemStack itemStack) {

    }

    @Override
    public void dropSpawner(StackedSpawner stackedSpawner, Player player, int amount) {
        ItemStack dropItem = EventsCaller.callSpawnerDropEvent(stackedSpawner, player, amount);
        Location toDrop = ItemUtils.getSafeDropLocation(stackedSpawner.getLocation());

        if (mineableSpawners.getConfigurationHandler().getBoolean("mining", "drop-to-inventory")) {
            ItemUtils.addItem(dropItem, player.getInventory(), toDrop);
        } else {
            ItemUtils.dropItem(dropItem, toDrop);
        }
    }

}
