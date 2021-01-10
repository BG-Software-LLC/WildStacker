package com.bgsoftware.wildstacker.menu;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.wildstacker.api.objects.StackedSpawner;
import com.bgsoftware.wildstacker.objects.WStackedSpawner;
import com.bgsoftware.wildstacker.utils.files.FileUtils;
import com.bgsoftware.wildstacker.utils.items.ItemBuilder;
import com.bgsoftware.wildstacker.utils.pair.Pair;
import com.bgsoftware.wildstacker.utils.spawners.SpawnerCachedData;
import com.bgsoftware.wildstacker.utils.spawners.SyncedCreatureSpawner;
import com.bgsoftware.wildstacker.utils.threads.Executor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class SpawnersManageMenu extends WildMenu {

    private static List<Integer> amountsMenuSlots = new ArrayList<>(), upgradeMenuSlots = new ArrayList<>();
    private static List<Pair<Integer, ItemBuilder>> statisticSlots = new ArrayList<>();
    private final WeakReference<StackedSpawner> stackedSpawner;
    private final BukkitTask bukkitTask;

    private boolean onBuild = false;

    private SpawnersManageMenu(StackedSpawner stackedSpawner){
        super("manageMenu");
        this.stackedSpawner = new WeakReference<>(stackedSpawner);
        bukkitTask = Executor.timer(this::tick, 20L);
    }

    @Override
    public void onPlayerClick(InventoryClickEvent e) {
        StackedSpawner stackedSpawner = this.stackedSpawner.get();

        if(stackedSpawner == null){
            e.getWhoClicked().closeInventory();
            stop();
            return;
        }

        if(amountsMenuSlots.contains(e.getRawSlot())){
            if(plugin.getSettings().amountsMenuEnabled) {
                SpawnerAmountsMenu.open((Player) e.getWhoClicked(), stackedSpawner);
            }
        }
        else if(upgradeMenuSlots.contains(e.getRawSlot())){
            if(plugin.getSettings().amountsMenuEnabled) {
                SpawnerUpgradeMenu.open((Player) e.getWhoClicked(), stackedSpawner);
            }
        }
    }

    @Override
    public void onMenuClose(InventoryCloseEvent e) {

    }

    public void onInventoryBuild(){
        onBuild = true;
        tick();
        onBuild = false;
    }

    public void tick(){
        if(inventory == null || (!onBuild && inventory.getViewers().isEmpty()))
            return;

        StackedSpawner stackedSpawner = this.stackedSpawner.get();

        if(stackedSpawner == null){
            stop();
            return;
        }

        SyncedCreatureSpawner creatureSpawner = (SyncedCreatureSpawner) stackedSpawner.getSpawner();
        SpawnerCachedData spawnerData = creatureSpawner.readData();

        for(Pair<Integer, ItemBuilder> statisticItem : statisticSlots){
            inventory.setItem(statisticItem.getKey(), statisticItem.getValue().copy()
                    .replaceAll("%min-spawn-delay%",  spawnerData.getMinSpawnDelay() + "")
                    .replaceAll("%max-spawn-delay%",  spawnerData.getMaxSpawnDelay() + "")
                    .replaceAll("%spawn-count%",  spawnerData.getSpawnCount() + "")
                    .replaceAll("%max-nearby-entities%",  spawnerData.getMaxNearbyEntities() + "")
                    .replaceAll("%player-range%",  spawnerData.getRequiredPlayerRange() + "")
                    .replaceAll("%required-player-range%",  spawnerData.getRequiredPlayerRange() + "")
                    .replaceAll("%spawn-range%",  spawnerData.getSpawnRange() + "")
                    .replaceAll("%ticks-left%", spawnerData.getTicksLeft() + "")
                    .replaceAll("%failure-reason%", spawnerData.getFailureReason())
                    .build());
        }
    }

    public void stop(){
        bukkitTask.cancel();
    }

    public static void open(Player player, StackedSpawner stackedSpawner){
        SpawnersManageMenu spawnersManageMenu = ((WStackedSpawner) stackedSpawner).getLinkedInventory();

        if(spawnersManageMenu == null) {
            spawnersManageMenu = new SpawnersManageMenu(stackedSpawner);
            ((WStackedSpawner) stackedSpawner).linkInventory(spawnersManageMenu);
        }

        spawnersManageMenu.openMenu(player);
    }

    public static void loadMenu(){
        SpawnersManageMenu spawnersManageMenu = new SpawnersManageMenu(null);

        File file = new File(plugin.getDataFolder(), "menus/spawner-manage.yml");

        if(!file.exists())
            FileUtils.saveResource("menus/spawner-manage.yml");

        CommentedConfiguration cfg = CommentedConfiguration.loadConfiguration(file);

        try {
            cfg.syncWithConfig(file, FileUtils.getResource("menus/spawner-manage.yml"), IGNORED_CONFIG_PATHS);
        }catch (Exception ex){
            ex.printStackTrace();
        }

        Map<Character, List<Integer>> charSlots = FileUtils.loadGUI(spawnersManageMenu, "spawner-manage.yml", cfg);

        amountsMenuSlots = getSlots(cfg, "amounts-menu", charSlots);
        upgradeMenuSlots = getSlots(cfg, "upgrade-menu", charSlots);
        statisticSlots = new ArrayList<>();

        for(Character character : cfg.getString("statistics", "").toCharArray()) {
            for(int slot : charSlots.getOrDefault(character, new ArrayList<>())){
                statisticSlots.add(new Pair<>(slot, spawnersManageMenu.getData().fillItems.get(slot)));
            }
        }
    }

}
