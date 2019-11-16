package com.bgsoftware.wildstacker.hooks;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.events.SpawnerDropEvent;
import com.bgsoftware.wildstacker.api.objects.StackedSpawner;
import com.bgsoftware.wildstacker.objects.WStackedSpawner;
import com.bgsoftware.wildstacker.utils.items.ItemUtils;
import com.bgsoftware.wildstacker.utils.legacy.Materials;
import com.dnyferguson.mineablespawners.MineableSpawners;
import com.dnyferguson.mineablespawners.utils.Chat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public final class SpawnersProvider_MineableSpawners implements SpawnersProvider {

    private boolean compatibility, requirePerm, requireSilk, dropInInventory, enableLore;
    private List<String> itemLore, blacklistedWorlds;
    private String mobNameColor;
    private double dropChance;


    public SpawnersProvider_MineableSpawners(){
        WildStackerPlugin.log(" - Using MineableSpawners as SpawnersProvider.");
        MineableSpawners mineableSpawners = JavaPlugin.getPlugin(MineableSpawners.class);
        FileConfiguration config = mineableSpawners.getConfig();
        compatibility = config.getBoolean("enable-compatibility");
        requirePerm = config.getBoolean("mining.require-permission");
        requireSilk = config.getBoolean("mining.require-silktouch");
        dropInInventory = config.getBoolean("mining.drop-in-inventory");
        mobNameColor = config.getString("mob-name-color");
        itemLore = config.getStringList("lore");
        enableLore = config.getBoolean("enable-lore");
        dropChance = config.getDouble("mining.drop-chance");
        blacklistedWorlds = config.getStringList("blacklisted-worlds");
    }

    @Override
    public ItemStack getSpawnerItem(CreatureSpawner spawner, int amount) {
        ItemStack spawnerItem = new ItemStack(Materials.SPAWNER.toBukkitType(), amount);
        ItemMeta itemMeta = spawnerItem.getItemMeta();
        String mobType = spawner.getSpawnedType().toString().replace("_", " ");
        String mobFormatted = mobType.substring(0, 1).toUpperCase() + mobType.substring(1).toLowerCase();
        itemMeta.setDisplayName(Chat.format("&8[" + this.mobNameColor + "%mob% &7Spawner&8]".replace("%mob%", mobFormatted)));

        if (itemLore != null && this.enableLore) {
            List<String> newLore = itemLore.stream().map(line -> Chat.format(line).replace("%mob%", mobFormatted)).collect(Collectors.toList());
            itemMeta.setLore(newLore);
        }

        spawnerItem.setItemMeta(itemMeta);

        return spawnerItem;
    }

    @Override
    public void dropOrGiveItem(Entity entity, CreatureSpawner spawner, int amount, UUID explodeSource) {
        //There's no official support for explosions in MineableSpawners.
    }

    @Override
    public void dropOrGiveItem(Player player, CreatureSpawner spawner, int amount, boolean isExplodeSource) {
        if (blacklistedWorlds.contains(spawner.getWorld().getName())) {
            return;
        }

        if (this.requirePerm && !player.hasPermission("mineablespawners.break")) {
            return;
        }

        if (this.requireSilk && !player.getInventory().getItemInHand().containsEnchantment(Enchantment.SILK_TOUCH) &&
                !player.hasPermission("mineablespawners.nosilk")) {
            return;
        }

        if (dropChance != 1.0 && (dropChance <= 0 || Math.random() <= dropChance)) {
            return;
        }

        SpawnerDropEvent spawnerDropEvent = new SpawnerDropEvent(WStackedSpawner.of(spawner), player, getSpawnerItem(spawner, amount));
        Bukkit.getPluginManager().callEvent(spawnerDropEvent);

        if (!dropInInventory) {
            ItemUtils.addItem(spawnerDropEvent.getItemStack(), player.getInventory(), spawner.getLocation());
        }
        else{
            ItemUtils.dropItem(spawnerDropEvent.getItemStack(), spawner.getLocation());
        }
    }

    @Override
    public void setSpawnerType(CreatureSpawner spawner, ItemStack itemStack, boolean updateName) {
        EntityType entityType = getSpawnerType(itemStack);

        spawner.setSpawnedType(entityType);
        spawner.update();

        StackedSpawner stackedSpawner = WStackedSpawner.of(spawner);

        int spawnerItemAmount = Math.max(ItemUtils.getSpawnerItemAmount(itemStack), stackedSpawner.getStackLimit());

        stackedSpawner.setStackAmount(spawnerItemAmount, updateName);
    }

    @Override
    public EntityType getSpawnerType(ItemStack itemStack) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        EntityType entityType = null;

        if (this.compatibility) {
            try {
                entityType = EntityType.valueOf(itemMeta.getLore().toString().split(": §7")[1].split("]")[0].toUpperCase());
            } catch (Exception ignored) {}
        }

        if(entityType == null){
            String entityName = ChatColor.stripColor(itemMeta.getDisplayName()).split(" Spawner")[0].replace("[", "").replace(" ", "_").toUpperCase();
            entityType = EntityType.valueOf(entityName);
        }

        return entityType;
    }

}
