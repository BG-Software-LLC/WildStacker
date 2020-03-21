package com.bgsoftware.wildstacker.hooks;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.events.SpawnerDropEvent;
import com.bgsoftware.wildstacker.api.objects.StackedSpawner;
import com.bgsoftware.wildstacker.objects.WStackedSpawner;
import com.bgsoftware.wildstacker.utils.items.ItemUtils;
import com.bgsoftware.wildstacker.utils.threads.Executor;
import org.bukkit.Bukkit;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class SpawnersProvider_Default implements SpawnersProvider {

    private WildStackerPlugin plugin = WildStackerPlugin.getPlugin();

    public SpawnersProvider_Default(){
        WildStackerPlugin.log(" - Couldn't find any spawners providers, using default one.");
    }

    @Override
    public ItemStack getSpawnerItem(CreatureSpawner spawner, int amount) {
        //In order to make sure the creature spawner is updated, I just get a new instance of it.
        return ItemUtils.getSpawnerItem(spawner.getSpawnedType(), amount);
    }

    @Override
    public void dropOrGiveItem(Entity entity, CreatureSpawner spawner, int amount, UUID explodeSource) {
        boolean drop = true;
        if (plugin.getSettings().explosionsDropSpawner && entity instanceof TNTPrimed) {
            Entity igniter = ((TNTPrimed) entity).getSource();
            if (igniter instanceof Player) {
                Player sourcePlayer = (Player) igniter;
                drop = sourcePlayer.hasPermission("wildstacker.silktouch");
            }
        }

        if(drop)
            dropOrGiveItem(explodeSource == null ? null : Bukkit.getPlayer(explodeSource), spawner, amount, explodeSource != null);
    }

    @Override
    public void dropOrGiveItem(Player player, CreatureSpawner spawner, int amount, boolean isExplodeSource) {
        //If player is null, it broke by an explosion.
        if(player == null){
            SpawnerDropEvent spawnerDropEvent = new SpawnerDropEvent(WStackedSpawner.of(spawner), player, getSpawnerItem(spawner, amount));
            Bukkit.getPluginManager().callEvent(spawnerDropEvent);
            Executor.sync(() -> ItemUtils.dropItem(spawnerDropEvent.getItemStack(), spawner.getLocation()), 5L);
            return;
        }

        if(!isExplodeSource && (!plugin.getSettings().silkTouchSpawners ||
                (!plugin.getSettings().silkWorlds.isEmpty() && !plugin.getSettings().silkWorlds.contains(spawner.getWorld().getName()))))
            return;

        if(isExplodeSource || ThreadLocalRandom.current().nextInt(100) < plugin.getSettings().silkTouchBreakChance) {
            if (isExplodeSource || (plugin.getSettings().dropSpawnerWithoutSilk && player.hasPermission("wildstacker.nosilkdrop")) ||
                    (isValidAndHasSilkTouch(player.getInventory().getItemInHand()) && player.hasPermission("wildstacker.silktouch"))) {
                SpawnerDropEvent spawnerDropEvent = new SpawnerDropEvent(WStackedSpawner.of(spawner), player, getSpawnerItem(spawner, amount));
                Bukkit.getPluginManager().callEvent(spawnerDropEvent);
                if (plugin.getSettings().dropToInventory) {
                    ItemUtils.addItem(spawnerDropEvent.getItemStack(), player.getInventory(), spawner.getLocation());
                } else {
                    ItemUtils.dropItem(spawnerDropEvent.getItemStack(), spawner.getLocation());
                }
            }
        }
    }

    @Override
    public void setSpawnerType(CreatureSpawner spawner, ItemStack itemStack, boolean updateName) {
        BlockStateMeta blockStateMeta = (BlockStateMeta) itemStack.getItemMeta();
        EntityType entityType = ((CreatureSpawner) blockStateMeta.getBlockState()).getSpawnedType();

        spawner.setSpawnedType(entityType);
        spawner.update();

        StackedSpawner stackedSpawner = WStackedSpawner.of(spawner);

        int spawnerItemAmount = Math.min(ItemUtils.getSpawnerItemAmount(itemStack), stackedSpawner.getStackLimit());

        stackedSpawner.setStackAmount(spawnerItemAmount, updateName);
    }

    @Override
    public EntityType getSpawnerType(ItemStack itemStack) {
        BlockStateMeta blockStateMeta = (BlockStateMeta) itemStack.getItemMeta();
        EntityType spawnType = ((CreatureSpawner) blockStateMeta.getBlockState()).getSpawnedType();

        if((spawnType == EntityType.PIG || spawnType == EntityType.UNKNOWN) && itemStack.getItemMeta().hasDisplayName()){
            String displayName = itemStack.getItemMeta().getDisplayName();
            Matcher matcher = plugin.getSettings().SPAWNERS_PATTERN.matcher(displayName);
            if(matcher.matches()) {
                List<String> indexes = Stream.of("0", "1", "2")
                        .sorted(Comparator.comparingInt(o -> displayName.indexOf("{" + o + "}"))).collect(Collectors.toList());
                spawnType = EntityType.valueOf(matcher.group(indexes.indexOf("1") + 1).toUpperCase().replace(" ", "_"));
            }
        }

        return spawnType;
    }

    private boolean isValidAndHasSilkTouch(ItemStack itemStack){
        if(itemStack == null || !itemStack.getType().name().contains("PICKAXE"))
            return false;

        return WildToolsHook.hasSilkTouch(itemStack) || itemStack.getEnchantmentLevel(Enchantment.SILK_TOUCH) >= 1;
    }

}
