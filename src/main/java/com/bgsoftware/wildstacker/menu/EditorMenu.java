package com.bgsoftware.wildstacker.menu;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.config.CommentedConfiguration;
import com.bgsoftware.wildstacker.handlers.SettingsHandler;
import com.bgsoftware.wildstacker.utils.entity.EntityUtils;
import com.bgsoftware.wildstacker.utils.items.ItemBuilder;
import com.bgsoftware.wildstacker.utils.legacy.Materials;
import com.bgsoftware.wildstacker.utils.threads.Executor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@SuppressWarnings("unused")
public abstract class EditorMenu extends WildMenu {

    protected static final Set<UUID> noResetClose = new HashSet<>();
    public static final Map<UUID, String> configValues = new HashMap<>();
    public static final Map<UUID, String> lastInventories = new HashMap<>();
    public static final Map<String, EditorMenu> editorMenus = new HashMap<>();

    public static CommentedConfiguration config;

    public final static String STEWS_SLOT_0 = "stews.enabled", STEWS_SLOT_1 = "stews.max-stack";

    private final Inventory inventory;
    private final String fieldPrefix, editorIdentifier;

    protected EditorMenu(Inventory inventory, String fieldPrefix, String editorIdentifier){
        this.inventory = inventory;
        this.fieldPrefix = fieldPrefix;
        this.editorIdentifier = editorIdentifier;
        editorMenus.put(editorIdentifier, this);
    }

    public boolean onEditorClick(InventoryClickEvent e){
        return false;
    }

    @Override
    public void onButtonClick(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();

        e.setCancelled(true);

        if(!onEditorClick(e)){
            lastInventories.put(player.getUniqueId(), editorIdentifier);
        }

        try{
            String value = (String) EditorMenu.class.getField(fieldPrefix + e.getRawSlot()).get(null);
            configValues.put(player.getUniqueId(), value);
            player.closeInventory();
            player.sendMessage("" + ChatColor.GOLD + ChatColor.BOLD + "WildStacker" + ChatColor.GRAY + " Please enter a new value (-cancel to cancel):");

            if(config.isList(configValues.get(player.getUniqueId())) ||
                    config.isConfigurationSection(configValues.get(player.getUniqueId()))){
                player.sendMessage("" + ChatColor.GOLD + ChatColor.BOLD + "WildStacker" + ChatColor.GRAY + " If you enter a value that is already in the list, it will be removed.");
            }
        }catch(Exception ignored){}
    }

    @Override
    public void onMenuClose(InventoryCloseEvent e) {
        if(configValues.containsKey(e.getPlayer().getUniqueId()))
            return;

        Player player = (Player) e.getPlayer();

        Executor.sync(() -> {
            if(e.getView().getTitle().equals("" + ChatColor.GOLD + ChatColor.BOLD + "WildStacker")){
                if(!noResetClose.contains(player.getUniqueId())) {
                    Executor.async(EditorMenu::reloadConfiguration);
                }
            }

            else {
                Inventory topInventory = e.getView().getTopInventory();
                InventoryHolder inventoryHolder = topInventory == null ? null : topInventory.getHolder();
                if(inventoryHolder instanceof EditorMenu && !(inventoryHolder instanceof EditorMainMenu)){
                    noResetClose.remove(player.getUniqueId());
                    EditorMainMenu.open(player);
                }
            }
        }, 1L);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public static void init(WildStackerPlugin plugin){
        File file = new File(plugin.getDataFolder(), "config.yml");
        config = CommentedConfiguration.loadConfiguration(file);
        config.syncWithConfig(file, plugin.getResource("config.yml"), "limits", "minimum-limits", "default-unstack", "break-slots", "fill-items", "break-charge", "place-charge");
    }

    public static void open(Player player){
        switch (lastInventories.getOrDefault(player.getUniqueId(), "")){
            case "generalEditor":
                EditorGeneralMenu.open(player);
                break;
            case "itemsEditor":
                EditorItemsMenu.open(player);
                break;
            case "entitiesEditor":
                EditorEntitiesMenu.open(player);
                break;
            case "spawnersEditor":
                EditorSpawnersMenu.open(player);
                break;
            case "barrelsEditor":
                EditorBarrelsMenu.open(player);
                break;
            case "bucketsEditor":
                EditorBucketsMenu.open(player);
                break;
            case "stewsEditor":
                EditorStewsMenu.open(player);
                break;
        }
    }

    protected static void saveConfiguration(){
        config.save(new File(plugin.getDataFolder(), "config.yml"));
        SettingsHandler.reload();
    }

    public static void reloadConfiguration(){
        try {
            config.load(new File(plugin.getDataFolder(), "config.yml"));
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

    private static void buildFromSection(List<ItemStack> itemStacks, ConfigurationSection section, String pathPrefix, String[] ignorePaths, String[] sectionsPaths){
        for(String path : section.getKeys(false)){
            String fullPath = section.getCurrentPath().isEmpty() ? path : section.getCurrentPath() + "." + path;

            if(Arrays.stream(ignorePaths).anyMatch(fullPath::contains))
                continue;

            if(section.isConfigurationSection(path) && Arrays.stream(sectionsPaths).noneMatch(fullPath::contains)){
                buildFromSection(itemStacks, section.getConfigurationSection(path), pathPrefix, ignorePaths, sectionsPaths);
            }
            else{
                ItemBuilder itemBuilder = new ItemBuilder(Materials.CLOCK).withName("&6" +
                        EntityUtils.getFormattedType(fullPath.replaceFirst(pathPrefix, "")
                                .replace("-", "_").replace(".", "_").replace(" ", "_"))
                );

                if(section.isBoolean(path))
                    itemBuilder.withLore("&7Value: " + section.getBoolean(path));
                else if(section.isInt(path))
                    itemBuilder.withLore("&7Value: " + section.getInt(path));
                else if(section.isDouble(path))
                    itemBuilder.withLore("&7Value: " + section.getDouble(path));
                else if(section.isString(path))
                    itemBuilder.withLore("&7Value: " + section.getString(path));
                else if(section.isList(path))
                    itemBuilder.withLore("&7Value:", section.getStringList(path));
                else if(section.isConfigurationSection(path))
                    itemBuilder.withLore("&7Value:", section.getConfigurationSection(path));

                itemStacks.add(itemBuilder.build());
            }
        }
    }

    protected static Inventory buildInventory(EditorMenu holder, String title, String pathPrefix, String[] ignorePaths, String[] sectionsPaths){
        List<ItemStack> itemStacks = new ArrayList<>(54);
        buildFromSection(itemStacks, config.getConfigurationSection(pathPrefix), pathPrefix, ignorePaths, sectionsPaths);

        Inventory inventory = Bukkit.createInventory(holder, ((itemStacks.size() / 9) + 1) * 9, title);
        inventory.setContents(Arrays.copyOf(itemStacks.toArray(new ItemStack[0]), inventory.getSize()));

        return inventory;
    }

}
