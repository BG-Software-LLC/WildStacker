package com.bgsoftware.wildstacker.menu;

import com.bgsoftware.wildstacker.Locale;
import com.bgsoftware.wildstacker.api.enums.UnstackResult;
import com.bgsoftware.wildstacker.api.objects.StackedSpawner;
import com.bgsoftware.wildstacker.hooks.CoreProtectHook;
import com.bgsoftware.wildstacker.hooks.EconomyHook;
import com.bgsoftware.wildstacker.hooks.PluginHooks;
import com.bgsoftware.wildstacker.key.Key;
import com.bgsoftware.wildstacker.objects.WStackedSpawner;
import com.bgsoftware.wildstacker.utils.entity.EntityUtils;
import com.bgsoftware.wildstacker.utils.items.ItemUtils;
import com.bgsoftware.wildstacker.utils.legacy.Materials;
import com.bgsoftware.wildstacker.utils.pair.Pair;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class SpawnersBreakMenu extends WildMenu {

    private static final Map<Integer, Integer> breakSlots = new HashMap<>();
    private static final Map<UUID, Location> clickedSpawners = new HashMap<>();
    private static Inventory inventory;

    @Override
    public void onButtonClick(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();

        e.setCancelled(true);

        if(!clickedSpawners.containsKey(player.getUniqueId())){
            player.closeInventory();
            return;
        }

        if(!breakSlots.containsKey(e.getRawSlot()))
            return;

        int removeAmount = breakSlots.get(e.getRawSlot());

        Block spawnerBlock = clickedSpawners.get(player.getUniqueId()).getBlock();
        StackedSpawner stackedSpawner;

        try {
            stackedSpawner = WStackedSpawner.of(spawnerBlock);
        }catch(IllegalArgumentException ex){
            //IllegelArgumentException can be thrown if the block no longer exist.
            player.closeInventory();
            return;
        }


        removeAmount = Math.min(stackedSpawner.getStackAmount(), removeAmount);

        Pair<Double, Boolean> chargeInfo = plugin.getSettings().spawnersBreakCharge.getOrDefault(
                Key.of(stackedSpawner.getSpawnedType().name()), new Pair<>(0.0, false));

        double amountToCharge = chargeInfo.getKey() * (chargeInfo.getValue() ? removeAmount : 1);

        if (PluginHooks.isVaultEnabled && EconomyHook.getMoneyInBank(player) < amountToCharge) {
            Locale.SPAWNER_BREAK_NOT_ENOUGH_MONEY.send(player, amountToCharge);
            e.setCancelled(true);
            return;
        }

        if(stackedSpawner.runUnstack(removeAmount) == UnstackResult.SUCCESS){
            CoreProtectHook.recordBlockChange(player, spawnerBlock, false);

            if(amountToCharge > 0)
                EconomyHook.withdrawMoney(player, amountToCharge);

            plugin.getProviders().dropOrGiveItem(player, stackedSpawner.getSpawner(), removeAmount, true);

            if(stackedSpawner.getStackAmount() <= 0)
                spawnerBlock.setType(Material.AIR);

            if(spawnerBlock.getType() == Materials.SPAWNER.toBukkitType())
                Locale.SPAWNER_BREAK.send(player, EntityUtils.getFormattedType(stackedSpawner.getSpawnedType().name()), stackedSpawner.getStackAmount(), amountToCharge);

            player.closeInventory();
        }
    }


    @Override
    public void onMenuClose(InventoryCloseEvent e) {
        clickedSpawners.remove(e.getPlayer().getUniqueId());
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public static void open(Player player, Location location){
        clickedSpawners.put(player.getUniqueId(), location);
        player.openInventory(inventory);
    }

    public static void loadMenu(ConfigurationSection section){
        String title = ChatColor.translateAlternateColorCodes('&', section.getString("title", "&lBreak Menu"));
        inventory = Bukkit.createInventory(new SpawnersBreakMenu(), 9 * section.getInt("rows", 3), title);

        if(section.contains("fill-items")){
            for(String key : section.getConfigurationSection("fill-items").getKeys(false)){
                ItemStack itemStack = ItemUtils.getFromConfig(section.getConfigurationSection("fill-items." + key));
                for(String slot : section.getString("fill-items." + key + ".slots").split(",")){
                    inventory.setItem(Integer.parseInt(slot), itemStack);
                }
            }
        }

        if(section.contains("break-slots")){
            for(String slot : section.getConfigurationSection("break-slots").getKeys(false)){
                ItemStack itemStack = ItemUtils.getFromConfig(section.getConfigurationSection("break-slots." + slot));
                inventory.setItem(Integer.parseInt(slot), itemStack);
                breakSlots.put(Integer.valueOf(slot), section.getInt("break-slots." + slot + ".amount"));
            }
        }
    }

}
