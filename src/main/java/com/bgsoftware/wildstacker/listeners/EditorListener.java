package com.bgsoftware.wildstacker.listeners;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.utils.Executor;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
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

    private String[] integerValues = new String[] {
            "save-interval", "items.merge-radius", "entities.merge-radius", "entities.stack-interval",
            "entities.kill-all.interval", "entities.linked-entities.max-distance", "spawners.merge-radius", "spawners.explosions-break-chance",
            "spawners.break-charge.amount", "spawners.place-charge.amount", "barrels.merge-radius"};
    private String[] booleanValues = new String[] {
            "items.enabled", "items.fix-stack", "items.item-display", "items.buckets-stacker.enabled", "items.kill-all",
            "entities.enabled", "entities.kill-all.clear-lagg", "entities.linked-entities.enabled", "entities.stack-down.enabled",
            "entities.keep-fire", "entities.mythic-mobs-stack", "entities.blazes-always-drop", "entities.keep-lowest-health",
            "entities.stack-after-breed", "entities.hide-names", "spawners.enabled", "spawners.chunk-merge", "spawners.explosions-break-stack",
            "spawners.drop-without-silk", "spawners.silk-spawners.enabled", "spawners.silk-spawners.explosions-drop-spawner",
            "spawners.silk-spawners.drop-to-inventory", "spawners.shift-get-whole-stack", "spawners.get-stacked-item", "spawners.floating-names",
            "spawners.break-menu.enabled", "spawners.place-inventory", "spawners.placement-permission", "spawners.shift-place-stack",
            "spawners.break-charge.multiply-stack-amount", "spawners.place-charge.multiply-stack-amount", "spawners.change-using-eggs",
            "spawners.eggs-stack-multiply", "spawners.next-spawner-placement", "spawners.only-one-spawner", "barrels.enabled",
            "barrels.chunk-merge", "barrels.explosions-break-stack", "barrels.toggle-command.enabled", "barrels.place-inventory"};
    private String[] listValues = new String[] {
            "items.blacklist", "items.disabled-worlds", "items.buckets-stacker.name-blacklist", "entities.disabled-worlds", "entities.disabled-regions",
            "entities.blacklist", "entities.spawn-blacklist", "entities.name-blacklist", "entities.instant-kill", "entities.nerfed-spawning",
            "entities.nerfed-worlds", "entities.stack-down.stack-down-types", "spawners.blacklist", "spawners.disabled-worlds",
            "barrels.whitelist", "barrels.disabled-worlds"};
    private String[] sectionValues = new String[] {
            "items.limits", "entities.limits", "entities.minimum-limits", "entities.stack-checks", "entities.stack-split", "spawners.limits", "barrels.limits"};

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

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClickMonitor(InventoryClickEvent e){
        if(e.getCurrentItem() != null && e.isCancelled() && e.getClickedInventory().getType() == InventoryType.CHEST &&
                e.getView().getTopInventory().equals(e.getClickedInventory())) {
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

        if(e.getInventory().getName().equals("" + ChatColor.GOLD + ChatColor.BOLD + "WildStacker")){
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

        else if(e.getInventory().getName().equals("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "General Settings")){
            e.setCancelled(true);

            switch (e.getRawSlot()){
                case 0:
                    configValues.put(player.getUniqueId(), "save-interval");
                    break;
                case 1:
                    configValues.put(player.getUniqueId(), "give-item-name");
                    break;
                default:
                    return;
            }

            lastInventories.put(player.getUniqueId(), "generalEditor");

            player.closeInventory();
            player.sendMessage("" + ChatColor.GOLD + ChatColor.BOLD + "WildStacker" + ChatColor.GRAY + " Please enter a new value (-cancel to cancel):");
        }

        else if(e.getInventory().getName().equals("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Items Settings")){
            e.setCancelled(true);

            switch (e.getRawSlot()){
                case 0:
                    configValues.put(player.getUniqueId(), "items.enabled");
                    break;
                case 1:
                    configValues.put(player.getUniqueId(), "items.merge-radius");
                    break;
                case 2:
                    configValues.put(player.getUniqueId(), "items.custom-name");
                    break;
                case 3:
                    configValues.put(player.getUniqueId(), "items.blacklist");
                    break;
                case 4:
                    configValues.put(player.getUniqueId(), "items.limits");
                    break;
                case 5:
                    configValues.put(player.getUniqueId(), "items.disabled-worlds");
                    break;
                case 6:
                    configValues.put(player.getUniqueId(), "items.fix-stack");
                    break;
                case 7:
                    configValues.put(player.getUniqueId(), "items.item-display");
                    break;
                case 8:
                    configValues.put(player.getUniqueId(), "items.buckets-stacker.enabled");
                    break;
                case 9:
                    configValues.put(player.getUniqueId(), "items.buckets-stacker.name-blacklist");
                    break;
                case 10:
                    configValues.put(player.getUniqueId(), "items.kill-all");
                    break;
                default:
                    return;
            }

            lastInventories.put(player.getUniqueId(), "itemsEditor");

            player.closeInventory();
            player.sendMessage("" + ChatColor.GOLD + ChatColor.BOLD + "WildStacker" + ChatColor.GRAY + " Please enter a new value (-cancel to cancel):");

            if(Arrays.asList(listValues).contains(configValues.get(player.getUniqueId())) || Arrays.asList(sectionValues).contains(configValues.get(player.getUniqueId()))){
                player.sendMessage("" + ChatColor.GOLD + ChatColor.BOLD + "WildStacker" + ChatColor.GRAY + " If you enter a value that is already in the list, it will be removed.");
            }
        }

        else if(e.getInventory().getName().equals("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Entities Settings")){
            e.setCancelled(true);

            switch (e.getRawSlot()){
                case 0:
                    configValues.put(player.getUniqueId(), "entities.enabled");
                    break;
                case 1:
                    configValues.put(player.getUniqueId(), "entities.merge-radius");
                    break;
                case 2:
                    configValues.put(player.getUniqueId(), "entities.custom-name");
                    break;
                case 3:
                    configValues.put(player.getUniqueId(), "entities.blacklist");
                    break;
                case 4:
                    configValues.put(player.getUniqueId(), "entities.limits");
                    break;
                case 5:
                    configValues.put(player.getUniqueId(), "entities.minimum-limits");
                    break;
                case 6:
                    configValues.put(player.getUniqueId(), "entities.disabled-worlds");
                    break;
                case 7:
                    configValues.put(player.getUniqueId(), "entities.disabled-regions");
                    break;
                case 8:
                    configValues.put(player.getUniqueId(), "entities.spawn-blacklist");
                    break;
                case 9:
                    configValues.put(player.getUniqueId(), "entities.name-blacklist");
                    break;
                case 10:
                    configValues.put(player.getUniqueId(), "entities.stack-interval");
                    break;
                case 11:
                    configValues.put(player.getUniqueId(), "entities.kill-all.interval");
                    break;
                case 12:
                    configValues.put(player.getUniqueId(), "entities.kill-all.clear-lagg");
                    break;
                case 13:
                    configValues.put(player.getUniqueId(), "entities.stack-checks");
                    break;
                case 14:
                    configValues.put(player.getUniqueId(), "entities.stack-split");
                    break;
                case 15:
                    configValues.put(player.getUniqueId(), "entities.linked-entities.enabled");
                    break;
                case 16:
                    configValues.put(player.getUniqueId(), "entities.linked-entities.max-distance");
                    break;
                case 17:
                    configValues.put(player.getUniqueId(), "entities.instant-kill");
                    break;
                case 18:
                    configValues.put(player.getUniqueId(), "entities.nerfed-spawning");
                    break;
                case 19:
                    configValues.put(player.getUniqueId(), "entities.stack-down.enabled");
                    break;
                case 20:
                    configValues.put(player.getUniqueId(), "entities.stack-down.stack-down-types");
                    break;
                case 21:
                    configValues.put(player.getUniqueId(), "entities.keep-fire");
                    break;
                case 22:
                    configValues.put(player.getUniqueId(), "entities.mythic-mobs-stack");
                    break;
                case 23:
                    configValues.put(player.getUniqueId(), "entities.blazes-always-drop");
                    break;
                case 24:
                    configValues.put(player.getUniqueId(), "entities.keep-lowest-health");
                    break;
                case 25:
                    configValues.put(player.getUniqueId(), "entities.stack-after-breed");
                    break;
                case 26:
                    configValues.put(player.getUniqueId(), "entities.hide-names");
                    break;
                default:
                    return;
            }

            lastInventories.put(player.getUniqueId(), "entitiesEditor");

            player.closeInventory();
            player.sendMessage("" + ChatColor.GOLD + ChatColor.BOLD + "WildStacker" + ChatColor.GRAY + " Please enter a new value (-cancel to cancel):");

            if(Arrays.asList(listValues).contains(configValues.get(player.getUniqueId())) || Arrays.asList(sectionValues).contains(configValues.get(player.getUniqueId()))){
                player.sendMessage("" + ChatColor.GOLD + ChatColor.BOLD + "WildStacker" + ChatColor.GRAY + " If you enter a value that is already in the list, it will be removed.");
            }
        }

        else if(e.getInventory().getName().equals("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Spawners Settings")){
            e.setCancelled(true);

            switch (e.getRawSlot()){
                case 0:
                    configValues.put(player.getUniqueId(), "spawners.enabled");
                    break;
                case 1:
                    configValues.put(player.getUniqueId(), "spawners.merge-radius");
                    break;
                case 2:
                    configValues.put(player.getUniqueId(), "spawners.custom-name");
                    break;
                case 3:
                    configValues.put(player.getUniqueId(), "spawners.blacklist");
                    break;
                case 4:
                    configValues.put(player.getUniqueId(), "spawners.limits");
                    break;
                case 5:
                    configValues.put(player.getUniqueId(), "spawners.disabled-worlds");
                    break;
                case 6:
                    configValues.put(player.getUniqueId(), "spawners.chunk-merge");
                    break;
                case 7:
                    configValues.put(player.getUniqueId(), "spawners.explosions-break-stack");
                    break;
                case 8:
                    configValues.put(player.getUniqueId(), "spawners.explosions-break-chance");
                    break;
                case 9:
                    configValues.put(player.getUniqueId(), "spawners.drop-without-silk");
                    break;
                case 10:
                    configValues.put(player.getUniqueId(), "spawners.silk-spawners.enabled");
                    break;
                case 11:
                    configValues.put(player.getUniqueId(), "spawners.silk-spawners.custom-name");
                    break;
                case 12:
                    configValues.put(player.getUniqueId(), "spawners.silk-spawners.explosions-drop-spawner");
                    break;
                case 13:
                    configValues.put(player.getUniqueId(), "spawners.silk-spawners.drop-to-inventory");
                    break;
                case 14:
                    configValues.put(player.getUniqueId(), "spawners.shift-get-whole-stack");
                    break;
                case 15:
                    configValues.put(player.getUniqueId(), "spawners.get-stacked-item");
                    break;
                case 16:
                    configValues.put(player.getUniqueId(), "spawners.floating-names");
                    break;
                case 17:
                    configValues.put(player.getUniqueId(), "spawners.break-menu.enabled");
                    break;
                case 18:
                    configValues.put(player.getUniqueId(), "spawners.place-inventory");
                    break;
                case 19:
                    configValues.put(player.getUniqueId(), "spawners.placement-permission");
                    break;
                case 20:
                    configValues.put(player.getUniqueId(), "spawners.shift-place-stack");
                    break;
                case 21:
                    configValues.put(player.getUniqueId(), "spawners.break-charge.amount");
                    break;
                case 22:
                    configValues.put(player.getUniqueId(), "spawners.break-charge.multiply-stack-amount");
                    break;
                case 23:
                    configValues.put(player.getUniqueId(), "spawners.place-charge.amount");
                    break;
                case 24:
                    configValues.put(player.getUniqueId(), "spawners.place-charge.multiply-stack-amount");
                    break;
                case 25:
                    configValues.put(player.getUniqueId(), "spawners.change-using-eggs");
                    break;
                case 26:
                    configValues.put(player.getUniqueId(), "spawners.eggs-stack-multiply");
                    break;
                case 27:
                    configValues.put(player.getUniqueId(), "spawners.next-spawner-placement");
                    break;
                case 28:
                    configValues.put(player.getUniqueId(), "spawners.only-one-spawner");
                    break;
                default:
                    return;
            }

            lastInventories.put(player.getUniqueId(), "spawnersEditor");

            player.closeInventory();
            player.sendMessage("" + ChatColor.GOLD + ChatColor.BOLD + "WildStacker" + ChatColor.GRAY + " Please enter a new value (-cancel to cancel):");

            if(Arrays.asList(listValues).contains(configValues.get(player.getUniqueId())) || Arrays.asList(sectionValues).contains(configValues.get(player.getUniqueId()))){
                player.sendMessage("" + ChatColor.GOLD + ChatColor.BOLD + "WildStacker" + ChatColor.GRAY + " If you enter a value that is already in the list, it will be removed.");
            }
        }

        else if(e.getInventory().getName().equals("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Barrels Settings")){
            e.setCancelled(true);

            switch (e.getRawSlot()){
                case 0:
                    configValues.put(player.getUniqueId(), "barrels.enabled");
                    break;
                case 1:
                    configValues.put(player.getUniqueId(), "barrels.merge-radius");
                    break;
                case 2:
                    configValues.put(player.getUniqueId(), "barrels.custom-name");
                    break;
                case 3:
                    configValues.put(player.getUniqueId(), "barrels.blacklist");
                    break;
                case 4:
                    configValues.put(player.getUniqueId(), "barrels.limits");
                    break;
                case 5:
                    configValues.put(player.getUniqueId(), "barrels.disabled-worlds");
                    break;
                case 6:
                    configValues.put(player.getUniqueId(), "barrels.chunk-merge");
                    break;
                case 7:
                    configValues.put(player.getUniqueId(), "barrels.explosions-break-stack");
                    break;
                case 8:
                    configValues.put(player.getUniqueId(), "barrels.toggle-command.enabled");
                    break;
                case 9:
                    configValues.put(player.getUniqueId(), "barrels.toggle-command.command");
                    break;
                case 10:
                    configValues.put(player.getUniqueId(), "barrels.place-inventory");
                    break;
                default:
                    return;
            }

            lastInventories.put(player.getUniqueId(), "barrelsEditor");

            player.closeInventory();
            player.sendMessage("" + ChatColor.GOLD + ChatColor.BOLD + "WildStacker" + ChatColor.GRAY + " Please enter a new value (-cancel to cancel):");

            if(Arrays.asList(listValues).contains(configValues.get(player.getUniqueId())) || Arrays.asList(sectionValues).contains(configValues.get(player.getUniqueId()))){
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
            if(e.getInventory().getName().equals("" + ChatColor.GOLD + ChatColor.BOLD + "WildStacker")){
                if(!noResetClose.contains(player.getUniqueId())) {
                    Executor.async(() -> plugin.getEditor().reloadConfiguration());
                }
            }

            else if(e.getInventory().getName().equals("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "General Settings") ||
                    e.getInventory().getName().equals("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Items Settings") ||
                    e.getInventory().getName().equals("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Entities Settings") ||
                    e.getInventory().getName().equals("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Spawners Settings") ||
                    e.getInventory().getName().equals("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Barrels Settings")){
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
            if(Arrays.asList(sectionValues).contains(path)){
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

            else if(Arrays.asList(listValues).contains(path)){
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
                if(Arrays.asList(integerValues).contains(path)){
                    try{
                        value = Integer.valueOf(value.toString());
                    }catch(IllegalArgumentException ex){
                        e.getPlayer().sendMessage(ChatColor.RED + "Please specify a valid number");
                        valid = false;
                    }
                }

                else if(Arrays.asList(booleanValues).contains(path)){
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
