package com.bgsoftware.wildstacker.hooks;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.objects.StackedSpawner;
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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class SpawnersProvider_Default implements SpawnersProvider {

    private final WildStackerPlugin plugin = WildStackerPlugin.getPlugin();

    public SpawnersProvider_Default(){
        WildStackerPlugin.log(" - Couldn't find any spawners providers, using default one.");
    }

    @Override
    public ItemStack getSpawnerItem(EntityType entityType, int amount, String upgradeDisplayName) {
        ItemStack itemStack = Materials.SPAWNER.toBukkitItem(amount);
        int perStackAmount = amount;

        if(plugin.getSettings().getStackedItem) {
            itemStack.setAmount(1);
            itemStack = ItemUtils.setSpawnerItemAmount(itemStack, amount);
        }
        else{
            perStackAmount = 1;
        }

        ItemMeta itemMeta = itemStack.getItemMeta();

        try {
            BlockStateMeta blockStateMeta = (BlockStateMeta) itemMeta;
            CreatureSpawner creatureSpawner = (CreatureSpawner) blockStateMeta.getBlockState();

            creatureSpawner.setSpawnedType(entityType);

            blockStateMeta.setBlockState(creatureSpawner);
        }catch(Throwable ignored){}

        String customName = plugin.getSettings().spawnerItemName;

        if(!customName.equals(""))
            itemMeta.setDisplayName(customName.replace("{0}", perStackAmount + "")
                    .replace("{1}", EntityUtils.getFormattedType(entityType.name()))
                    .replace("{2}", upgradeDisplayName));

        List<String> customLore = plugin.getSettings().spawnerItemLore;

        if(!customLore.isEmpty()){
            List<String> lore = new ArrayList<>();
            for(String line : customLore)
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
        EntityType spawnType = EntityType.UNKNOWN;

        try {
            BlockStateMeta blockStateMeta = (BlockStateMeta) itemMeta;
            spawnType = ((CreatureSpawner) blockStateMeta.getBlockState()).getSpawnedType();
        }catch(Throwable ignored){}

        if((spawnType == EntityType.PIG || spawnType == EntityType.UNKNOWN) && itemMeta.hasDisplayName()){
            String displayName = itemMeta.getDisplayName();
            Matcher matcher = plugin.getSettings().SPAWNERS_PATTERN.matcher(displayName);
            if(matcher.matches()) {
                List<String> indexes = Stream.of("0", "1", "2")
                        .sorted(Comparator.comparingInt(o -> displayName.indexOf("{" + o + "}"))).collect(Collectors.toList());
                try {
                    spawnType = EntityType.valueOf(matcher.group(indexes.indexOf("1") + 1).toUpperCase().replace(" ", "_"));
                }catch(Exception ex){
                    spawnType = EntityType.PIG;
                }
            }
        }

        return spawnType;
    }

    @Override
    public void handleSpawnerExplode(StackedSpawner stackedSpawner, Entity entity, Player ignite, int brokenAmount) {
        if(!plugin.getSettings().explosionsDropSpawner || (!plugin.getSettings().explosionsWorlds.isEmpty() &&
                !plugin.getSettings().explosionsWorlds.contains(stackedSpawner.getWorld().getName())))
            return;

        if(plugin.getSettings().explosionsBreakChance >= 100 || ThreadLocalRandom.current().nextInt(100) < plugin.getSettings().explosionsBreakChance)
            dropSpawner(stackedSpawner, ignite, brokenAmount);
    }

    @Override
    public void handleSpawnerBreak(StackedSpawner stackedSpawner, Player player, int brokenAmount, boolean breakMenu) {
        if(!breakMenu && (!plugin.getSettings().silkTouchSpawners || (!plugin.getSettings().silkWorlds.isEmpty() &&
                !plugin.getSettings().silkWorlds.contains(stackedSpawner.getWorld().getName()))))
            return;

        if(breakMenu || (
                (plugin.getSettings().silkTouchBreakChance >= 100 || ThreadLocalRandom.current().nextInt(100) < plugin.getSettings().silkTouchBreakChance) &&
                ((plugin.getSettings().dropSpawnerWithoutSilk && player.hasPermission("wildstacker.nosilkdrop")) ||
                (ItemUtils.isPickaxeAndHasSilkTouch(player.getInventory().getItemInHand()) && player.hasPermission("wildstacker.silktouch"))))) {
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
        ItemStack dropItem = EventsCaller.callSpawnerDropEvent(stackedSpawner, player, brokenAmount);
        Location toDrop = ItemUtils.getSafeDropLocation(stackedSpawner.getLocation());

        if (plugin.getSettings().dropToInventory && player != null) {
            ItemUtils.addItem(dropItem, player.getInventory(), toDrop);
        } else {
            ItemUtils.dropItem(dropItem, toDrop);
        }
    }
}
