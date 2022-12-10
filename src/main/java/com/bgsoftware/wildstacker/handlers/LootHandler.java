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

                if(!isValidEntityType(key.replace("_BABY", ""))) {
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
        if (containsEntity("ALLAY"))
            FileUtils.saveResource("loottables/allay.json");
        if (containsEntity("AXOLOTL")) {
            FileUtils.saveResource("loottables/axolotl.json");
            FileUtils.saveResource("loottables/axolotl_baby.json");
        }
        FileUtils.saveResource("loottables/bat.json");
        if (containsEntity("BEE")) {
            FileUtils.saveResource("loottables/bee.json");
            FileUtils.saveResource("loottables/bee_baby.json");
        }
        FileUtils.saveResource("loottables/blaze.json");
        if(containsEntity("CAMEL")) {
            FileUtils.saveResource("loottables/camel.json");
            FileUtils.saveResource("loottables/camel_baby.json");
        }
        if (containsEntity("CAT")) {
            FileUtils.saveResource("loottables/cat.json");
            FileUtils.saveResource("loottables/cat_baby.json");
        }
        FileUtils.saveResource("loottables/cave_spider.json");
        FileUtils.saveResource("loottables/chicken.json");
        FileUtils.saveResource("loottables/chicken_baby.json");
        if (containsEntity("COD"))
            FileUtils.saveResource("loottables/cod.json");
        FileUtils.saveResource("loottables/cow.json");
        FileUtils.saveResource("loottables/cow_baby.json");
        FileUtils.saveResource("loottables/creeper.json");
        if (containsEntity("DOLPHIN"))
            FileUtils.saveResource("loottables/dolphin.json");
        FileUtils.saveResource("loottables/donkey.json");
        FileUtils.saveResource("loottables/donkey_baby.json");
        if (containsEntity("DROWNED"))
            FileUtils.saveResource("loottables/drowned.json");
        FileUtils.saveResource("loottables/elder_guardian.json");
        FileUtils.saveResource("loottables/ender_dragon.json");
        FileUtils.saveResource("loottables/enderman.json");
        FileUtils.saveResource("loottables/endermite.json");
        if (containsEntity("EVOKER"))
            FileUtils.saveResource("loottables/evoker.json");
        if (containsEntity("FOX")) {
            FileUtils.saveResource("loottables/fox.json");
            FileUtils.saveResource("loottables/fox_baby.json");
        }
        if (containsEntity("FROG"))
            FileUtils.saveResource("loottables/frog.json");
        FileUtils.saveResource("loottables/ghast.json");
        FileUtils.saveResource("loottables/giant.json");
        if (containsEntity("GLOW_SQUID")) {
            FileUtils.saveResource("loottables/glow_squid.json");
        }
        if (containsEntity("GOAT")) {
            FileUtils.saveResource("loottables/goat.json");
            FileUtils.saveResource("loottables/goat_baby.json");
        }
        FileUtils.saveResource("loottables/guardian.json");
        if (containsEntity("HOGLIN")) {
            FileUtils.saveResource("loottables/hoglin.json");
            FileUtils.saveResource("loottables/hoglin_baby.json");
        }
        FileUtils.saveResource("loottables/horse.json");
        FileUtils.saveResource("loottables/horse_baby.json");
        if (containsEntity("HUSK"))
            FileUtils.saveResource("loottables/husk.json");
        if (containsEntity("ILLUSIONER"))
            FileUtils.saveResource("loottables/illusioner.json");
        FileUtils.saveResource("loottables/iron_golem.json");
        if (containsEntity("LLAMA"))
            FileUtils.saveResource("loottables/llama.json");
        if(ServerVersion.isAtLeast(ServerVersion.v1_19)) {
            FileUtils.saveResource("loottables/magma_cube1_19.json", "loottables/magma_cube.json");
        } else {
            FileUtils.saveResource("loottables/magma_cube.json");
        }
        FileUtils.saveResource("loottables/mooshroom.json");
        FileUtils.saveResource("loottables/mooshroom_baby.json");
        FileUtils.saveResource("loottables/mule.json");
        FileUtils.saveResource("loottables/mule_baby.json");
        FileUtils.saveResource("loottables/ocelot.json");
        FileUtils.saveResource("loottables/ocelot_baby.json");
        if (containsEntity("PANDA")) {
            FileUtils.saveResource("loottables/panda.json");
            FileUtils.saveResource("loottables/panda_baby.json");
        }
        if (containsEntity("PARROT"))
            FileUtils.saveResource("loottables/parrot.json");
        if (containsEntity("PHANTOM"))
            FileUtils.saveResource("loottables/phantom.json");
        FileUtils.saveResource("loottables/pig.json");
        FileUtils.saveResource("loottables/pig_baby.json");
        if (containsEntity("PIGLIN")) {
            FileUtils.saveResource("loottables/piglin.json");
            FileUtils.saveResource("loottables/piglin_baby.json");
        }
        if (containsEntity("PIGLIN_BRUTE"))
            FileUtils.saveResource("loottables/piglin_brute.json");
        if (containsEntity("PILLAGER"))
            FileUtils.saveResource("loottables/pillager.json");
        if (containsEntity("POLAR_BEAR")) {
            FileUtils.saveResource("loottables/polar_bear.json");
            FileUtils.saveResource("loottables/polar_bear_baby.json");
        }
        if (containsEntity("PUFFERFISH"))
            FileUtils.saveResource("loottables/pufferfish.json");
        FileUtils.saveResource("loottables/rabbit.json");
        FileUtils.saveResource("loottables/rabbit_baby.json");
        if (containsEntity("RAVAGER"))
            FileUtils.saveResource("loottables/ravager.json");
        if (containsEntity("SALMON"))
            FileUtils.saveResource("loottables/salmon.json");
        FileUtils.saveResource("loottables/sheep.json");
        FileUtils.saveResource("loottables/sheep_baby.json");
        if (containsEntity("SHULKER"))
            FileUtils.saveResource("loottables/shulker.json");
        FileUtils.saveResource("loottables/silverfish.json");
        FileUtils.saveResource("loottables/skeleton.json");
        FileUtils.saveResource("loottables/skeleton_horse.json");
        FileUtils.saveResource("loottables/slime.json");
        FileUtils.saveResource("loottables/snowman.json");
        FileUtils.saveResource("loottables/spider.json");
        FileUtils.saveResource("loottables/squid.json");
        if (containsEntity("STRAY"))
            FileUtils.saveResource("loottables/stray.json");
        if (containsEntity("STRIDER")) {
            FileUtils.saveResource("loottables/strider.json");
            FileUtils.saveResource("loottables/strider_baby.json");
        }
        if (containsEntity("TADPOLE"))
            FileUtils.saveResource("loottables/tadpole.json");
        if (containsEntity("TRADER_LLAMA"))
            FileUtils.saveResource("loottables/trader_llama.json");
        if (containsEntity("TROPICAL_FISH"))
            FileUtils.saveResource("loottables/tropical_fish.json");
        if (containsEntity("TURTLE")) {
            FileUtils.saveResource("loottables/turtle.json");
            FileUtils.saveResource("loottables/turtle_baby.json");
        }
        if (containsEntity("VEX"))
            FileUtils.saveResource("loottables/vex.json");
        FileUtils.saveResource("loottables/villager.json");
        FileUtils.saveResource("loottables/villager_baby.json");
        if (containsEntity("VINDICATOR"))
            FileUtils.saveResource("loottables/vindicator.json");
        if (containsEntity("WANDERING_TRADER"))
            FileUtils.saveResource("loottables/wandering_trader.json");
        if (containsEntity("WARDEN"))
            FileUtils.saveResource("loottables/warden.json");
        FileUtils.saveResource("loottables/witch.json");
        FileUtils.saveResource("loottables/wither_skeleton.json");
        FileUtils.saveResource("loottables/wither.json");
        FileUtils.saveResource("loottables/wolf.json");
        FileUtils.saveResource("loottables/wolf_baby.json");
        if (containsEntity("ZOGLIN"))
            FileUtils.saveResource("loottables/zoglin.json");
        FileUtils.saveResource("loottables/zombie.json");
        FileUtils.saveResource("loottables/zombie_baby.json");
        FileUtils.saveResource("loottables/zombie_horse.json");
        FileUtils.saveResource("loottables/zombie_pigman.json");
        FileUtils.saveResource("loottables/zombie_pigman_baby.json");
        FileUtils.saveResource("loottables/zombie_villager.json");
    }

    private boolean containsEntity(String entity) {
        try {
            EntityType.valueOf(entity);
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    public LootTable getLootTable(LivingEntity livingEntity) {
        EntityTypes entityType = EntityTypes.fromEntity(livingEntity);
        String entityTypeName = entityType.name();

        if ((livingEntity instanceof Ageable && !((Ageable) livingEntity).isAdult()) ||
                ((livingEntity instanceof Zombie) && ((Zombie) livingEntity).isBaby()))
            entityTypeName += "_BABY";

        return lootTables.getOrDefault(entityTypeName, lootTables.get("EMPTY"));
    }

}
