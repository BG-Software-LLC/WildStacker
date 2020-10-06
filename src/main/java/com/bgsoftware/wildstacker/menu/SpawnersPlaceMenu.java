package com.bgsoftware.wildstacker.menu;

import com.bgsoftware.wildstacker.Locale;
import com.bgsoftware.wildstacker.api.events.SpawnerPlaceInventoryEvent;
import com.bgsoftware.wildstacker.api.objects.StackedSpawner;
import com.bgsoftware.wildstacker.objects.WStackedSpawner;
import com.bgsoftware.wildstacker.utils.entity.EntityUtils;
import com.bgsoftware.wildstacker.utils.items.ItemUtils;
import com.bgsoftware.wildstacker.utils.legacy.Materials;
import com.bgsoftware.wildstacker.utils.threads.Executor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public final class SpawnersPlaceMenu extends WildMenu {

    private final Inventory inventory;
    private final Location location;

    private boolean closeFlag = false;

    private SpawnersPlaceMenu(Location location, EntityType spawnedType){
        this.location = location;
        this.inventory = Bukkit.createInventory(this, 9 * 4,
                plugin.getSettings().spawnersPlaceMenuTitle.replace("{0}", EntityUtils.getFormattedType(spawnedType.name())));
    }

    @Override
    public void onButtonClick(InventoryClickEvent e) {
        StackedSpawner stackedSpawner = WStackedSpawner.of(location.getBlock());

        ItemStack spawnerItem = null;

        if(e.getCurrentItem() != null && (e.getCurrentItem().getType() != Materials.SPAWNER.toBukkitType() ||
                plugin.getProviders().getSpawnerType(e.getCurrentItem()) != stackedSpawner.getSpawnedType())) {
            e.setCancelled(true);
            spawnerItem = e.getCurrentItem();
        }

        if(e.getAction() == InventoryAction.HOTBAR_SWAP) {
            e.setCancelled(true);
            spawnerItem = e.getWhoClicked().getInventory().getItem(e.getHotbarButton());
        }

        if(spawnerItem != null) {
            ItemStack SPAWNER_ITEM = spawnerItem;
            Executor.sync(() -> {
                if (closeFlag) {
                    for(ItemStack itemStack : e.getWhoClicked().getInventory().getContents()){
                        if(SPAWNER_ITEM.equals(itemStack))
                            return;
                    }

                    ItemUtils.addItem(SPAWNER_ITEM, e.getWhoClicked().getInventory(), stackedSpawner.getLocation());
                }
            }, 5L);
        }

    }

    @Override
    public void onMenuClose(InventoryCloseEvent e) {
        closeFlag = true;

        WStackedSpawner stackedSpawner = (WStackedSpawner) WStackedSpawner.of(location.getBlock());
        int amount = 0;

        for(ItemStack itemStack : e.getInventory().getContents()){
            if(itemStack != null) {
                if (itemStack.getType() == Materials.SPAWNER.toBukkitType() &&
                        plugin.getProviders().getSpawnerType(itemStack) == stackedSpawner.getSpawnedType())
                    amount += ItemUtils.getSpawnerItemAmount(itemStack) * itemStack.getAmount();
                else if (itemStack.getType() != Material.AIR)
                    ItemUtils.addItem(itemStack, e.getPlayer().getInventory(), stackedSpawner.getLocation());
            }
        }

        if(amount != 0) {
            int limit = stackedSpawner.getStackLimit();
            int newStackAmount = stackedSpawner.getStackAmount() + amount;

            if(stackedSpawner.getStackAmount() + amount > limit){
                ItemStack toAdd = plugin.getProviders().getSpawnerItem(stackedSpawner.getSpawner().getSpawnedType(),stackedSpawner.getStackAmount() + amount - limit);
                ItemUtils.addItem(toAdd, e.getPlayer().getInventory(), stackedSpawner.getLocation());
                newStackAmount = limit;
            }

            SpawnerPlaceInventoryEvent spawnerPlaceInventoryEvent = new SpawnerPlaceInventoryEvent((Player) e.getPlayer(), stackedSpawner, newStackAmount - stackedSpawner.getStackAmount());
            Bukkit.getPluginManager().callEvent(spawnerPlaceInventoryEvent);

            if(!spawnerPlaceInventoryEvent.isCancelled()) {
                stackedSpawner.setStackAmount(newStackAmount, true);
                Locale.SPAWNER_UPDATE.send(e.getPlayer(), stackedSpawner.getStackAmount());
            }
            else{
                ItemUtils.addItems(e.getInventory().getContents(), e.getPlayer().getInventory(), stackedSpawner.getLocation());
            }
        }

        stackedSpawner.unlinkInventory(e.getInventory());
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public static void open(Player player, StackedSpawner stackedSpawner){
        SpawnersPlaceMenu spawnersPlaceMenu = new SpawnersPlaceMenu(stackedSpawner.getLocation(), stackedSpawner.getSpawnedType());
        ((WStackedSpawner) stackedSpawner).linkInventory(spawnersPlaceMenu.inventory);
        player.openInventory(spawnersPlaceMenu.inventory);
    }

}
