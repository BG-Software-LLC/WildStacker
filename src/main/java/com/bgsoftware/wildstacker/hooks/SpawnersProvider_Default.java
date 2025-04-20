package com.bgsoftware.wildstacker.hooks;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.config.SettingsManager;
import com.bgsoftware.wildstacker.api.objects.StackedSpawner;
import com.bgsoftware.wildstacker.api.upgrades.SpawnerUpgrade;
import com.bgsoftware.wildstacker.utils.entity.EntityUtils;
import com.bgsoftware.wildstacker.utils.events.EventsCaller;
import com.bgsoftware.wildstacker.utils.items.ItemUtils;
import com.bgsoftware.wildstacker.utils.legacy.Materials;
import org.bukkit.Location;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class SpawnersProvider_Default implements SpawnersProvider {

    private final WildStackerPlugin plugin = WildStackerPlugin.getPlugin();

    public SpawnersProvider_Default() {
        WildStackerPlugin.log(" - Couldn't find any spawners providers, using default one.");
    }

    @Override
    public ItemStack getSpawnerItem(EntityType entityType, int amount, @Nullable SpawnerUpgrade spawnerUpgrade) {
        ItemStack itemStack = Materials.SPAWNER.toBukkitItem(1);

        if (spawnerUpgrade != null && !spawnerUpgrade.isDefault()) {
            itemStack = ItemUtils.setSpawnerUpgrade(itemStack, spawnerUpgrade.getId());
        }

        int perStackAmount = amount;

        if (plugin.getSettings().getSpawners().isDropStackedItemEnabled()) {
            itemStack.setAmount(1);
            itemStack = ItemUtils.setSpawnerItemAmount(itemStack, amount);
        } else {
            itemStack.setAmount(amount);
            perStackAmount = 1;
        }

        ItemMeta itemMeta = itemStack.getItemMeta();

        try {
            BlockStateMeta blockStateMeta = (BlockStateMeta) itemMeta;
            CreatureSpawner creatureSpawner = (CreatureSpawner) blockStateMeta.getBlockState();

            creatureSpawner.setSpawnedType(entityType);
            blockStateMeta.setBlockState(creatureSpawner);
        } catch (Throwable ignored) {
        }

        String customName = plugin.getSettings().getSpawners().getItemName();
        if (!customName.equals("")) {
            itemMeta.setDisplayName(customName.replace("{0}", perStackAmount + "")
                    .replace("{1}", EntityUtils.getFormattedType(entityType.name()))
                    .replace("{2}", spawnerUpgrade == null ? "" : spawnerUpgrade.getDisplayName()));
        }

        List<String> customLore = plugin.getSettings().getSpawners().getItemLore();
        if (!customLore.isEmpty()) {
            List<String> lore = new ArrayList<>();
            for (String line : customLore)
                lore.add(line.replace("{0}", perStackAmount + "")
                        .replace("{1}", EntityUtils.getFormattedType(entityType.name())));
            itemMeta.setLore(lore);
        }

        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    @Override
    public EntityType getSpawnerType(ItemStack itemStack) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        EntityType spawnType = null;

        try {
            BlockStateMeta blockStateMeta = (BlockStateMeta) itemMeta;
            spawnType = ((CreatureSpawner) blockStateMeta.getBlockState()).getSpawnedType();
        } catch (Throwable ignored) {
        }

        if ((spawnType == EntityType.PIG || spawnType == null) && itemMeta.hasDisplayName()) {
            String displayName = itemMeta.getDisplayName();
            Matcher matcher = plugin.getSettings().getSpawners().getSpawnersPattern().matcher(displayName);
            if (matcher.matches()) {
                List<String> indexes = Stream.of("0", "1", "2")
                        .sorted(Comparator.comparingInt(o -> displayName.indexOf("{" + o + "}"))).collect(Collectors.toList());
                try {
                    spawnType = EntityType.valueOf(matcher.group(indexes.indexOf("1") + 1).toUpperCase().replace(" ", "_"));
                } catch (Exception ignored) {
                }
            }
        }

        return spawnType == null ? EntityType.PIG : spawnType;
    }

    @Override
    public void handleSpawnerExplode(StackedSpawner stackedSpawner, Entity entity, Player ignite, int brokenAmount) {
        SettingsManager.Spawners spawners = plugin.getSettings().getSpawners();

        if (!spawners.isExplosionDropEnabled() ||
                (!spawners.getExplosionWorlds().isEmpty() &&
                        !spawners.getExplosionWorlds().contains(stackedSpawner.getWorld().getName())))
            return;

        if (spawners.getExplosionBreakChance() >= 100 ||
                ThreadLocalRandom.current().nextInt(100) < spawners.getExplosionBreakChance()) {
            _dropSpawner(stackedSpawner, ignite, brokenAmount, spawners.isExplosionDropToInventoryEnabled());
        }
    }

    @Override
    public void handleSpawnerBreak(StackedSpawner stackedSpawner, Player player, int brokenAmount, boolean breakMenu) {
        SettingsManager.Spawners spawners = plugin.getSettings().getSpawners();

        if (!breakMenu && (!spawners.isSilkTouchEnabled() ||
                (!spawners.getSilkTouchWorlds().isEmpty() &&
                        !spawners.getSilkTouchWorlds().contains(stackedSpawner.getWorld().getName()))))
            return;

        boolean success = spawners.getSilkTouchBreakChance() >= 100 ||
                ThreadLocalRandom.current().nextInt(100) < spawners.getSilkTouchBreakChance();

        boolean allowed =
                (spawners.isDropWithoutSilkEnabled() && player.hasPermission("wildstacker.nosilkdrop")) ||
                        (ItemUtils.isPickaxeAndHasSilkTouch(player.getInventory().getItemInHand()) &&
                                player.hasPermission("wildstacker.silktouch"));

        if (breakMenu || (success && allowed)) {
            dropSpawner(stackedSpawner, player, brokenAmount);
        }
    }

    @Override
    public void handleSpawnerPlace(CreatureSpawner creatureSpawner, ItemStack itemStack) {
        EntityType entityType = getSpawnerType(itemStack);
        creatureSpawner.setSpawnedType(entityType);
        creatureSpawner.update();
    }

    @Override
    public void dropSpawner(StackedSpawner stackedSpawner, Player player, int brokenAmount) {
        _dropSpawner(stackedSpawner, player, brokenAmount, plugin.getSettings().getSpawners().isDropToInventoryEnabled());
    }

    private void _dropSpawner(StackedSpawner stackedSpawner, Player player, int brokenAmount, boolean dropToInventory) {
        ItemStack dropItem = EventsCaller.callSpawnerDropEvent(stackedSpawner, player, brokenAmount);
        Location toDrop = ItemUtils.getSafeDropLocation(stackedSpawner.getLocation());

        if (dropToInventory && player != null) {
            ItemUtils.addItem(dropItem, player.getInventory(), toDrop);
        } else {
            ItemUtils.dropItem(dropItem, toDrop);
        }
    }
}
