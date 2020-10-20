package com.bgsoftware.wildstacker.menu;

import com.bgsoftware.wildstacker.api.objects.StackedSpawner;
import com.bgsoftware.wildstacker.objects.WStackedSpawner;
import com.bgsoftware.wildstacker.utils.files.FileUtils;
import com.bgsoftware.wildstacker.utils.items.ItemBuilder;
import com.bgsoftware.wildstacker.utils.pair.Pair;
import com.bgsoftware.wildstacker.utils.spawners.SyncedCreatureSpawner;
import com.bgsoftware.wildstacker.utils.threads.Executor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.scheduler.BukkitTask;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class SpawnersManageMenu extends WildMenu {

    private static List<Integer> depositMenuSlots = new ArrayList<>();
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
        if(depositMenuSlots.contains(e.getRawSlot())){
            StackedSpawner stackedSpawner = this.stackedSpawner.get();
            if(stackedSpawner != null) {
                SpawnersBreakMenu.open((Player) e.getWhoClicked(), stackedSpawner.getLocation());
            }
            else {
                e.getWhoClicked().closeInventory();
                stop();
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

        int requiredPlayerRange = creatureSpawner.getRequiredPlayerRange();
        int ticksLeft = creatureSpawner.getDelay() / 20;

        for(Pair<Integer, ItemBuilder> statisticItem : statisticSlots){
            inventory.setItem(statisticItem.getKey(), statisticItem.getValue().copy()
                    .replaceAll("%player-range%",  requiredPlayerRange + "")
                    .replaceAll("%ticks-left%", ticksLeft + "")
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

    public static void loadMenu(ConfigurationSection section){
        SpawnersManageMenu spawnersManageMenu = new SpawnersManageMenu(null);
        Map<Character, List<Integer>> charSlots = FileUtils.loadGUI(spawnersManageMenu, "manage-menu", section);
        List<Integer> depositMenuSlots = new ArrayList<>();
        List<Pair<Integer, ItemBuilder>> statisticSlots = new ArrayList<>();

        for(Character character : section.getString("deposit-menu", "").toCharArray())
            depositMenuSlots.addAll(charSlots.getOrDefault(character, new ArrayList<>()));

        for(Character character : section.getString("statistics", "").toCharArray()) {
            for(int slot : charSlots.getOrDefault(character, new ArrayList<>())){
                statisticSlots.add(new Pair<>(slot, spawnersManageMenu.getData().fillItems.get(slot)));
            }
        }

        SpawnersManageMenu.depositMenuSlots = depositMenuSlots;
        SpawnersManageMenu.statisticSlots = statisticSlots;
    }

}
