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
    private List<String> inventoryTitles = Arrays.asList("Add items here", "WildStacker", "General Settings", "Items Settings",
            "Entities Settings", "Spawners Settings", "Barrels Settings");

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClickMonitor(InventoryClickEvent e){
        if(e.getCurrentItem() != null && e.isCancelled() && (inventoryTitles.contains(e.getView().getTitle()) ||
                plugin.getBreakMenuHandler().isBreakMenu(e.getInventory()))) {
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

        String slotPrefix = "";

        if(e.getView().getTitle().equals("" + ChatColor.GOLD + ChatColor.BOLD + "WildStacker")){
            e.setCancelled(true);

            switch (e.getRawSlot()){
                case 19:
                    noResetClose.add(player.getUniqueId());
                    plugin.getEditor().openGeneralEditor(player);
                    break;
                case 21:
                    noResetClose.add(player.getUniqueId());
                    plugin.getEditor().openItemsEditor(player);
                    break;
                case 23:
                    noResetClose.add(player.getUniqueId());
                    plugin.getEditor().openEntitiesEditor(player);
                    break;
                case 25:
                    noResetClose.add(player.getUniqueId());
                    plugin.getEditor().openBucketsEditor(player);
                    break;
                case 29:
                    noResetClose.add(player.getUniqueId());
                    plugin.getEditor().openSpawnersEditor(player);
                    break;
                case 31:
                    noResetClose.add(player.getUniqueId());
                    plugin.getEditor().openBarrelsEditor(player);
                    break;
                case 33:
                    noResetClose.add(player.getUniqueId());
                    plugin.getEditor().openStewsEditor(player);
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
            slotPrefix = "GENERAL_SLOT_";
            lastInventories.put(player.getUniqueId(), "generalEditor");
        }

        else if(e.getView().getTitle().equals("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Items Settings")){
            e.setCancelled(true);
            slotPrefix = "ITEMS_SLOT_";
            lastInventories.put(player.getUniqueId(), "itemsEditor");
        }

        else if(e.getView().getTitle().equals("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Entities Settings")){
            e.setCancelled(true);
            slotPrefix = "ENTITIES_SLOT_";
            lastInventories.put(player.getUniqueId(), "entitiesEditor");
        }

        else if(e.getView().getTitle().equals("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Buckets Settings")){
            e.setCancelled(true);
            slotPrefix = "BUCKETS_SLOT_";
            lastInventories.put(player.getUniqueId(), "bucketsEditor");
        }

        else if(e.getView().getTitle().equals("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Spawners Settings")){
            e.setCancelled(true);
            slotPrefix = "SPAWNERS_SLOT_";
            lastInventories.put(player.getUniqueId(), "spawnersEditor");
        }

        else if(e.getView().getTitle().equals("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Stews Settings")){
            e.setCancelled(true);
            slotPrefix = "STEWS_SLOT_";
            lastInventories.put(player.getUniqueId(), "stewsEditor");
        }

        try{
            String value = (String) EditorHandler.class.getField(slotPrefix + e.getRawSlot()).get(null);
            configValues.put(player.getUniqueId(), value);
            player.closeInventory();
            player.sendMessage("" + ChatColor.GOLD + ChatColor.BOLD + "WildStacker" + ChatColor.GRAY + " Please enter a new value (-cancel to cancel):");

            if(plugin.getEditor().config.isList(configValues.get(player.getUniqueId())) ||
                    plugin.getEditor().config.isConfigurationSection(configValues.get(player.getUniqueId()))){
                player.sendMessage("" + ChatColor.GOLD + ChatColor.BOLD + "WildStacker" + ChatColor.GRAY + " If you enter a value that is already in the list, it will be removed.");
            }
        }catch(Exception ignored){}
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
                    e.getView().getTitle().equals("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Buckets Settings") ||
                    e.getView().getTitle().equals("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Spawners Settings") ||
                    e.getView().getTitle().equals("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Barrels Settings") ||
                    e.getView().getTitle().equals("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Stews Settings")){
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

                else if(plugin.getEditor().config.isDouble(path)){
                    try{
                        value = Double.valueOf(value.toString());
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

        Executor.sync(() -> {
            plugin.getEditor().openEditor(e.getPlayer(), lastInventories.get(e.getPlayer().getUniqueId()));
            lastInventories.remove(e.getPlayer().getUniqueId());
            configValues.remove(e.getPlayer().getUniqueId());
        });
    }

}
