package com.bgsoftware.wildstacker.hooks;

import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.objects.StackedSpawner;
import com.bgsoftware.wildstacker.api.upgrades.SpawnerUpgrade;
import com.bgsoftware.wildstacker.objects.WStackedSpawner;
import com.bgsoftware.wildstacker.utils.events.EventsCaller;
import com.bgsoftware.wildstacker.utils.items.ItemUtils;
import com.bgsoftware.wildstacker.utils.legacy.Materials;
import com.bgsoftware.wildstacker.utils.threads.Executor;
import de.dustplanet.silkspawners.SilkSpawners;
import de.dustplanet.util.SilkUtil;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

@SuppressWarnings("SameParameterValue")
public final class SpawnersProvider_SilkSpawners implements SpawnersProvider {

    private static final ReflectMethod<Object> GET_SPAWNER_ENTITY_ID = new ReflectMethod<>(
            SilkUtil.class, "getSpawnerEntityID", Block.class);
    private static final ReflectMethod<Object> GET_STORED_SPAWNER_ITEM_ENTITY_ID = new ReflectMethod<>(
            SilkUtil.class, "getStoredSpawnerItemEntityID", ItemStack.class);
    private static final ReflectMethod<String> GET_CREATURE_NAME_SHORT = new ReflectMethod<>(
            SilkUtil.class, "getCreatureName", short.class);
    private static final ReflectMethod<ItemStack> NEW_SPAWNER_ITEM = new ReflectMethod<>(
            SilkUtil.class, "newSpawnerItem", short.class, String.class, int.class, boolean.class);

    private final SilkSpawners ss;
    private final SilkUtil silkUtil;

    public SpawnersProvider_SilkSpawners() {
        WildStackerPlugin.log(" - Using SilkSpawners as SpawnersProvider.");
        ss = JavaPlugin.getPlugin(SilkSpawners.class);
        silkUtil = SilkUtil.hookIntoSilkSpanwers();
    }

    @Override
    public ItemStack getSpawnerItem(EntityType entityType, int amount, SpawnerUpgrade spawnerUpgrade) {
        ItemStack itemStack;
        try {
            //noinspection deprecation
            String entityID = silkUtil.getDisplayNameToMobID().get(entityType.getName());
            itemStack = silkUtil.newSpawnerItem(entityID, silkUtil.getCustomSpawnerName(entityID), amount, false);
        } catch (Throwable ex) {
            try {
                //noinspection deprecation
                short entityID = entityType.getTypeId();
                itemStack = newSpawnerItem(entityID, getCreatureName(entityID), amount, false);
            } catch (Throwable err2) {
                throw new RuntimeException(ex);
            }
        }

        return spawnerUpgrade == null || spawnerUpgrade.isDefault() ? itemStack :
                ItemUtils.setSpawnerUpgrade(itemStack, spawnerUpgrade.getId());
    }

    @Override
    public EntityType getSpawnerType(ItemStack itemStack) {
        Object entityType = getStoredSpawnerItemEntityID(itemStack);
        //noinspection deprecation
        return entityType instanceof String ? EntityType.fromName((String) entityType) : EntityType.fromId((short) entityType);
    }

    @Override
    public void handleSpawnerExplode(StackedSpawner stackedSpawner, Entity entity, Player ignite, int brokenAmount) {
        if (stackedSpawner.getStackAmount() <= brokenAmount)
            brokenAmount = brokenAmount - 1;

        Object entityId = getSpawnerEntityID(stackedSpawner.getSpawner());
        int randomNumber = ThreadLocalRandom.current().nextInt(100), dropChance;

        if (ss.mobs.contains("creatures." + entityId + ".explosionDropChance")) {
            dropChance = ss.mobs.getInt("creatures." + entityId + ".explosionDropChance", 100);
        } else {
            dropChance = ss.config.getInt("explosionDropChance", 100);
        }

        if (randomNumber < dropChance)
            dropSpawner(stackedSpawner, ignite, brokenAmount);
    }

    @Override
    public void handleSpawnerBreak(StackedSpawner stackedSpawner, Player player, int brokenAmount, boolean breakMenu) {
        Object entityId = getSpawnerEntityID(stackedSpawner.getSpawner());
        String mobName = Objects.requireNonNull(getCreatureName(entityId)).toLowerCase().replace(" ", "");
        int randomNumber = ThreadLocalRandom.current().nextInt(100), dropChance;

        if (breakMenu || (silkUtil.isValidItemAndHasSilkTouch(player.getInventory().getItemInHand()) &&
                player.hasPermission("silkspawners.silkdrop." + mobName))) {
            if (ss.mobs.contains("creatures." + entityId + ".silkDropChance")) {
                dropChance = ss.mobs.getInt("creatures." + entityId + ".silkDropChance", 100);
            } else {
                dropChance = ss.config.getInt("silkDropChance", 100);
            }

            if (randomNumber < dropChance)
                dropSpawner(stackedSpawner, player, brokenAmount);
        }
    }

    @Override
    public void handleSpawnerPlace(CreatureSpawner creatureSpawner, ItemStack itemStack) {
        Block block = creatureSpawner.getBlock();

        Executor.sync(() -> {
            if (block.getType() != Materials.SPAWNER.toBukkitType())
                return;

            WStackedSpawner.of(block).updateName();
        }, 1L);
    }

    @Override
    public void dropSpawner(StackedSpawner stackedSpawner, Player player, int brokenAmount) {
        ItemStack dropItem = EventsCaller.callSpawnerDropEvent(stackedSpawner, player, brokenAmount);
        Location toDrop = ItemUtils.getSafeDropLocation(stackedSpawner.getLocation());

        if (ss.config.getBoolean("dropSpawnerToInventory", false) && player != null) {
            ItemUtils.addItem(dropItem, player.getInventory(), toDrop);
        } else {
            ItemUtils.dropItem(dropItem, toDrop);
        }
    }

    private Object getSpawnerEntityID(CreatureSpawner spawner) {
        Object entityId = GET_SPAWNER_ENTITY_ID.invoke(silkUtil, spawner.getBlock());
        return entityId == null || entityId.equals(0) ? silkUtil.getDefaultEntityID() : entityId;
    }

    private Object getStoredSpawnerItemEntityID(ItemStack itemStack) {
        Object entityId = GET_STORED_SPAWNER_ITEM_ENTITY_ID.invoke(silkUtil, itemStack);
        return entityId == null || entityId.equals(0) ? silkUtil.getDefaultEntityID() : entityId;
    }

    private String getCreatureName(Object entityId) {
        return GET_CREATURE_NAME_SHORT.isValid() ? GET_CREATURE_NAME_SHORT.invoke(entityId) : silkUtil.getCreatureName((String) entityId);
    }

    private ItemStack newSpawnerItem(Object entityId, String customName, int amount, boolean forceLore) {
        return NEW_SPAWNER_ITEM.isValid() ? NEW_SPAWNER_ITEM.invoke(silkUtil, entityId, customName, amount, forceLore) :
                silkUtil.newSpawnerItem((String) entityId, customName, amount, forceLore);
    }

}
