package com.bgsoftware.wildstacker.handlers;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.loot.LootTable;
import com.bgsoftware.wildstacker.loot.LootTableSheep;
import com.bgsoftware.wildstacker.utils.ServerVersion;
import com.bgsoftware.wildstacker.utils.files.FileUtils;
import com.bgsoftware.wildstacker.utils.legacy.EntityTypes;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Zombie;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings({"FieldCanBeLocal", "ResultOfMethodCallIgnored", "ConstantConditions"})
public final class LootHandler {

    private final Map<String, LootTable> lootTables = new HashMap<>();

    public LootHandler(WildStackerPlugin plugin) {
        WildStackerPlugin.log("Loading loot-tables started...");
        long startTime = System.currentTimeMillis();

        File folderFile = new File(plugin.getDataFolder(), "loottables");

        if (!folderFile.exists())
            folderFile.mkdirs();

        initAllLootTables();

        lootTables.put("EMPTY", new LootTable(new ArrayList<>(), -1, -1, -1, -1, true, false));

        for (File file : folderFile.listFiles()) {
            try {
                JSONParser jsonParser = new JSONParser();
                JSONObject jsonObject = (JSONObject) jsonParser.parse(new FileReader(file));
                String key = file.getName().replace(".json", "").toUpperCase();

                if (!isValidEntityType(key.replace("_BABY", ""))) {
                    WildStackerPlugin.log("&cWarning: The file " + file.getName() + " doesn't seem like a valid loot table name.");
                    WildStackerPlugin.log("&cDetected entity of this file is " + key);
                }

                lootTables.put(key, key.contains("SHEEP") ? LootTableSheep.fromJson(jsonObject, file.getName()) : LootTable.fromJson(jsonObject, file.getName()));
            } catch (Exception ex) {
                ex.printStackTrace();
                WildStackerPlugin.log("[" + file.getName() + "] Couldn't load loot table:");
                WildStackerPlugin.log("    " + ex.getMessage());
            }
        }

        WildStackerPlugin.log("Loading loot-tables done (Took " + (System.currentTimeMillis() - startTime) + "ms)");
    }

    private static boolean isValidEntityType(String entityType) {
        try {
            EntityTypes.fromName(entityType.replace("_baby", ""));
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    public static void reload() {
        WildStackerPlugin plugin = WildStackerPlugin.getPlugin();
        plugin.setLootHandler(new LootHandler(plugin));
    }

    private void initAllLootTables() {
        saveLootTable(EntityTypes.ALLAY, "allay");
        saveLootTable(EntityTypes.AXOLOTL, "axolotl", "axolotl_baby");
        saveLootTable("bat");
        saveLootTable(EntityTypes.BEE, "bee", "bee_baby");
        saveLootTable("blaze");
        saveLootTable(EntityTypes.BOGGED, "bogged");
        saveLootTable(EntityTypes.BREEZE, "breeze");
        saveLootTable(EntityTypes.CAMEL, "camel", "camel_baby");
        saveLootTable(EntityTypes.CAT, "cat", "cat_baby");
        saveLootTable("cave_spider");
        saveLootTable("chicken");
        saveLootTable("chicken_baby");
        saveLootTable(EntityTypes.COD, "cod");
        saveLootTable("cow");
        saveLootTable("cow_baby");
        saveLootTable("creeper");
        saveLootTable(EntityTypes.DOLPHIN, "dolphin");
        saveLootTable("donkey", "donkey_baby");
        saveLootTable(EntityTypes.DROWNED, "drowned");
        saveLootTable("elder_guardian");
        saveLootTable("ender_dragon");
        saveLootTable("enderman");
        saveLootTable("endermite");
        saveLootTable(EntityTypes.EVOKER, "evoker");
        saveLootTable(EntityTypes.FOX, "fox", "fox_baby");
        saveLootTable(EntityTypes.FROG, "frog");
        saveLootTable("ghast");
        saveLootTable("giant");
        saveLootTable(EntityTypes.GLOW_SQUID, "glow_squid");
        saveLootTable(EntityTypes.GOAT, "goat", "goat_baby");
        saveLootTable("guardian");
        saveLootTable(EntityTypes.HOGLIN, "hoglin", "hoglin_baby");
        saveLootTable("horse");
        saveLootTable("horse_baby");
        saveLootTable(EntityTypes.HUSK, "husk");
        saveLootTable(EntityTypes.ILLUSIONER, "illusioner");
        saveLootTable("iron_golem");
        saveLootTable(EntityTypes.LLAMA, "llama");
        if (ServerVersion.isAtLeast(ServerVersion.v1_19)) {
            FileUtils.saveResource("loottables/magma_cube1_19.json", "loottables/magma_cube.json");
        } else {
            saveLootTable("magma_cube");
        }
        saveLootTable("mooshroom");
        saveLootTable("mooshroom_baby");
        saveLootTable("mule");
        saveLootTable("mule_baby");
        saveLootTable("ocelot");
        saveLootTable("ocelot_baby");
        saveLootTable(EntityTypes.PANDA, "panda", "panda_baby");
        saveLootTable(EntityTypes.PARROT, "parrot");
        saveLootTable(EntityTypes.PHANTOM, "phantom");
        saveLootTable("pig");
        saveLootTable("pig_baby");
        saveLootTable(EntityTypes.PIGLIN, "piglin", "piglin_baby");
        saveLootTable(EntityTypes.PIGLIN_BRUTE, "piglin_brute");
        saveLootTable(EntityTypes.PILLAGER, "pillager");
        saveLootTable(EntityTypes.POLAR_BEAR, "polar_bear", "polar_bear_baby");
        saveLootTable(EntityTypes.PUFFERFISH, "pufferfish");
        saveLootTable("rabbit");
        saveLootTable("rabbit_baby");
        saveLootTable(EntityTypes.RAVAGER, "ravager");
        saveLootTable(EntityTypes.SALMON, "salmon");
        saveLootTable("sheep");
        saveLootTable("sheep_baby");
        saveLootTable(EntityTypes.SHULKER, "shulker");
        saveLootTable("silverfish");
        saveLootTable("skeleton");
        saveLootTable("skeleton_horse");
        saveLootTable("slime");
        saveLootTable(EntityTypes.SNIFFER, "sniffer", "sniffer_baby");
        saveLootTable("snowman");
        saveLootTable("spider");
        saveLootTable("squid");
        saveLootTable(EntityTypes.STRAY, "stray");
        saveLootTable(EntityTypes.STRIDER, "strider", "strider_baby");
        saveLootTable(EntityTypes.TADPOLE, "tadpole");
        saveLootTable(EntityTypes.TRADER_LLAMA, "trader_llama");
        saveLootTable(EntityTypes.TROPICAL_FISH, "tropical_fish");
        saveLootTable(EntityTypes.TURTLE, "turtle", "turtle_baby");
        saveLootTable(EntityTypes.VEX, "vex");
        saveLootTable("villager");
        saveLootTable("villager_baby");
        saveLootTable(EntityTypes.VINDICATOR, "vindicator");
        saveLootTable(EntityTypes.WANDERING_TRADER, "wandering_trader");
        saveLootTable(EntityTypes.WARDEN, "warden");
        saveLootTable("witch");
        saveLootTable("wither_skeleton");
        saveLootTable("wither");
        saveLootTable("wolf");
        saveLootTable("wolf_baby");
        saveLootTable(EntityTypes.ZOGLIN, "zoglin");
        saveLootTable("zombie");
        saveLootTable("zombie_baby");
        saveLootTable("zombie_horse");
        saveLootTable("zombie_pigman");
        saveLootTable("zombie_pigman_baby");
        saveLootTable("zombie_villager");
    }

    public LootTable getLootTable(LivingEntity livingEntity) {
        EntityTypes entityType = EntityTypes.fromEntity(livingEntity);
        String entityTypeName = entityType.name();

        if ((livingEntity instanceof Ageable && !((Ageable) livingEntity).isAdult()) ||
                ((livingEntity instanceof Zombie) && ((Zombie) livingEntity).isBaby()))
            entityTypeName += "_BABY";

        return lootTables.getOrDefault(entityTypeName, lootTables.get("EMPTY"));
    }

    private static void saveLootTable(EntityTypes entityType, String... lootTableNames) {
        if (containsEntity(entityType.name()))
            saveLootTable(lootTableNames);
    }

    private static void saveLootTable(String... lootTableNames) {
        for (String lootTableName : lootTableNames)
            FileUtils.saveResource("loottables/" + lootTableName + ".json");
    }

    private static boolean containsEntity(String entity) {
        try {
            EntityType.valueOf(entity);
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

}
