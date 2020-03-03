package com.bgsoftware.wildstacker.menu;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.config.CommentedConfiguration;
import com.bgsoftware.wildstacker.handlers.SettingsHandler;
import com.bgsoftware.wildstacker.utils.entity.EntityUtils;
import com.bgsoftware.wildstacker.utils.items.ItemBuilder;
import com.bgsoftware.wildstacker.utils.legacy.Materials;
import com.bgsoftware.wildstacker.utils.threads.Executor;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.io.File;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@SuppressWarnings("unused")
public abstract class EditorMenu extends WildMenu {

    protected static final Set<Field> generalFields = new HashSet<>(), itemsFields = new HashSet<>(), entitiesFields = new HashSet<>(),
            bucketsFields = new HashSet<>(), spawnersFields = new HashSet<>(), barrelsFields = new HashSet<>(), stewsFields = new HashSet<>();

    protected static final Set<UUID> noResetClose = new HashSet<>();
    public static final Map<UUID, String> configValues = new HashMap<>();
    public static final Map<UUID, String> lastInventories = new HashMap<>();
    public static final Map<String, EditorMenu> editorMenus = new HashMap<>();

    public static CommentedConfiguration config;

    public final static String GENERAL_SLOT_0 = "give-item-name", GENERAL_SLOT_1 = "database.delete-invalid-worlds",
            GENERAL_SLOT_2 = "database.delete-invalid-blocks", GENERAL_SLOT_3 = "kill-task.interval",
            GENERAL_SLOT_4 = "kill-task.stacked-entities", GENERAL_SLOT_5 = "kill-task.unstacked-entities",
            GENERAL_SLOT_6 = "kill-task.stacked-items", GENERAL_SLOT_7 = "kill-task.unstacked-items",
            GENERAL_SLOT_8 = "kill-task.sync-clear-lagg", GENERAL_SLOT_9 = "kill-task.whitelist",
            GENERAL_SLOT_10 = "kill-task.blacklist";

    public final static String ITEMS_SLOT_0 = "items.enabled", ITEMS_SLOT_1 = "items.merge-radius",
            ITEMS_SLOT_2 = "items.custom-name", ITEMS_SLOT_3 = "items.blacklist", ITEMS_SLOT_4 = "items.whitelist",
            ITEMS_SLOT_5 = "items.limits", ITEMS_SLOT_6 = "items.disabled-worlds", ITEMS_SLOT_7 = "items.chunk-limit",
            ITEMS_SLOT_8 = "items.particles", ITEMS_SLOT_9 = "items.unstacked-custom-name",
            ITEMS_SLOT_10 = "items.fix-stack", ITEMS_SLOT_11 = "items.item-display",
            ITEMS_SLOT_12 = "items.names-toggle.enabled", ITEMS_SLOT_13 = "items.names-toggle.command",
            ITEMS_SLOT_14 = "items.pickup-sound.enabled", ITEMS_SLOT_15 = "items.pickup-sound.volume",
            ITEMS_SLOT_16 = "items.pickup-sound.pitch", ITEMS_SLOT_17 = "items.max-pickup-delay";

    public final static String ENTITIES_SLOT_0 = "entities.enabled", ENTITIES_SLOT_1 = "entities.merge-radius",
            ENTITIES_SLOT_2 = "entities.custom-name", ENTITIES_SLOT_3 = "entities.blacklist",
            ENTITIES_SLOT_4 = "entities.whitelist", ENTITIES_SLOT_5 = "entities.limits",
            ENTITIES_SLOT_6 = "entities.minimum-limits", ENTITIES_SLOT_7 = "entities.disabled-worlds",
            ENTITIES_SLOT_8 = "entities.chunk-limit", ENTITIES_SLOT_9 = "entities.particles",
            ENTITIES_SLOT_10 = "entities.disabled-regions", ENTITIES_SLOT_11 = "entities.name-blacklist",
            ENTITIES_SLOT_12 = "entities.stack-interval", ENTITIES_SLOT_13 = "entities.stack-checks",
            ENTITIES_SLOT_14 = "entities.stack-split", ENTITIES_SLOT_15 = "entities.linked-entities.enabled",
            ENTITIES_SLOT_16 = "entities.linked-entities.max-distance", ENTITIES_SLOT_17 = "entities.instant-kill",
            ENTITIES_SLOT_18 = "entities.nerfed-entities.whitelist", ENTITIES_SLOT_19 = "entities.nerfed-entities.blacklist",
            ENTITIES_SLOT_20 = "entities.nerfed-entities.worlds", ENTITIES_SLOT_21 = "entities.nerfed-entities.teleport",
            ENTITIES_SLOT_22 = "entities.stack-down.enabled", ENTITIES_SLOT_23 = "entities.stack-down.stack-down-types",
            ENTITIES_SLOT_24 = "entities.keep-fire", ENTITIES_SLOT_25 = "entities.mythic-mobs-custom-name",
            ENTITIES_SLOT_26 = "entities.keep-lowest-health", ENTITIES_SLOT_27 = "entities.stack-after-breed",
            ENTITIES_SLOT_28 = "entities.hide-names", ENTITIES_SLOT_29 = "entities.next-stack-knockback",
            ENTITIES_SLOT_30 = "entities.default-unstack", ENTITIES_SLOT_31 = "entities.auto-exp-pickup",
            ENTITIES_SLOT_32 = "entities.egg-lay-multiply", ENTITIES_SLOT_33 = "entities.clear-equipment",
            ENTITIES_SLOT_34 = "entities.spawn-corpses";

    public final static String BUCKETS_SLOT_0 = "buckets.enabled", BUCKETS_SLOT_1 = "buckets.name-blacklist",
            BUCKETS_SLOT_2 = "buckets.max-stack";

    public final static String SPAWNERS_SLOT_0 = "spawners.enabled", SPAWNERS_SLOT_1 = "spawners.merge-radius",
            SPAWNERS_SLOT_2 = "spawners.custom-name", SPAWNERS_SLOT_3 = "spawners.blacklist",
            SPAWNERS_SLOT_4 = "spawners.whitelist", SPAWNERS_SLOT_5 = "spawners.limits",
            SPAWNERS_SLOT_6 = "spawners.disabled-worlds", SPAWNERS_SLOT_7 = "spawners.chunk-limit",
            SPAWNERS_SLOT_8 = "spawners.per-spawner-limit", SPAWNERS_SLOT_9 = "spawners.explosions-amount-percentage",
            SPAWNERS_SLOT_10 = "spawners.explosions-drop-to-inventory", SPAWNERS_SLOT_11 = "spawners.particles",
            SPAWNERS_SLOT_12 = "spawners.chunk-merge", SPAWNERS_SLOT_13 = "spawners.explosions-break-stack",
            SPAWNERS_SLOT_14 = "spawners.explosions-break-chance", SPAWNERS_SLOT_15 = "spawners.silk-touch-break-chance",
            SPAWNERS_SLOT_16 = "spawners.drop-without-silk", SPAWNERS_SLOT_17 = "spawners.silk-spawners.enabled",
            SPAWNERS_SLOT_18 = "spawners.silk-spawners.custom-name", SPAWNERS_SLOT_19 = "spawners.silk-spawners.custom-lore",
            SPAWNERS_SLOT_20 = "spawners.silk-spawners.explosions-drop-spawner", SPAWNERS_SLOT_21 = "spawners.silk-spawners.drop-to-inventory",
            SPAWNERS_SLOT_22 = "spawners.silk-spawners.worlds", SPAWNERS_SLOT_23 = "spawners.shift-get-whole-stack",
            SPAWNERS_SLOT_24 = "spawners.get-stacked-item", SPAWNERS_SLOT_25 = "spawners.floating-names",
            SPAWNERS_SLOT_26 = "spawners.break-menu.enabled", SPAWNERS_SLOT_27 = "spawners.place-inventory.enabled",
            SPAWNERS_SLOT_28 = "spawners.place-inventory.title", SPAWNERS_SLOT_29 = "spawners.placement-permission",
            SPAWNERS_SLOT_30 = "spawners.shift-place-stack", SPAWNERS_SLOT_31 = "spawners.change-using-eggs",
            SPAWNERS_SLOT_32 = "spawners.eggs-stack-multiply", SPAWNERS_SLOT_33 = "spawners.next-spawner-placement",
            SPAWNERS_SLOT_34 = "spawners.only-one-spawner", SPAWNERS_SLOT_35 = "spawners.inventory-tweaks.enabled",
            SPAWNERS_SLOT_36 = "spawners.inventory-tweaks.permission", SPAWNERS_SLOT_37 = "spawners.inventory-tweaks.toggle-command";

    public final static String BARRELS_SLOT_0 = "barrels.enabled", BARRELS_SLOT_1 = "barrels.merge-radius",
            BARRELS_SLOT_2 = "barrels.custom-name", BARRELS_SLOT_3 = "barrels.blacklist", BARRELS_SLOT_4 = "barrels.whitelist",
            BARRELS_SLOT_5 = "barrels.limits", BARRELS_SLOT_6 = "barrels.disabled-worlds", BARRELS_SLOT_7 = "barrels.chunk-limit",
            BARRELS_SLOT_8 = "barrels.particles", BARRELS_SLOT_9 = "barrels.chunk-merge", BARRELS_SLOT_10 = "barrels.explosions-break-stack",
            BARRELS_SLOT_11 = "barrels.toggle-command.enabled", BARRELS_SLOT_12 = "barrels.toggle-command.command",
            BARRELS_SLOT_13 = "barrels.place-inventory.enabled", BARRELS_SLOT_14 = "barrels.place-inventory.title",
            BARRELS_SLOT_15 = "barrels.required-permission", BARRELS_SLOT_16 = "barrels.auto-pickup";

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

        for(Field field : EditorMenu.class.getDeclaredFields()){
            if(field.getName().startsWith("GENERAL")){
                generalFields.add(field);
            }
            else if(field.getName().startsWith("ITEMS")){
                itemsFields.add(field);
            }
            else if(field.getName().startsWith("ENTITIES")){
                entitiesFields.add(field);
            }
            else if(field.getName().startsWith("BUCKETS")){
                bucketsFields.add(field);
            }
            else if(field.getName().startsWith("SPAWNERS")){
                spawnersFields.add(field);
            }
            else if(field.getName().startsWith("BARRELS")){
                barrelsFields.add(field);
            }
            else if(field.getName().startsWith("STEWS")){
                stewsFields.add(field);
            }
        }
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

    protected static void buildInventory(EditorMenu editorMenu, Set<Field> fields, String pathPrefix){
        for(Field field : fields){
            String path;

            try{
                path = (String) field.get(editorMenu);
            }catch(Exception ex){
                ex.printStackTrace();
                continue;
            }

            int slot = Integer.parseInt(field.getName().split("_")[2]);
            ItemBuilder itemBuilder = new ItemBuilder(Materials.CLOCK).withName("&6" +
                    EntityUtils.getFormattedType(path.replaceFirst(pathPrefix, "")
                            .replace("-", "_").replace(".", "_").replace(" ", "_"))
            );

            if(config.isBoolean(path))
                itemBuilder.withLore("&7Value: " + config.getBoolean(path));
            else if(config.isInt(path))
                itemBuilder.withLore("&7Value: " + config.getInt(path));
            else if(config.isDouble(path))
                itemBuilder.withLore("&7Value: " + config.getDouble(path));
            else if(config.isString(path))
                itemBuilder.withLore("&7Value: " + config.getString(path));
            else if(config.isList(path))
                itemBuilder.withLore("&7Value:", config.getStringList(path));
            else if(config.isConfigurationSection(path))
                itemBuilder.withLore("&7Value:", config.getConfigurationSection(path));

            editorMenu.inventory.setItem(slot, itemBuilder.build());
        }
    }

}
