package xyz.wildseries.wildstacker.loot;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.bukkit.entity.LivingEntity;
import xyz.wildseries.wildstacker.WildStackerPlugin;
import xyz.wildseries.wildstacker.handlers.ProvidersHandler;
import xyz.wildseries.wildstacker.loot.custom.LootTableCustom;
import xyz.wildseries.wildstacker.loot.custom.LootTableCustomDrops;
import xyz.wildseries.wildstacker.loot.custom.LootTableDropEdit;
import xyz.wildseries.wildstacker.loot.custom.LootTableEditDrops;
import xyz.wildseries.wildstacker.loot.custom.LootTableEpicSpawners;
import xyz.wildseries.wildstacker.loot.custom.LootTableStackSpawners;
import xyz.wildseries.wildstacker.utils.legacy.EntityTypes;

import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings({"FieldCanBeLocal", "ResultOfMethodCallIgnored", "ConstantConditions"})
public class LootHandler {

    private final Map<String, LootTable> lootTables = new HashMap<>();
    private LootTableCustom lootTableCustom = null;
    private final Gson gson = new Gson();

    public LootHandler(WildStackerPlugin plugin){
        File folderFile = new File(plugin.getDataFolder(), "loottables");

        if(!folderFile.exists()){
            folderFile.mkdirs();
            initAllLootTables(plugin);
        }
        else{
            for(File file : folderFile.listFiles()){
                try {
                    JsonObject jsonObject = gson.fromJson(new FileReader(file), JsonObject.class);
                    String key = file.getName().replace(".json", "").toUpperCase();
                    lootTables.put(key, key.equals("SHEEP") ? LootTableSheep.fromJson(jsonObject) : LootTable.fromJson(jsonObject));
                }catch(Exception ex){
                    ex.printStackTrace();
                }
            }
        }
    }

    private void initAllLootTables(WildStackerPlugin plugin){
        plugin.saveResource("loottables/bat.json", true);
        plugin.saveResource("loottables/blaze.json", true);
        plugin.saveResource("loottables/cave_spider.json", true);
        plugin.saveResource("loottables/chicken.json", true);
        plugin.saveResource("loottables/cod.json", true);
        plugin.saveResource("loottables/cow.json", true);
        plugin.saveResource("loottables/creeper.json", true);
        plugin.saveResource("loottables/dolphin.json", true);
        plugin.saveResource("loottables/donkey.json", true);
        plugin.saveResource("loottables/drowned.json", true);
        plugin.saveResource("loottables/elder_guardian.json", true);
        plugin.saveResource("loottables/empty.json", true);
        plugin.saveResource("loottables/ender_dragon.json", true);
        plugin.saveResource("loottables/enderman.json", true);
        plugin.saveResource("loottables/endermite.json", true);
        plugin.saveResource("loottables/evoker.json", true);
        plugin.saveResource("loottables/ghast.json", true);
        plugin.saveResource("loottables/giant.json", true);
        plugin.saveResource("loottables/guardian.json", true);
        plugin.saveResource("loottables/horse.json", true);
        plugin.saveResource("loottables/husk.json", true);
        plugin.saveResource("loottables/iron_golem.json", true);
        plugin.saveResource("loottables/llama.json", true);
        plugin.saveResource("loottables/magma_cube.json", true);
        plugin.saveResource("loottables/mule.json", true);
        plugin.saveResource("loottables/mushroom_cow.json", true);
        plugin.saveResource("loottables/ocelot.json", true);
        plugin.saveResource("loottables/parrot.json", true);
        plugin.saveResource("loottables/phantom.json", true);
        plugin.saveResource("loottables/pig.json", true);
        plugin.saveResource("loottables/polar_bear.json", true);
        plugin.saveResource("loottables/pufferfish.json", true);
        plugin.saveResource("loottables/rabbit.json", true);
        plugin.saveResource("loottables/salmon.json", true);
        plugin.saveResource("loottables/sheep.json", true);
        plugin.saveResource("loottables/shulker.json", true);
        plugin.saveResource("loottables/silverfish.json", true);
        plugin.saveResource("loottables/skeleton.json", true);
        plugin.saveResource("loottables/skeleton_horse.json", true);
        plugin.saveResource("loottables/slime.json", true);
        plugin.saveResource("loottables/snowman.json", true);
        plugin.saveResource("loottables/spider.json", true);
        plugin.saveResource("loottables/squid.json", true);
        plugin.saveResource("loottables/stray.json", true);
        plugin.saveResource("loottables/tropical_fish.json", true);
        plugin.saveResource("loottables/turtle.json", true);
        plugin.saveResource("loottables/vex.json", true);
        plugin.saveResource("loottables/villager.json", true);
        plugin.saveResource("loottables/vindicator.json", true);
        plugin.saveResource("loottables/witch.json", true);
        plugin.saveResource("loottables/wither_skeleton.json", true);
        plugin.saveResource("loottables/wolf.json", true);
        plugin.saveResource("loottables/zombie.json", true);
        plugin.saveResource("loottables/zombie_horse.json", true);
        plugin.saveResource("loottables/zombie_pigman.json", true);
        plugin.saveResource("loottables/zombie_villager.json", true);
    }

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

    public LootTable getLootTable(LivingEntity livingEntity){
        EntityTypes entityType = EntityTypes.fromEntity(livingEntity);
        return !lootTables.containsKey(entityType.name()) ? lootTables.get("EMPTY") : lootTables.get(entityType.name());
    }

    public LootTableCustom getLootTableCustom(){
        return lootTableCustom;
    }

}
