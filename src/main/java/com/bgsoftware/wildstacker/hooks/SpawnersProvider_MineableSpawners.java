package com.bgsoftware.wildstacker.hooks;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.objects.StackedSpawner;
import com.bgsoftware.wildstacker.utils.events.EventsCaller;
import com.bgsoftware.wildstacker.utils.items.ItemUtils;
import com.dnyferguson.mineablespawners.MineableSpawners;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public final class SpawnersProvider_MineableSpawners implements SpawnersProvider {

    private final Map<String, Double> permissionChances = new HashMap<>();
    private final MineableSpawners plugin;

    public SpawnersProvider_MineableSpawners(){
        WildStackerPlugin.log(" - Using MineableSpawners as SpawnersProvider.");
        plugin = JavaPlugin.getPlugin(MineableSpawners.class);
        for (String line : plugin.getConfigurationHandler().getList("mining", "perm-based-chances")) {
            String[] args = line.split(":");
            try {
                String permission = args[0];
                double chance = Double.parseDouble(args[1]);
                permissionChances.put(permission, chance);
            } catch (Exception ignored) {}
        }
    }

    @Override
    public ItemStack getSpawnerItem(EntityType entityType, int amount) {
        ItemStack itemStack = MineableSpawners.getApi().getSpawnerFromEntityType(entityType);
        itemStack.setAmount(amount);
        return itemStack;
    }

    @Override
    public EntityType getSpawnerType(ItemStack itemStack) {
        return MineableSpawners.getApi().getEntityTypeFromItemStack(itemStack);
    }

    @Override
    public void handleSpawnerExplode(StackedSpawner stackedSpawner, Entity entity, Player ignite, int brokenAmount) {
        if (!plugin.getConfigurationHandler().getBoolean("explode", "drop"))
            return;

        double dropChance = plugin.getConfigurationHandler().getDouble("explode", "chance") / 100.0;

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

        if(!bypassing){
            if (plugin.getConfigurationHandler().getList("mining", "blacklisted-worlds").contains(player.getWorld().getName()))
                return;

            if (plugin.getConfigurationHandler().getBoolean("mining", "require-permission") && !player.hasPermission("mineablespawners.mine"))
                return;

            if (plugin.getConfigurationHandler().getBoolean("mining", "require-individual-permission") && !player.hasPermission("mineablespawners.mine." + entityType.name().toLowerCase()))
                return;

            ItemStack heldItem = player.getItemInHand();

            if (heldItem != null && !plugin.getConfigurationHandler().getList("mining", "tools").contains(heldItem.getType().name()))
                return;

            if (plugin.getConfigurationHandler().getBoolean("mining", "require-silktouch") && !player.hasPermission("mineablespawners.nosilk")) {
                int silkTouchLevel = 0;

                if (heldItem != null && heldItem.containsEnchantment(Enchantment.SILK_TOUCH))
                    silkTouchLevel = heldItem.getEnchantmentLevel(Enchantment.SILK_TOUCH);

                if (this.plugin.getConfigurationHandler().getBoolean("mining", "require-silktouch-level")) {
                    int requiredLevel = plugin.getConfigurationHandler().getInteger("mining", "required-level");
                    if (silkTouchLevel < requiredLevel && !WildToolsHook.hasSilkTouch(heldItem))
                        return;
                }

                else if (silkTouchLevel < 1)
                    return;
            }

            double dropChance = 1.0;
            if (plugin.getConfigurationHandler().getBoolean("mining", "use-perm-based-chances") && permissionChances.size() > 0) {
                for (String perm : permissionChances.keySet()) {
                    if (player.hasPermission(perm)) {
                        dropChance = permissionChances.get(perm) / 100.0;
                        break;
                    }
                }
            }
            else {
                dropChance = plugin.getConfigurationHandler().getDouble("mining", "chance") / 100.0;
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

        if (plugin.getConfigurationHandler().getBoolean("mining", "drop-to-inventory")) {
            ItemUtils.addItem(dropItem, player.getInventory(), toDrop);
        }
        else{
            ItemUtils.dropItem(dropItem, toDrop);
        }
    }

}
