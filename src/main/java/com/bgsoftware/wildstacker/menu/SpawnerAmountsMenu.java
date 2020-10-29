package com.bgsoftware.wildstacker.menu;

import com.bgsoftware.wildstacker.Locale;
import com.bgsoftware.wildstacker.api.events.SpawnerPlaceInventoryEvent;
import com.bgsoftware.wildstacker.api.objects.StackedSpawner;
import com.bgsoftware.wildstacker.config.CommentedConfiguration;
import com.bgsoftware.wildstacker.listeners.SpawnersListener;
import com.bgsoftware.wildstacker.utils.files.FileUtils;
import com.bgsoftware.wildstacker.utils.files.SoundWrapper;
import com.bgsoftware.wildstacker.utils.items.ItemUtils;
import com.bgsoftware.wildstacker.utils.legacy.Materials;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class SpawnerAmountsMenu extends WildMenu {

    private static Map<Integer, Integer> depositSpawners = new HashMap<>(), withdrawSpawners = new HashMap<>();
    private static SoundWrapper successSound, failureSound;

    private final WeakReference<StackedSpawner> stackedSpawner;

    private SpawnerAmountsMenu(StackedSpawner stackedSpawner){
        super("amountsMenu");
        this.stackedSpawner = new WeakReference<>(stackedSpawner);
    }

    @Override
    public void onPlayerClick(InventoryClickEvent e) {
        StackedSpawner stackedSpawner = this.stackedSpawner.get();

        if(stackedSpawner == null){
            e.getWhoClicked().closeInventory();
            return;
        }

        if(handleBreak(e, stackedSpawner))
            return;

        handlePlace(e, stackedSpawner);
    }

    private boolean handleBreak(InventoryClickEvent e, StackedSpawner stackedSpawner){
        Integer withdrawAmount = withdrawSpawners.get(e.getRawSlot());

        if(withdrawAmount == null)
            return false;

        withdrawAmount = Math.min(stackedSpawner.getStackAmount(), withdrawAmount);

        if(withdrawAmount <= 0){
            if(failureSound != null)
                failureSound.playSound(e.getWhoClicked());
            return true;
        }

        SoundWrapper soundToPlay = SpawnersListener.handleSpawnerBreak(plugin, stackedSpawner, withdrawAmount,
                (Player) e.getWhoClicked(), true) ? successSound : failureSound;

        if(soundToPlay != null)
            soundToPlay.playSound(e.getWhoClicked());

        if(stackedSpawner.getStackAmount() <= 0)
            e.getWhoClicked().closeInventory();

        return true;
    }

    private void handlePlace(InventoryClickEvent e, StackedSpawner stackedSpawner){
        Integer depositAmount = depositSpawners.get(e.getRawSlot());

        if (depositAmount == null)
            return;

        int newStackAmount = stackedSpawner.getStackAmount() + depositAmount;
        int limit = stackedSpawner.getStackLimit();

        if(newStackAmount > limit)
            depositAmount = newStackAmount - limit;

        Map<Integer, Integer> itemsToRemove = new HashMap<>();
        Inventory inventory = e.getWhoClicked().getInventory();

        if(e.getWhoClicked().getGameMode() != GameMode.CREATIVE) {
            int amount = 0;

            for (int i = 0; i < inventory.getSize(); i++) {
                ItemStack itemStack = inventory.getItem(i);

                if (itemStack == null || itemStack.getType() != Materials.SPAWNER.toBukkitType() ||
                        plugin.getProviders().getSpawnerType(itemStack) != stackedSpawner.getSpawnedType())
                    continue;

                int itemAmount = ItemUtils.getSpawnerItemAmount(itemStack);

                int itemsToAdd = (depositAmount - amount) / itemAmount;

                if(itemsToAdd <= 0)
                    continue;

                itemsToRemove.put(i, itemsToAdd);
                amount += (itemAmount * itemsToAdd);

                if (amount >= depositAmount)
                    break;
            }

            depositAmount = Math.min(depositAmount, amount);
            newStackAmount = stackedSpawner.getStackAmount() + depositAmount;
        }

        if(depositAmount <= 0){
            if(failureSound != null)
                failureSound.playSound(e.getWhoClicked());
            return;
        }

        SpawnerPlaceInventoryEvent spawnerPlaceInventoryEvent = new SpawnerPlaceInventoryEvent(
                (Player) e.getWhoClicked(), stackedSpawner, depositAmount);
        Bukkit.getPluginManager().callEvent(spawnerPlaceInventoryEvent);

        if(spawnerPlaceInventoryEvent.isCancelled()) {
            if(failureSound != null)
                failureSound.playSound(e.getWhoClicked());
            return;
        }

        stackedSpawner.setStackAmount(newStackAmount, true);
        Locale.SPAWNER_UPDATE.send(e.getWhoClicked(), stackedSpawner.getStackAmount());

        if(successSound != null)
            successSound.playSound(e.getWhoClicked());

        // Remove items from inventory
        for(Map.Entry<Integer, Integer> entry : itemsToRemove.entrySet()){
            ItemStack currentItem = inventory.getItem(entry.getKey());
            if(currentItem.getAmount() == entry.getValue())
                inventory.setItem(entry.getKey(), new ItemStack(Material.AIR));
            else
                currentItem.setAmount(currentItem.getAmount() - entry.getValue());
        }
    }

    @Override
    public void onMenuClose(InventoryCloseEvent e) {

    }

    public static void open(Player player, StackedSpawner stackedSpawner){
        new SpawnerAmountsMenu(stackedSpawner).openMenu(player);
    }

    public static void loadMenu(){
        SpawnerAmountsMenu spawnerAmountsMenu = new SpawnerAmountsMenu(null);

        File file = new File(plugin.getDataFolder(), "menus/spawner-amounts.yml");

        if(!file.exists())
            FileUtils.saveResource("menus/spawner-amounts.yml");

        CommentedConfiguration cfg = CommentedConfiguration.loadConfiguration(file);
        cfg.syncWithConfig(file, FileUtils.getResource("menus/spawner-amounts.yml"), IGNORED_CONFIG_PATHS);

        Map<Character, List<Integer>> charSlots = FileUtils.loadGUI(spawnerAmountsMenu, "spawner-amounts.yml", cfg);
        Map<Integer, Integer> depositSpawners = new HashMap<>();
        Map<Integer, Integer> withdrawSpawners = new HashMap<>();

        if(!charSlots.isEmpty()) {
            for (String itemChar : cfg.getConfigurationSection("items").getKeys(false)) {
                if(cfg.contains("items." + itemChar + ".deposit")){
                    List<Integer> slots = charSlots.get(itemChar.toCharArray()[0]);
                    if(slots != null)
                        slots.forEach(slot -> depositSpawners.put(slot, cfg.getInt("items." + itemChar + ".deposit")));
                }
                else if(cfg.contains("items." + itemChar + ".withdraw")){
                    List<Integer> slots = charSlots.get(itemChar.toCharArray()[0]);
                    if(slots != null)
                        slots.forEach(slot -> withdrawSpawners.put(slot, cfg.getInt("items." + itemChar + ".withdraw")));
                }
            }
        }

        SpawnerAmountsMenu.depositSpawners = depositSpawners;
        SpawnerAmountsMenu.withdrawSpawners = withdrawSpawners;
        SpawnerAmountsMenu.successSound = FileUtils.getSound(cfg.getConfigurationSection("success-sound"));
        SpawnerAmountsMenu.failureSound = FileUtils.getSound(cfg.getConfigurationSection("failure-sound"));
    }

}
