package com.bgsoftware.wildstacker.handlers;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.config.CommentedConfiguration;
import com.bgsoftware.wildstacker.config.ConfigComments;
import com.bgsoftware.wildstacker.utils.threads.Executor;
import com.bgsoftware.wildstacker.utils.items.ItemBuilder;
import com.bgsoftware.wildstacker.utils.legacy.Materials;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("unused")
public final class EditorHandler {

    private final Set<Field> generalFields = new HashSet<>(), itemsFields = new HashSet<>(), entitiesFields = new HashSet<>(),
            bucketsFields = new HashSet<>(), spawnersFields = new HashSet<>(), barrelsFields = new HashSet<>(), stewsFields = new HashSet<>();

    private WildStackerPlugin plugin;
    private ItemStack[] settingsEditor;

    public CommentedConfiguration config;

    public final static String GENERAL_SLOT_0 = "give-item-name", GENERAL_SLOT_1 = "database.delete-invalid-worlds",
            GENERAL_SLOT_2 = "database.delete-invalid-blocks", GENERAL_SLOT_3 = "kill-task.interval",
            GENERAL_SLOT_4 = "kill-task.stacked-entities", GENERAL_SLOT_5 = "kill-task.unstacked-entities",
            GENERAL_SLOT_6 = "kill-task.stacked-items", GENERAL_SLOT_7 = "kill-task.unstacked-items",
            GENERAL_SLOT_8 = "kill-task.sync-clear-lagg", GENERAL_SLOT_9 = "kill-task.whitelist";

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
            ENTITIES_SLOT_20 = "entities.nerfed-entities.worlds", ENTITIES_SLOT_21 = "entities.stack-down.enabled",
            ENTITIES_SLOT_22 = "entities.stack-down.stack-down-types", ENTITIES_SLOT_23 = "entities.keep-fire",
            ENTITIES_SLOT_24 = "entities.mythic-mobs-custom-name", ENTITIES_SLOT_25 = "entities.keep-lowest-health",
            ENTITIES_SLOT_26 = "entities.stack-after-breed", ENTITIES_SLOT_27 = "entities.hide-names",
            ENTITIES_SLOT_28 = "entities.next-stack-knockback", ENTITIES_SLOT_29 = "entities.default-unstack",
            ENTITIES_SLOT_30 = "entities.auto-exp-pickup";

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
            SPAWNERS_SLOT_30 = "spawners.shift-place-stack", SPAWNERS_SLOT_31 = "spawners.break-charge.amount",
            SPAWNERS_SLOT_32 = "spawners.break-charge.multiply-stack-amount", SPAWNERS_SLOT_33 = "spawners.place-charge.amount",
            SPAWNERS_SLOT_34 = "spawners.place-charge.multiply-stack-amount", SPAWNERS_SLOT_35 = "spawners.change-using-eggs",
            SPAWNERS_SLOT_36 = "spawners.eggs-stack-multiply", SPAWNERS_SLOT_37 = "spawners.next-spawner-placement",
            SPAWNERS_SLOT_38 = "spawners.only-one-spawner";

    public final static String BARRELS_SLOT_0 = "barrels.enabled", BARRELS_SLOT_1 = "barrels.merge-radius",
            BARRELS_SLOT_2 = "barrels.custom-name", BARRELS_SLOT_3 = "barrels.blacklist", BARRELS_SLOT_4 = "barrels.whitelist",
            BARRELS_SLOT_5 = "barrels.limits", BARRELS_SLOT_6 = "barrels.disabled-worlds", BARRELS_SLOT_7 = "barrels.chunk-limit",
            BARRELS_SLOT_8 = "barrels.particles", BARRELS_SLOT_9 = "barrels.chunk-merge", BARRELS_SLOT_10 = "barrels.explosions-break-stack",
            BARRELS_SLOT_11 = "barrels.toggle-command.enabled", BARRELS_SLOT_12 = "barrels.toggle-command.command",
            BARRELS_SLOT_13 = "barrels.place-inventory.enabled", BARRELS_SLOT_14 = "barrels.place-inventory.title",
            BARRELS_SLOT_15 = "barrels.required-permission";

    public final static String STEWS_SLOT_0 = "stews.enabled", STEWS_SLOT_1 = "stews.max-stack";

    public EditorHandler(WildStackerPlugin plugin){
        this.plugin = plugin;
        this.config = new CommentedConfiguration(ConfigComments.class, new File(plugin.getDataFolder(), "config.yml"));

        for(Field field : getClass().getDeclaredFields()){
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

        loadSettingsEditor();
    }

    public void saveConfiguration(){
        config.save(new File(plugin.getDataFolder(), "config.yml"));
        SettingsHandler.reload();
    }

    public void reloadConfiguration(){
        config.load(new File(plugin.getDataFolder(), "config.yml"));
    }

    public Inventory getSettingsEditor(){
        Inventory editor = Bukkit.createInventory(null, settingsEditor.length, "" + ChatColor.GOLD + ChatColor.BOLD + "WildStacker");
        editor.setContents(settingsEditor);
        return editor;
    }

    public void openGeneralEditor(Player player){
        if(Bukkit.isPrimaryThread()){
            Executor.async(() -> openGeneralEditor(player));
            return;
        }

        Inventory editor = Bukkit.createInventory(null, 18, "" + ChatColor.DARK_GRAY + ChatColor.BOLD + "General Settings");
        buildInventory(editor, generalFields, "");

        Executor.sync(() -> player.openInventory(editor));
    }

    public void openItemsEditor(Player player){
        if(Bukkit.isPrimaryThread()){
            Executor.async(() -> openItemsEditor(player));
            return;
        }

        Inventory editor = Bukkit.createInventory(null, 18, "" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Items Settings");
        buildInventory(editor, itemsFields, "items.");

        Executor.sync(() -> player.openInventory(editor));
    }

    public void openEntitiesEditor(Player player){
        if(Bukkit.isPrimaryThread()){
            Executor.async(() -> openEntitiesEditor(player));
            return;
        }

        Inventory editor = Bukkit.createInventory(null, 9 * 4, "" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Entities Settings");
        buildInventory(editor, entitiesFields, "entities.");

        Executor.sync(() -> player.openInventory(editor));
    }

    public void openBucketsEditor(Player player){
        if(Bukkit.isPrimaryThread()){
            Executor.async(() -> openBucketsEditor(player));
            return;
        }

        Inventory editor = Bukkit.createInventory(null, 9, "" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Buckets Settings");
        buildInventory(editor, bucketsFields, "buckets.");

        Executor.sync(() -> player.openInventory(editor));
    }

    public void openSpawnersEditor(Player player){
        if(Bukkit.isPrimaryThread()){
            Executor.async(() -> openSpawnersEditor(player));
            return;
        }

        Inventory editor = Bukkit.createInventory(null, 9 * 5, "" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Spawners Settings");
        buildInventory(editor, spawnersFields, "spawners.");

        Executor.sync(() -> player.openInventory(editor));
    }

    public void openBarrelsEditor(Player player){
        if(Bukkit.isPrimaryThread()){
            Executor.async(() -> openBarrelsEditor(player));
            return;
        }

        Inventory editor = Bukkit.createInventory(null, 9 * 2, "" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Barrels Settings");
        buildInventory(editor, barrelsFields, "barrels.");

        Executor.sync(() -> player.openInventory(editor));
    }

    public void openStewsEditor(Player player){
        if(Bukkit.isPrimaryThread()){
            Executor.async(() -> openStewsEditor(player));
            return;
        }

        Inventory editor = Bukkit.createInventory(null, 9, "" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Stews Settings");
        buildInventory(editor, stewsFields, "stews.");

        Executor.sync(() -> player.openInventory(editor));
    }

    public void openEditor(Player player, String editor){
        switch (editor){
            case "generalEditor":
                openGeneralEditor(player);
                break;
            case "itemsEditor":
                openItemsEditor(player);
                break;
            case "entitiesEditor":
                openEntitiesEditor(player);
                break;
            case "bucketsEditor":
                openBucketsEditor(player);
                break;
            case "spawnersEditor":
                openSpawnersEditor(player);
                break;
            case "barrelsEditor":
                openBarrelsEditor(player);
                break;
            case "stewsEditor":
                openStewsEditor(player);
        }
    }

    private void loadSettingsEditor(){
        Inventory editor = Bukkit.createInventory(null, 9 * 6);

        ItemStack glassPane = new ItemBuilder(Materials.BLACK_STAINED_GLASS_PANE).withName("&6").build();

        for(int i = 0; i < 9; i++)
            editor.setItem(i, glassPane);

        for(int i = 45; i < 54; i++)
            editor.setItem(i, glassPane);

        editor.setItem(9, glassPane);
        editor.setItem(17, glassPane);
        editor.setItem(18, glassPane);
        editor.setItem(26, glassPane);
        editor.setItem(27, glassPane);
        editor.setItem(35, glassPane);
        editor.setItem(36, glassPane);
        editor.setItem(44, glassPane);

        editor.setItem(19, new ItemBuilder(Materials.MAP)
                .withName("&6General Settings").withLore("&7Click to edit general settings.").build());

        editor.setItem(21, new ItemBuilder(Material.GOLD_INGOT)
                .withName("&6Items Settings").withLore("&7Click to edit items settings.").build());

        editor.setItem(23, new ItemBuilder(Material.ROTTEN_FLESH)
                .withName("&6Entities Settings").withLore("&7Click to edit entities settings.").build());

        editor.setItem(25, new ItemBuilder(Material.BUCKET)
                .withName("&6Buckets Settings").withLore("&7Click to edit buckets settings.").build());

        editor.setItem(29, new ItemBuilder(Materials.SPAWNER)
                .withName("&6Spawner Settings").withLore("&7Click to edit spawners settings.").build());

        editor.setItem(31, new ItemBuilder(Materials.CAULDRON)
                .withName("&6Barrels Settings").withLore("&7Click to edit barrels settings.").build());

        editor.setItem(33, new ItemBuilder(Materials.MUSHROOM_STEW)
                .withName("&6Stews Settings").withLore("&7Click to edit stews settings.").build());

        editor.setItem(49, new ItemBuilder(Material.EMERALD)
                .withName("&aSave Changes").withLore("&7Click to save all changes.").build());

        settingsEditor = editor.getContents();
    }

    private String getFormattedName(String name){
        StringBuilder stringBuilder = new StringBuilder();

        name = name.replace("-", " ").replace(".", "_").replace(" ", "_");

        for (String section : name.split("_")) {
            stringBuilder.append(section.substring(0, 1).toUpperCase()).append(section.substring(1).toLowerCase()).append(" ");
        }

        return stringBuilder.substring(0, stringBuilder.length() - 1);
    }

    private void buildInventory(Inventory inventory, Set<Field> fields, String pathPrefix){
        for(Field field : fields){
            String path;

            try{
                path = (String) field.get(this);
            }catch(Exception ex){
                ex.printStackTrace();
                continue;
            }

            int slot = Integer.parseInt(field.getName().split("_")[2]);
            ItemBuilder itemBuilder = new ItemBuilder(Materials.CLOCK).withName("&6" + getFormattedName(path.replaceFirst(pathPrefix, "")));

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

            inventory.setItem(slot, itemBuilder.build());
        }
    }

}
