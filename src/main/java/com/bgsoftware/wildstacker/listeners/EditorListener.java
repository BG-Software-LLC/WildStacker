package com.bgsoftware.wildstacker.listeners;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.handlers.EditorHandler;
import com.bgsoftware.wildstacker.utils.Executor;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("unused")
public final class EditorListener implements Listener {

    private WildStackerPlugin plugin;

    private Set<UUID> noResetClose = new HashSet<>();
    private Map<UUID, String> configValues = new HashMap<>();
    private Map<UUID, String> lastInventories = new HashMap<>();

    public EditorListener(WildStackerPlugin plugin){
        this.plugin = plugin;
    }

    /**
     * The following two events are here for patching a dupe glitch caused
     * by shift clicking and closing the inventory in the same time.
     */

    private Map<UUID, ItemStack> latestClickedItem = new HashMap<>();
    private String[] inventoryTitles = new String[] {WildStackerPlugin.getPlugin().getBreakMenuHandler().getTitle(), "Add items here",
            "WildStacker", "General Settings", "Items Settings", "Entities Settings", "Spawners Settings", "Barrels Settings"};

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClickMonitor(InventoryClickEvent e){
        if(e.getCurrentItem() != null && e.isCancelled() && Arrays.stream(inventoryTitles).anyMatch(title -> e.getView().getTitle().contains(title))) {
            latestClickedItem.put(e.getWhoClicked().getUniqueId(), e.getCurrentItem());
            Executor.sync(() -> latestClickedItem.remove(e.getWhoClicked().getUniqueId()), 20L);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryCloseMonitor(InventoryCloseEvent e){
        if(latestClickedItem.containsKey(e.getPlayer().getUniqueId())){
            ItemStack clickedItem = latestClickedItem.get(e.getPlayer().getUniqueId());
            Executor.sync(() -> {
                e.getPlayer().getInventory().removeItem(clickedItem);
                ((Player) e.getPlayer()).updateInventory();
            }, 1L);
        }
    }

    @EventHandler
    public void onEditorClick(InventoryClickEvent e){
        if(e.getInventory() == null)
            return;

        if(!(e.getWhoClicked() instanceof Player))
            return;

        Player player = (Player) e.getWhoClicked();

        if(e.getView().getTitle().equals("" + ChatColor.GOLD + ChatColor.BOLD + "WildStacker")){
            e.setCancelled(true);

            switch (e.getRawSlot()){
                case 20:
                    noResetClose.add(player.getUniqueId());
                    player.openInventory(plugin.getEditor().getGeneralEditor());
                    break;
                case 22:
                    noResetClose.add(player.getUniqueId());
                    player.openInventory(plugin.getEditor().getItemsEditor());
                    break;
                case 24:
                    noResetClose.add(player.getUniqueId());
                    player.openInventory(plugin.getEditor().getEntitiesEditor());
                    break;
                case 30:
                    noResetClose.add(player.getUniqueId());
                    player.openInventory(plugin.getEditor().getSpawnersEditor());
                    break;
                case 32:
                    noResetClose.add(player.getUniqueId());
                    player.openInventory(plugin.getEditor().getBarrelsEditor());
                    break;
                case 49:
                    Executor.async(() -> {
                        plugin.getEditor().saveConfiguration();
                        player.sendMessage("" + ChatColor.GOLD + ChatColor.BOLD + "WildStacker " + ChatColor.GRAY + "Saved configuration successfully.");
                    });
                    break;
            }
        }

        else if(e.getView().getTitle().equals("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "General Settings")){
            e.setCancelled(true);

            switch (e.getRawSlot()){
                case 0:
                    configValues.put(player.getUniqueId(), EditorHandler.GENERAL_SLOT_0);
                    break;
                case 1:
                    configValues.put(player.getUniqueId(), EditorHandler.GENERAL_SLOT_1);
                    break;
                default:
                    return;
            }

            lastInventories.put(player.getUniqueId(), "generalEditor");

            player.closeInventory();
            player.sendMessage("" + ChatColor.GOLD + ChatColor.BOLD + "WildStacker" + ChatColor.GRAY + " Please enter a new value (-cancel to cancel):");
        }

        else if(e.getView().getTitle().equals("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Items Settings")){
            e.setCancelled(true);

            switch (e.getRawSlot()){
                case 0:
                    configValues.put(player.getUniqueId(), EditorHandler.ITEMS_SLOT_0);
                    break;
                case 1:
                    configValues.put(player.getUniqueId(), EditorHandler.ITEMS_SLOT_1);
                    break;
                case 2:
                    configValues.put(player.getUniqueId(), EditorHandler.ITEMS_SLOT_2);
                    break;
                case 3:
                    configValues.put(player.getUniqueId(), EditorHandler.ITEMS_SLOT_3);
                    break;
                case 4:
                    configValues.put(player.getUniqueId(), EditorHandler.ITEMS_SLOT_4);
                    break;
                case 5:
                    configValues.put(player.getUniqueId(), EditorHandler.ITEMS_SLOT_5);
                    break;
                case 6:
                    configValues.put(player.getUniqueId(), EditorHandler.ITEMS_SLOT_6);
                    break;
                case 7:
                    configValues.put(player.getUniqueId(), EditorHandler.ITEMS_SLOT_7);
                    break;
                case 8:
                    configValues.put(player.getUniqueId(), EditorHandler.ITEMS_SLOT_8);
                    break;
                case 9:
                    configValues.put(player.getUniqueId(), EditorHandler.ITEMS_SLOT_9);
                    break;
                case 10:
                    configValues.put(player.getUniqueId(), EditorHandler.ITEMS_SLOT_10);
                    break;
                case 11:
                    configValues.put(player.getUniqueId(), EditorHandler.ITEMS_SLOT_11);
                    break;
                case 12:
                    configValues.put(player.getUniqueId(), EditorHandler.ITEMS_SLOT_12);
                    break;
                default:
                    return;
            }

            lastInventories.put(player.getUniqueId(), "itemsEditor");

            player.closeInventory();
            player.sendMessage("" + ChatColor.GOLD + ChatColor.BOLD + "WildStacker" + ChatColor.GRAY + " Please enter a new value (-cancel to cancel):");

            if(plugin.getEditor().config.isList(configValues.get(player.getUniqueId())) ||
                    plugin.getEditor().config.isConfigurationSection(configValues.get(player.getUniqueId()))){
                player.sendMessage("" + ChatColor.GOLD + ChatColor.BOLD + "WildStacker" + ChatColor.GRAY + " If you enter a value that is already in the list, it will be removed.");
            }
        }

        else if(e.getView().getTitle().equals("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Entities Settings")){
            e.setCancelled(true);

            switch (e.getRawSlot()){
                case 0:
                    configValues.put(player.getUniqueId(), EditorHandler.ENTITIES_SLOT_0);
                    break;
                case 1:
                    configValues.put(player.getUniqueId(), EditorHandler.ENTITIES_SLOT_1);
                    break;
                case 2:
                    configValues.put(player.getUniqueId(), EditorHandler.ENTITIES_SLOT_2);
                    break;
                case 3:
                    configValues.put(player.getUniqueId(), EditorHandler.ENTITIES_SLOT_3);
                    break;
                case 4:
                    configValues.put(player.getUniqueId(), EditorHandler.ENTITIES_SLOT_4);
                    break;
                case 5:
                    configValues.put(player.getUniqueId(), EditorHandler.ENTITIES_SLOT_5);
                    break;
                case 6:
                    configValues.put(player.getUniqueId(), EditorHandler.ENTITIES_SLOT_6);
                    break;
                case 7:
                    configValues.put(player.getUniqueId(), EditorHandler.ENTITIES_SLOT_7);
                    break;
                case 8:
                    configValues.put(player.getUniqueId(), EditorHandler.ENTITIES_SLOT_8);
                    break;
                case 9:
                    configValues.put(player.getUniqueId(), EditorHandler.ENTITIES_SLOT_9);
                    break;
                case 10:
                    configValues.put(player.getUniqueId(), EditorHandler.ENTITIES_SLOT_10);
                    break;
                case 11:
                    configValues.put(player.getUniqueId(), EditorHandler.ENTITIES_SLOT_11);
                    break;
                case 12:
                    configValues.put(player.getUniqueId(), EditorHandler.ENTITIES_SLOT_12);
                    break;
                case 13:
                    configValues.put(player.getUniqueId(), EditorHandler.ENTITIES_SLOT_13);
                    break;
                case 14:
                    configValues.put(player.getUniqueId(), EditorHandler.ENTITIES_SLOT_14);
                    break;
                case 15:
                    configValues.put(player.getUniqueId(), EditorHandler.ENTITIES_SLOT_15);
                    break;
                case 16:
                    configValues.put(player.getUniqueId(), EditorHandler.ENTITIES_SLOT_16);
                    break;
                case 17:
                    configValues.put(player.getUniqueId(), EditorHandler.ENTITIES_SLOT_17);
                    break;
                case 18:
                    configValues.put(player.getUniqueId(), EditorHandler.ENTITIES_SLOT_18);
                    break;
                case 19:
                    configValues.put(player.getUniqueId(), EditorHandler.ENTITIES_SLOT_19);
                    break;
                case 20:
                    configValues.put(player.getUniqueId(), EditorHandler.ENTITIES_SLOT_20);
                    break;
                case 21:
                    configValues.put(player.getUniqueId(), EditorHandler.ENTITIES_SLOT_21);
                    break;
                case 22:
                    configValues.put(player.getUniqueId(), EditorHandler.ENTITIES_SLOT_22);
                    break;
                case 23:
                    configValues.put(player.getUniqueId(), EditorHandler.ENTITIES_SLOT_23);
                    break;
                case 24:
                    configValues.put(player.getUniqueId(), EditorHandler.ENTITIES_SLOT_24);
                    break;
                case 25:
                    configValues.put(player.getUniqueId(), EditorHandler.ENTITIES_SLOT_25);
                    break;
                case 26:
                    configValues.put(player.getUniqueId(), EditorHandler.ENTITIES_SLOT_26);
                    break;
                case 27:
                    configValues.put(player.getUniqueId(), EditorHandler.ENTITIES_SLOT_27);
                    break;
                default:
                    return;
            }

            lastInventories.put(player.getUniqueId(), "entitiesEditor");

            player.closeInventory();
            player.sendMessage("" + ChatColor.GOLD + ChatColor.BOLD + "WildStacker" + ChatColor.GRAY + " Please enter a new value (-cancel to cancel):");

            if(plugin.getEditor().config.isList(configValues.get(player.getUniqueId())) ||
                    plugin.getEditor().config.isConfigurationSection(configValues.get(player.getUniqueId()))){
                player.sendMessage("" + ChatColor.GOLD + ChatColor.BOLD + "WildStacker" + ChatColor.GRAY + " If you enter a value that is already in the list, it will be removed.");
            }
        }

        else if(e.getView().getTitle().equals("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Spawners Settings")){
            e.setCancelled(true);

            switch (e.getRawSlot()){
                case 0:
                    configValues.put(player.getUniqueId(), EditorHandler.SPAWNERS_SLOT_0);
                    break;
                case 1:
                    configValues.put(player.getUniqueId(), EditorHandler.SPAWNERS_SLOT_1);
                    break;
                case 2:
                    configValues.put(player.getUniqueId(), EditorHandler.SPAWNERS_SLOT_2);
                    break;
                case 3:
                    configValues.put(player.getUniqueId(), EditorHandler.SPAWNERS_SLOT_3);
                    break;
                case 4:
                    configValues.put(player.getUniqueId(), EditorHandler.SPAWNERS_SLOT_4);
                    break;
                case 5:
                    configValues.put(player.getUniqueId(), EditorHandler.SPAWNERS_SLOT_5);
                    break;
                case 6:
                    configValues.put(player.getUniqueId(), EditorHandler.SPAWNERS_SLOT_6);
                    break;
                case 7:
                    configValues.put(player.getUniqueId(), EditorHandler.SPAWNERS_SLOT_7);
                    break;
                case 8:
                    configValues.put(player.getUniqueId(), EditorHandler.SPAWNERS_SLOT_8);
                    break;
                case 9:
                    configValues.put(player.getUniqueId(), EditorHandler.SPAWNERS_SLOT_9);
                    break;
                case 10:
                    configValues.put(player.getUniqueId(), EditorHandler.SPAWNERS_SLOT_10);
                    break;
                case 11:
                    configValues.put(player.getUniqueId(), EditorHandler.SPAWNERS_SLOT_11);
                    break;
                case 12:
                    configValues.put(player.getUniqueId(), EditorHandler.SPAWNERS_SLOT_12);
                    break;
                case 13:
                    configValues.put(player.getUniqueId(), EditorHandler.SPAWNERS_SLOT_13);
                    break;
                case 14:
                    configValues.put(player.getUniqueId(), EditorHandler.SPAWNERS_SLOT_14);
                    break;
                case 15:
                    configValues.put(player.getUniqueId(), EditorHandler.SPAWNERS_SLOT_15);
                    break;
                case 16:
                    configValues.put(player.getUniqueId(), EditorHandler.SPAWNERS_SLOT_16);
                    break;
                case 17:
                    configValues.put(player.getUniqueId(), EditorHandler.SPAWNERS_SLOT_17);
                    break;
                case 18:
                    configValues.put(player.getUniqueId(), EditorHandler.SPAWNERS_SLOT_18);
                    break;
                case 19:
                    configValues.put(player.getUniqueId(), EditorHandler.SPAWNERS_SLOT_19);
                    break;
                case 20:
                    configValues.put(player.getUniqueId(), EditorHandler.SPAWNERS_SLOT_20);
                    break;
                case 21:
                    configValues.put(player.getUniqueId(), EditorHandler.SPAWNERS_SLOT_21);
                    break;
                case 22:
                    configValues.put(player.getUniqueId(), EditorHandler.SPAWNERS_SLOT_22);
                    break;
                case 23:
                    configValues.put(player.getUniqueId(), EditorHandler.SPAWNERS_SLOT_23);
                    break;
                case 24:
                    configValues.put(player.getUniqueId(), EditorHandler.SPAWNERS_SLOT_24);
                    break;
                case 25:
                    configValues.put(player.getUniqueId(), EditorHandler.SPAWNERS_SLOT_25);
                    break;
                case 26:
                    configValues.put(player.getUniqueId(), EditorHandler.SPAWNERS_SLOT_26);
                    break;
                case 27:
                    configValues.put(player.getUniqueId(), EditorHandler.SPAWNERS_SLOT_27);
                    break;
                case 28:
                    configValues.put(player.getUniqueId(), EditorHandler.SPAWNERS_SLOT_28);
                    break;
                case 29:
                    configValues.put(player.getUniqueId(), EditorHandler.SPAWNERS_SLOT_29);
                    break;
                case 30:
                    configValues.put(player.getUniqueId(), EditorHandler.SPAWNERS_SLOT_30);
                    break;
                case 31:
                    configValues.put(player.getUniqueId(), EditorHandler.SPAWNERS_SLOT_31);
                    break;
                default:
                    return;
            }

            lastInventories.put(player.getUniqueId(), "spawnersEditor");

            player.closeInventory();
            player.sendMessage("" + ChatColor.GOLD + ChatColor.BOLD + "WildStacker" + ChatColor.GRAY + " Please enter a new value (-cancel to cancel):");

            if(plugin.getEditor().config.isList(configValues.get(player.getUniqueId())) ||
                    plugin.getEditor().config.isConfigurationSection(configValues.get(player.getUniqueId()))){
                player.sendMessage("" + ChatColor.GOLD + ChatColor.BOLD + "WildStacker" + ChatColor.GRAY + " If you enter a value that is already in the list, it will be removed.");
            }
        }

        else if(e.getView().getTitle().equals("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Barrels Settings")){
            e.setCancelled(true);

            switch (e.getRawSlot()){
                case 0:
                    configValues.put(player.getUniqueId(), EditorHandler.BARRELS_SLOT_0);
                    break;
                case 1:
                    configValues.put(player.getUniqueId(), EditorHandler.BARRELS_SLOT_1);
                    break;
                case 2:
                    configValues.put(player.getUniqueId(), EditorHandler.BARRELS_SLOT_2);
                    break;
                case 3:
                    configValues.put(player.getUniqueId(), EditorHandler.BARRELS_SLOT_3);
                    break;
                case 4:
                    configValues.put(player.getUniqueId(), EditorHandler.BARRELS_SLOT_4);
                    break;
                case 5:
                    configValues.put(player.getUniqueId(), EditorHandler.BARRELS_SLOT_5);
                    break;
                case 6:
                    configValues.put(player.getUniqueId(), EditorHandler.BARRELS_SLOT_6);
                    break;
                case 7:
                    configValues.put(player.getUniqueId(), EditorHandler.BARRELS_SLOT_7);
                    break;
                case 8:
                    configValues.put(player.getUniqueId(), EditorHandler.BARRELS_SLOT_8);
                    break;
                case 9:
                    configValues.put(player.getUniqueId(), EditorHandler.BARRELS_SLOT_9);
                    break;
                case 10:
                    configValues.put(player.getUniqueId(), EditorHandler.BARRELS_SLOT_10);
                    break;
                case 11:
                    configValues.put(player.getUniqueId(), EditorHandler.BARRELS_SLOT_11);
                    break;
                case 12:
                    configValues.put(player.getUniqueId(), EditorHandler.BARRELS_SLOT_12);
                    break;
                default:
                    return;
            }

            lastInventories.put(player.getUniqueId(), "barrelsEditor");

            player.closeInventory();
            player.sendMessage("" + ChatColor.GOLD + ChatColor.BOLD + "WildStacker" + ChatColor.GRAY + " Please enter a new value (-cancel to cancel):");

            if(plugin.getEditor().config.isList(configValues.get(player.getUniqueId())) ||
                    plugin.getEditor().config.isConfigurationSection(configValues.get(player.getUniqueId()))){
                player.sendMessage("" + ChatColor.GOLD + ChatColor.BOLD + "WildStacker" + ChatColor.GRAY + " If you enter a value that is already in the list, it will be removed.");
            }
        }

    }

    @EventHandler
    public void onEditorClose(InventoryCloseEvent e){
        if(e.getInventory() == null)
            return;

        if(!(e.getPlayer() instanceof Player))
            return;

        if(configValues.containsKey(e.getPlayer().getUniqueId()))
            return;

        Player player = (Player) e.getPlayer();

        Executor.sync(() -> {
            if(e.getView().getTitle().equals("" + ChatColor.GOLD + ChatColor.BOLD + "WildStacker")){
                if(!noResetClose.contains(player.getUniqueId())) {
                    Executor.async(() -> plugin.getEditor().reloadConfiguration());
                }
            }

            else if(e.getView().getTitle().equals("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "General Settings") ||
                    e.getView().getTitle().equals("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Items Settings") ||
                    e.getView().getTitle().equals("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Entities Settings") ||
                    e.getView().getTitle().equals("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Spawners Settings") ||
                    e.getView().getTitle().equals("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Barrels Settings")){
                noResetClose.remove(player.getUniqueId());
                player.openInventory(plugin.getEditor().getSettingsEditor());
            }
        }, 1L);
    }

    @EventHandler
    public void onAsyncPlayerChat(AsyncPlayerChatEvent e){
        if(!configValues.containsKey(e.getPlayer().getUniqueId()))
            return;

        e.setCancelled(true);

        String path = configValues.get(e.getPlayer().getUniqueId());
        Object value = e.getMessage();

        if(!value.toString().equalsIgnoreCase("-cancel")){
            if(plugin.getEditor().config.isConfigurationSection(path)){
                Matcher matcher;
                if(!(matcher = Pattern.compile("(.*):(.*)").matcher(value.toString())).matches()){
                    e.getPlayer().sendMessage(ChatColor.RED + "Please follow the <sub-section>:<value> format");
                }else {
                    String key = matcher.group(1);
                    path = path + "." + matcher.group(1);
                    value = matcher.group(2);

                    if(plugin.getEditor().config.get(path) != null && plugin.getEditor().config.get(path).toString().equals(value.toString())){
                        e.getPlayer().sendMessage("" + ChatColor.GOLD + ChatColor.BOLD + "WildStacker" + ChatColor.GRAY + " Removed the value " + matcher.group(1) + " from " + path);
                        value = null;
                    }else{
                        e.getPlayer().sendMessage("" + ChatColor.GOLD + ChatColor.BOLD + "WildStacker" + ChatColor.GRAY + " Added the value " + value.toString() + " to " + path);

                        try{
                            value = Integer.valueOf(value.toString());
                        }catch(IllegalArgumentException ex){
                            if(value.toString().equalsIgnoreCase("true") || value.toString().equalsIgnoreCase("false")){
                                value = Boolean.valueOf(value.toString());
                            }
                        }

                    }

                    plugin.getEditor().config.set(path, value);
                }
            }

            else if(plugin.getEditor().config.isList(path)){
                List<String> list = plugin.getEditor().config.getStringList(path);

                if (list.contains(value.toString())) {
                    list.remove(value.toString());
                    e.getPlayer().sendMessage("" + ChatColor.GOLD + ChatColor.BOLD + "WildStacker" + ChatColor.GRAY + " Removed the value " + value.toString() + " from " + path);
                } else {
                    list.add(value.toString());
                    e.getPlayer().sendMessage("" + ChatColor.GOLD + ChatColor.BOLD + "WildStacker" + ChatColor.GRAY + " Added the value " + value.toString() + " to " + path);
                }

                plugin.getEditor().config.set(path, list);
            }

            else{
                boolean valid = true;
                if(plugin.getEditor().config.isInt(path)){
                    try{
                        value = Integer.valueOf(value.toString());
                    }catch(IllegalArgumentException ex){
                        e.getPlayer().sendMessage(ChatColor.RED + "Please specify a valid number");
                        valid = false;
                    }
                }

                else if(plugin.getEditor().config.isBoolean(path)){
                    if(value.toString().equalsIgnoreCase("true") || value.toString().equalsIgnoreCase("false")){
                        value = Boolean.valueOf(value.toString());
                    }else{
                        e.getPlayer().sendMessage(ChatColor.RED + "Please specify a valid boolean");
                        valid = false;
                    }
                }

                if(valid) {
                    plugin.getEditor().config.set(path, value);
                    e.getPlayer().sendMessage("" + ChatColor.GOLD + ChatColor.BOLD + "WildStacker" + ChatColor.GRAY + " Changed value of " + path + " to " + value.toString());
                }
            }
        }

        e.getPlayer().openInventory(plugin.getEditor().getEditor(lastInventories.get(e.getPlayer().getUniqueId())));
        lastInventories.remove(e.getPlayer().getUniqueId());
        configValues.remove(e.getPlayer().getUniqueId());
    }

}
