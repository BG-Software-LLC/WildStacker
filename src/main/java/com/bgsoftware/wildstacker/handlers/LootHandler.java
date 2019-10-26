package com.bgsoftware.wildstacker.handlers;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.loot.LootTable;
import com.bgsoftware.wildstacker.loot.LootTableSheep;
import com.bgsoftware.wildstacker.loot.custom.LootTableCustom;
import com.bgsoftware.wildstacker.utils.FileUtils;
import com.bgsoftware.wildstacker.utils.legacy.EntityTypes;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Zombie;

import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings({"FieldCanBeLocal", "ResultOfMethodCallIgnored", "ConstantConditions"})
public final class LootHandler {

    private final Map<String, LootTable> lootTables = new HashMap<>();
    private LootTableCustom lootTableCustom = null;
    private final Gson gson = new Gson();

    public LootHandler(WildStackerPlugin plugin){
        WildStackerPlugin.log("Loading loot-tables started...");
        long startTime = System.currentTimeMillis();

        File folderFile = new File(plugin.getDataFolder(), "loottables");

        if(!folderFile.exists())
            folderFile.mkdirs();

        initAllLootTables();

        lootTables.put("EMPTY", new LootTable(new ArrayList<>(), -1, -1, -1, -1, true, false));

        for(File file : folderFile.listFiles()){
            try {
                JsonObject jsonObject = gson.fromJson(new FileReader(file), JsonObject.class);
                String key = file.getName().replace(".json", "").toUpperCase();
                lootTables.put(key, key.contains("SHEEP") ? LootTableSheep.fromJson(jsonObject, file.getName()) : LootTable.fromJson(jsonObject, file.getName()));
            }catch(Exception ex){
                WildStackerPlugin.log("[" + file.getName() + "] Couldn't load loot table:");
                WildStackerPlugin.log("    " + ex.getMessage());
            }
        }

        WildStackerPlugin.log("Loading loot-tables done (Took " + (System.currentTimeMillis() - startTime) + "ms)");
    }

    private void initAllLootTables(){
        FileUtils.saveResource("loottables/bat.json");
        FileUtils.saveResource("loottables/blaze.json");
        if(containsEntity("CAT"))
            FileUtils.saveResource("loottables/cat.json");
        FileUtils.saveResource("loottables/cave_spider.json");
        FileUtils.saveResource("loottables/chicken.json");
        if(containsEntity("COD"))
            FileUtils.saveResource("loottables/cod.json");
        FileUtils.saveResource("loottables/cow.json");
        FileUtils.saveResource("loottables/creeper.json");
        if(containsEntity("DOLPHIN"))
            FileUtils.saveResource("loottables/dolphin.json");
        FileUtils.saveResource("loottables/donkey.json");
        if(containsEntity("DROWNED"))
            FileUtils.saveResource("loottables/drowned.json");
        FileUtils.saveResource("loottables/elder_guardian.json");
        FileUtils.saveResource("loottables/ender_dragon.json");
        FileUtils.saveResource("loottables/enderman.json");
        FileUtils.saveResource("loottables/endermite.json");
        if(containsEntity("EVOKER"))
            FileUtils.saveResource("loottables/evoker.json");
        if(containsEntity("FOX"))
            FileUtils.saveResource("loottables/fox.json");
        FileUtils.saveResource("loottables/ghast.json");
        FileUtils.saveResource("loottables/giant.json");
        FileUtils.saveResource("loottables/guardian.json");
        FileUtils.saveResource("loottables/horse.json");
        if(containsEntity("HUSK"))
            FileUtils.saveResource("loottables/husk.json");
        if(containsEntity("ILLUSIONER"))
            FileUtils.saveResource("loottables/illusioner.json");
        FileUtils.saveResource("loottables/iron_golem.json");
        if(containsEntity("LLAMA"))
            FileUtils.saveResource("loottables/llama.json");
        FileUtils.saveResource("loottables/magma_cube.json");
        FileUtils.saveResource("loottables/mule.json");
        FileUtils.saveResource("loottables/mooshroom.json");
        FileUtils.saveResource("loottables/ocelot.json");
        if(containsEntity("PANDA"))
            FileUtils.saveResource("loottables/panda.json");
        if(containsEntity("PARROT"))
            FileUtils.saveResource("loottables/parrot.json");
        if(containsEntity("PHANTOM"))
            FileUtils.saveResource("loottables/phantom.json");
        FileUtils.saveResource("loottables/pig.json");
        if(containsEntity("PILLAGER"))
            FileUtils.saveResource("loottables/pillager.json");
        if(containsEntity("POLAR_BEAR"))
            FileUtils.saveResource("loottables/polar_bear.json");
        if(containsEntity("PUFFERFISH"))
            FileUtils.saveResource("loottables/pufferfish.json");
        FileUtils.saveResource("loottables/rabbit.json");
        if(containsEntity("RAVAGER"))
            FileUtils.saveResource("loottables/ravager.json");
        if(containsEntity("SALMON"))
            FileUtils.saveResource("loottables/salmon.json");
        FileUtils.saveResource("loottables/sheep.json");
        if(containsEntity("SHULKER"))
            FileUtils.saveResource("loottables/shulker.json");
        FileUtils.saveResource("loottables/silverfish.json");
        FileUtils.saveResource("loottables/skeleton.json");
        FileUtils.saveResource("loottables/skeleton_horse.json");
        FileUtils.saveResource("loottables/slime.json");
        FileUtils.saveResource("loottables/snowman.json");
        FileUtils.saveResource("loottables/spider.json");
        FileUtils.saveResource("loottables/squid.json");
        if(containsEntity("STRAY"))
            FileUtils.saveResource("loottables/stray.json");
        if(containsEntity("TRADER_LLAMA"))
            FileUtils.saveResource("loottables/trader_llama.json");
        if(containsEntity("TROPICAL_FISH"))
            FileUtils.saveResource("loottables/tropical_fish.json");
        if(containsEntity("TURTLE"))
            FileUtils.saveResource("loottables/turtle.json");
        if(containsEntity("VEX"))
            FileUtils.saveResource("loottables/vex.json");
        FileUtils.saveResource("loottables/villager.json");
        if(containsEntity("VINDICATOR"))
            FileUtils.saveResource("loottables/vindicator.json");
        if(containsEntity("WANDERING_TRADER"))
            FileUtils.saveResource("loottables/wandering_trader.json");
        FileUtils.saveResource("loottables/witch.json");
        FileUtils.saveResource("loottables/wither_skeleton.json");
        FileUtils.saveResource("loottables/wither.json");
        FileUtils.saveResource("loottables/wolf.json");
        FileUtils.saveResource("loottables/zombie.json");
        FileUtils.saveResource("loottables/zombie_horse.json");
        FileUtils.saveResource("loottables/zombie_pigman.json");
        FileUtils.saveResource("loottables/zombie_villager.json");
    }

    private boolean containsEntity(String entity){
        try{
            EntityType.valueOf(entity);
            return true;
        }catch(IllegalArgumentException ex){
            return false;
        }
    }

    public LootTable getLootTable(LivingEntity livingEntity){
        EntityTypes entityType = EntityTypes.fromEntity(livingEntity);
        String entityTypeName = entityType.name();

        if((livingEntity instanceof Ageable && !((Ageable) livingEntity).isAdult()) ||
                ((livingEntity instanceof Zombie) && ((Zombie) livingEntity).isBaby()))
            entityTypeName += "_BABY";

        return lootTables.getOrDefault(entityTypeName, lootTables.get("EMPTY"));
    }

    public LootTableCustom getLootTableCustom(){
        return lootTableCustom;
    }

    public static void reload(){
        try{
            WildStackerPlugin plugin = WildStackerPlugin.getPlugin();
            Field field = WildStackerPlugin.class.getDeclaredField("lootHandler");
            field.setAccessible(true);
            field.set(plugin, new LootHandler(plugin));
        }catch(NoSuchFieldException | IllegalAccessException ex){
            ex.printStackTrace();
        }
    }

}
