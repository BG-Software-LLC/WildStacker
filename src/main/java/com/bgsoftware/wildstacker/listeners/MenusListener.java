package com.bgsoftware.wildstacker.listeners;

import com.bgsoftware.wildstacker.menu.EditorMenu;
import com.bgsoftware.wildstacker.menu.WildMenu;
import com.bgsoftware.wildstacker.utils.threads.Executor;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("unused")
public final class MenusListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onMenuClick(InventoryClickEvent e){
        Inventory topInventory = e.getView().getTopInventory();
        if(topInventory != null && topInventory.getHolder() instanceof WildMenu){
            WildMenu wildMenu = (WildMenu) topInventory.getHolder();
            wildMenu.onButtonClick(e);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onMenuClose(InventoryCloseEvent e){
        Inventory topInventory = e.getView().getTopInventory();
        if(topInventory != null && topInventory.getHolder() instanceof WildMenu){
            WildMenu wildMenu = (WildMenu) topInventory.getHolder();
            wildMenu.onMenuClose(e);
        }
    }

    /**
     * The following two events are here for patching a dupe glitch caused
     * by shift clicking and closing the inventory in the same time.
     */

    private final Map<UUID, ItemStack> latestClickedItem = new HashMap<>();

    @EventHandler(priority = EventPriority.MONITOR)
    public void onMenuClickMonitor(InventoryClickEvent e){
        Inventory topInventory = e.getView().getTopInventory();
        if(e.getCurrentItem() != null && e.isCancelled() && topInventory != null && topInventory.getHolder() instanceof WildMenu &&
                ((WildMenu) topInventory.getHolder()).isCancelOnClick()){
            latestClickedItem.put(e.getWhoClicked().getUniqueId(), e.getCurrentItem());
            Executor.sync(() -> latestClickedItem.remove(e.getWhoClicked().getUniqueId()), 20L);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onMenuCloseMonitor(InventoryCloseEvent e){
        ItemStack clickedItem = latestClickedItem.get(e.getPlayer().getUniqueId());
        if(clickedItem != null){
            Executor.sync(() -> {
                e.getPlayer().getInventory().removeItem(clickedItem);
                ((Player) e.getPlayer()).updateInventory();
            }, 1L);
        }
    }

    /**
     * Listening to AsyncPlayerChatEvent for the editor menus.
     */

    @EventHandler
    public void onAsyncPlayerChat(AsyncPlayerChatEvent e){
        if(!EditorMenu.configValues.containsKey(e.getPlayer().getUniqueId()))
            return;

        e.setCancelled(true);

        String path = EditorMenu.configValues.get(e.getPlayer().getUniqueId());
        Object value = e.getMessage();

        if(!value.toString().equalsIgnoreCase("-cancel")){
            if(EditorMenu.config.isConfigurationSection(path)){
                Matcher matcher;
                if(!(matcher = Pattern.compile("(.*):(.*)").matcher(value.toString())).matches()){
                    e.getPlayer().sendMessage(ChatColor.RED + "Please follow the <sub-section>:<value> format");
                }else {
                    String key = matcher.group(1);
                    path = path + "." + matcher.group(1);
                    value = matcher.group(2);

                    if(EditorMenu.config.get(path) != null && EditorMenu.config.get(path).toString().equals(value.toString())){
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

                    EditorMenu.config.set(path, value);
                }
            }

            else if(EditorMenu.config.isList(path)){
                List<String> list = EditorMenu.config.getStringList(path);

                if (list.contains(value.toString())) {
                    list.remove(value.toString());
                    e.getPlayer().sendMessage("" + ChatColor.GOLD + ChatColor.BOLD + "WildStacker" + ChatColor.GRAY + " Removed the value " + value.toString() + " from " + path);
                } else {
                    list.add(value.toString());
                    e.getPlayer().sendMessage("" + ChatColor.GOLD + ChatColor.BOLD + "WildStacker" + ChatColor.GRAY + " Added the value " + value.toString() + " to " + path);
                }

                EditorMenu.config.set(path, list);
            }

            else{
                boolean valid = true;
                if(EditorMenu.config.isInt(path)){
                    try{
                        value = Integer.valueOf(value.toString());
                    }catch(IllegalArgumentException ex){
                        e.getPlayer().sendMessage(ChatColor.RED + "Please specify a valid number");
                        valid = false;
                    }
                }

                else if(EditorMenu.config.isDouble(path)){
                    try{
                        value = Double.valueOf(value.toString());
                    }catch(IllegalArgumentException ex){
                        e.getPlayer().sendMessage(ChatColor.RED + "Please specify a valid number");
                        valid = false;
                    }
                }

                else if(EditorMenu.config.isBoolean(path)){
                    if(value.toString().equalsIgnoreCase("true") || value.toString().equalsIgnoreCase("false")){
                        value = Boolean.valueOf(value.toString());
                    }else{
                        e.getPlayer().sendMessage(ChatColor.RED + "Please specify a valid boolean");
                        valid = false;
                    }
                }

                if(valid) {
                    EditorMenu.config.set(path, value);
                    e.getPlayer().sendMessage("" + ChatColor.GOLD + ChatColor.BOLD + "WildStacker" + ChatColor.GRAY + " Changed value of " + path + " to " + value.toString());
                }
            }
        }

        Executor.sync(() -> {
            EditorMenu.open(e.getPlayer());
            EditorMenu.lastInventories.remove(e.getPlayer().getUniqueId());
            EditorMenu.configValues.remove(e.getPlayer().getUniqueId());
        });
    }

}
