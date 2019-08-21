package com.bgsoftware.wildstacker.handlers;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.loot.LootTable;
import com.bgsoftware.wildstacker.loot.LootTableSheep;
import com.bgsoftware.wildstacker.loot.custom.LootTableCustom;
import com.bgsoftware.wildstacker.loot.custom.LootTableCustomDrops;
import com.bgsoftware.wildstacker.loot.custom.LootTableDropEdit;
import com.bgsoftware.wildstacker.loot.custom.LootTableEditDrops;
import com.bgsoftware.wildstacker.loot.custom.LootTableEpicSpawners;
import com.bgsoftware.wildstacker.loot.custom.LootTableStackSpawners;
import com.bgsoftware.wildstacker.utils.FileUtil;
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
        File folderFile = new File(plugin.getDataFolder(), "loottables");

        if(!folderFile.exists())
            folderFile.mkdirs();

        initAllLootTables();

        lootTables.put("EMPTY", new LootTable(new ArrayList<>(), -1, -1, -1, -1, true));

        for(File file : folderFile.listFiles()){
            try {
                JsonObject jsonObject = gson.fromJson(new FileReader(file), JsonObject.class);
                String key = file.getName().replace(".json", "").toUpperCase();
                lootTables.put(key, key.contains("SHEEP") ? LootTableSheep.fromJson(jsonObject) : LootTable.fromJson(jsonObject));
            }catch(Exception ex){
                System.out.println("Couldn't load " + file.getName());
                ex.printStackTrace();
            }
        }
    }

    private void initAllLootTables(){
        FileUtil.saveResource("loottables/bat.json");
        FileUtil.saveResource("loottables/blaze.json");
        if(containsEntity("CAT"))
            FileUtil.saveResource("loottables/cat.json");
        FileUtil.saveResource("loottables/cave_spider.json");
        FileUtil.saveResource("loottables/chicken.json");
        if(containsEntity("COD"))
            FileUtil.saveResource("loottables/cod.json");
        FileUtil.saveResource("loottables/cow.json");
        FileUtil.saveResource("loottables/creeper.json");
        if(containsEntity("DOLPHIN"))
            FileUtil.saveResource("loottables/dolphin.json");
        FileUtil.saveResource("loottables/donkey.json");
        if(containsEntity("DROWNED"))
            FileUtil.saveResource("loottables/drowned.json");
        FileUtil.saveResource("loottables/elder_guardian.json");
        FileUtil.saveResource("loottables/ender_dragon.json");
        FileUtil.saveResource("loottables/enderman.json");
        FileUtil.saveResource("loottables/endermite.json");
        if(containsEntity("EVOKER"))
            FileUtil.saveResource("loottables/evoker.json");
        if(containsEntity("FOX"))
            FileUtil.saveResource("loottables/fox.json");
        FileUtil.saveResource("loottables/ghast.json");
        FileUtil.saveResource("loottables/giant.json");
        FileUtil.saveResource("loottables/guardian.json");
        FileUtil.saveResource("loottables/horse.json");
        if(containsEntity("HUSK"))
            FileUtil.saveResource("loottables/husk.json");
        if(containsEntity("ILLUSIONER"))
            FileUtil.saveResource("loottables/illusioner.json");
        FileUtil.saveResource("loottables/iron_golem.json");
        if(containsEntity("LLAMA"))
            FileUtil.saveResource("loottables/llama.json");
        FileUtil.saveResource("loottables/magma_cube.json");
        FileUtil.saveResource("loottables/mule.json");
        FileUtil.saveResource("loottables/mooshroom.json");
        FileUtil.saveResource("loottables/ocelot.json");
        if(containsEntity("PANDA"))
            FileUtil.saveResource("loottables/panda.json");
        if(containsEntity("PARROT"))
            FileUtil.saveResource("loottables/parrot.json");
        if(containsEntity("PHANTOM"))
            FileUtil.saveResource("loottables/phantom.json");
        FileUtil.saveResource("loottables/pig.json");
        if(containsEntity("PILLAGER"))
            FileUtil.saveResource("loottables/pillager.json");
        if(containsEntity("POLAR_BEAR"))
            FileUtil.saveResource("loottables/polar_bear.json");
        if(containsEntity("PUFFERFISH"))
            FileUtil.saveResource("loottables/pufferfish.json");
        FileUtil.saveResource("loottables/rabbit.json");
        if(containsEntity("RAVAGER"))
            FileUtil.saveResource("loottables/ravager.json");
        if(containsEntity("SALMON"))
            FileUtil.saveResource("loottables/salmon.json");
        FileUtil.saveResource("loottables/sheep.json");
        if(containsEntity("SHULKER"))
            FileUtil.saveResource("loottables/shulker.json");
        FileUtil.saveResource("loottables/silverfish.json");
        FileUtil.saveResource("loottables/skeleton.json");
        FileUtil.saveResource("loottables/skeleton_horse.json");
        FileUtil.saveResource("loottables/slime.json");
        FileUtil.saveResource("loottables/snowman.json");
        FileUtil.saveResource("loottables/spider.json");
        FileUtil.saveResource("loottables/squid.json");
        if(containsEntity("STRAY"))
            FileUtil.saveResource("loottables/stray.json");
        if(containsEntity("TRADER_LLAMA"))
            FileUtil.saveResource("loottables/trader_llama.json");
        if(containsEntity("TROPICAL_FISH"))
            FileUtil.saveResource("loottables/tropical_fish.json");
        if(containsEntity("TURTLE"))
            FileUtil.saveResource("loottables/turtle.json");
        if(containsEntity("VEX"))
            FileUtil.saveResource("loottables/vex.json");
        FileUtil.saveResource("loottables/villager.json");
        if(containsEntity("VINDICATOR"))
            FileUtil.saveResource("loottables/vindicator.json");
        if(containsEntity("WANDERING_TRADER"))
            FileUtil.saveResource("loottables/wandering_trader.json");
        FileUtil.saveResource("loottables/witch.json");
        FileUtil.saveResource("loottables/wither_skeleton.json");
        FileUtil.saveResource("loottables/wither.json");
        FileUtil.saveResource("loottables/wolf.json");
        FileUtil.saveResource("loottables/zombie.json");
        FileUtil.saveResource("loottables/zombie_horse.json");
        FileUtil.saveResource("loottables/zombie_pigman.json");
        FileUtil.saveResource("loottables/zombie_villager.json");
    }

    @SuppressWarnings("WeakerAccess")
    public void initLootTableCustom(ProvidersHandler.DropsProvider dropsProvider){
        switch (dropsProvider){
            case CUSTOM_DROPS:
                lootTableCustom = new LootTableCustomDrops();
                break;
            case DROP_EDIT:
                lootTableCustom = new LootTableDropEdit();
                break;
            case EDIT_DROPS:
                lootTableCustom = new LootTableEditDrops();
                break;
            case EPIC_SPAWNERS:
                lootTableCustom = new LootTableEpicSpawners();
                break;
            case STACK_SPAWNERS:
                lootTableCustom = new LootTableStackSpawners();
                break;
        }
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
            plugin.getLootHandler().initLootTableCustom(plugin.getProviders().getDropsProvider());
        }catch(NoSuchFieldException | IllegalAccessException ex){
            ex.printStackTrace();
        }
    }

}
