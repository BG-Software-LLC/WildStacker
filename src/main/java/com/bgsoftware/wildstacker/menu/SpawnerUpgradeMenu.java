package com.bgsoftware.wildstacker.menu;

import com.bgsoftware.wildstacker.Locale;
import com.bgsoftware.wildstacker.api.objects.StackedSpawner;
import com.bgsoftware.wildstacker.api.upgrades.SpawnerUpgrade;
import com.bgsoftware.wildstacker.config.CommentedConfiguration;
import com.bgsoftware.wildstacker.hooks.EconomyHook;
import com.bgsoftware.wildstacker.hooks.PluginHooks;
import com.bgsoftware.wildstacker.utils.files.FileUtils;
import com.bgsoftware.wildstacker.utils.files.SoundWrapper;
import com.bgsoftware.wildstacker.utils.items.ItemBuilder;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class SpawnerUpgradeMenu extends WildMenu {

    private static List<Integer> currentUpgradeSlots = new ArrayList<>(), nextUpgradeSlots = new ArrayList<>();
    private static SoundWrapper successSound, failureSound;

    private final WeakReference<StackedSpawner> stackedSpawner;

    private SpawnerUpgradeMenu(StackedSpawner stackedSpawner){
        super("upgradeMenu");
        this.stackedSpawner = new WeakReference<>(stackedSpawner);
    }

    @Override
    public void onPlayerClick(InventoryClickEvent e) {
        StackedSpawner stackedSpawner = this.stackedSpawner.get();

        if(stackedSpawner == null){
            e.getWhoClicked().closeInventory();
            return;
        }

        if(!nextUpgradeSlots.contains(e.getRawSlot()))
            return;

        Player player = (Player) e.getWhoClicked();

        SpawnerUpgrade nextUpgrade = stackedSpawner.getUpgrade().getNextUpgrade();

        if(nextUpgrade == null){
            if(failureSound != null)
                failureSound.playSound(player);
            return;
        }

        double upgradeCost = nextUpgrade.getCost();

        if (upgradeCost > 0 && PluginHooks.isVaultEnabled && EconomyHook.getMoneyInBank(player) < upgradeCost) {
            Locale.SPAWNER_UPGRADE_NOT_ENOUGH_MONEY.send(player, upgradeCost);
            if(failureSound != null)
                failureSound.playSound(player);
            return;
        }

        stackedSpawner.setUpgrade(nextUpgrade);

        if(successSound != null)
            successSound.playSound(player);

        Locale.SPAWNER_UPGRADE_SUCCESS.send(player);

        if(upgradeCost > 0)
            EconomyHook.withdrawMoney(player, upgradeCost);

        open(player, stackedSpawner);
    }

    @Override
    public void onMenuClose(InventoryCloseEvent e) {

    }

    @Override
    protected Inventory buildInventory() {
        Inventory inventory = super.buildInventory();
        StackedSpawner stackedSpawner = this.stackedSpawner.get();

        if(stackedSpawner != null){
            SpawnerUpgrade spawnerUpgrade = stackedSpawner.getUpgrade();
            {
                ItemStack rawCurrentIcon = spawnerUpgrade.getIcon();
                ItemStack currentIcon = new ItemBuilder(rawCurrentIcon)
                        .withName("&6Current Upgrade: &7" + rawCurrentIcon.getItemMeta().getDisplayName())
                        .build();
                currentUpgradeSlots.forEach(slot -> inventory.setItem(slot, currentIcon));
            }

            SpawnerUpgrade nextUpgrade = spawnerUpgrade.getNextUpgrade();
            if(nextUpgrade != null){
                ItemStack rawNextIcon = nextUpgrade.getIcon();
                ItemStack nextIcon = new ItemBuilder(rawNextIcon)
                        .withName("&6Next Upgrade: &7" + rawNextIcon.getItemMeta().getDisplayName())
                        .build();
                nextUpgradeSlots.forEach(slot -> inventory.setItem(slot, nextIcon));
            }
        }

        return inventory;
    }

    public static void open(Player player, StackedSpawner stackedSpawner){
        new SpawnerUpgradeMenu(stackedSpawner).openMenu(player);
    }

    public static void loadMenu(){
        SpawnerUpgradeMenu spawnerUpgradeMenu = new SpawnerUpgradeMenu(null);

        File file = new File(plugin.getDataFolder(), "menus/spawner-upgrade.yml");

        if(!file.exists())
            FileUtils.saveResource("menus/spawner-upgrade.yml");

        CommentedConfiguration cfg = CommentedConfiguration.loadConfiguration(file);
        cfg.syncWithConfig(file, FileUtils.getResource("menus/spawner-upgrade.yml"), IGNORED_CONFIG_PATHS);

        Map<Character, List<Integer>> charSlots = FileUtils.loadGUI(spawnerUpgradeMenu, "spawner-upgrade.yml", cfg);

        currentUpgradeSlots = getSlots(cfg, "current-upgrade", charSlots);
        nextUpgradeSlots = getSlots(cfg, "next-upgrade", charSlots);

        successSound = FileUtils.getSound(cfg.getConfigurationSection("success-sound"));
        failureSound = FileUtils.getSound(cfg.getConfigurationSection("failure-sound"));
    }

}
