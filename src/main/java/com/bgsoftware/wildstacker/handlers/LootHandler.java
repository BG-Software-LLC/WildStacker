package com.bgsoftware.wildstacker.handlers;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.loot.LootTable;
import com.bgsoftware.wildstacker.loot.LootTableSheep;
import com.bgsoftware.wildstacker.utils.FileUtil;
import com.bgsoftware.wildstacker.utils.legacy.EntityTypes;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
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
    private final Gson gson = new Gson();

    public LootHandler(WildStackerPlugin plugin){
        File folderFile = new File(plugin.getDataFolder(), "loottables");

        if(!folderFile.exists()){
            folderFile.mkdirs();
            initAllLootTables();
        }

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
        boolean v1_8 = Bukkit.getBukkitVersion().contains("1.8");
        boolean v1_9 = Bukkit.getBukkitVersion().contains("1.9");
        boolean v1_10 = Bukkit.getBukkitVersion().contains("1.10");
        boolean v1_11 = Bukkit.getBukkitVersion().contains("1.11");
        boolean v1_12 = Bukkit.getBukkitVersion().contains("1.12");
        boolean v1_13 = Bukkit.getBukkitVersion().contains("1.13");
        FileUtil.saveResource("loottables/bat.json");
        FileUtil.saveResource("loottables/blaze.json");
        FileUtil.saveResource("loottables/cave_spider.json");
        FileUtil.saveResource(!v1_13 ? "loottables/chicken.json" : "loottables/chicken1_13.json", "loottables/chicken.json");
        if(containsEntity("COD"))
            FileUtil.saveResource("loottables/cod.json");
        FileUtil.saveResource(!v1_13 ? "loottables/cow.json" : "loottables/cow1_13.json", "loottables/cow.json");
        FileUtil.saveResource(v1_10 || v1_11 || v1_12 ? "loottables/creeper1_10.json" :
                v1_13 ? "loottables/creeper1_13.json" : "loottables/creeper.json", "loottables/creeper.json");
        if(containsEntity("DOLPHIN"))
            FileUtil.saveResource("loottables/dolphin.json");
        FileUtil.saveResource("loottables/donkey.json");
        if(containsEntity("DROWNED"))
            FileUtil.saveResource("loottables/drowned.json");
        FileUtil.saveResource(v1_8 ? "loottables/elder_guardian1_8.json" : v1_9 || v1_10 ? "loottables/elder_guardian1_9.json" :
                v1_11 || v1_12 ? "loottables/elder_guardian1_11.json" : "loottables/elder_guardian1_13.json", "loottables/elder_guardian.json");
        FileUtil.saveResource("loottables/ender_dragon.json");
        FileUtil.saveResource("loottables/enderman.json");
        FileUtil.saveResource("loottables/endermite.json");
        if(containsEntity("EVOKER"))
            FileUtil.saveResource(v1_13 ? "loottables/evoker1_13.json" : "loottables/evoker.json", "loottables/evoker.json");
        FileUtil.saveResource(!v1_13 ? "loottables/ghast.json" : "loottables/ghast1_13.json", "loottables/ghast.json");
        FileUtil.saveResource("loottables/giant.json");
        FileUtil.saveResource(v1_8 ? "loottables/guardian1_8.json" : v1_9 || v1_10 ? "loottables/guardian1_9.json" :
                v1_11 || v1_12 ? "loottables/guardian1_11.json" : "loottables/guardian1_13.json", "loottables/guardian.json");
        FileUtil.saveResource("loottables/horse.json");
        if(containsEntity("HUSK"))
            FileUtil.saveResource("loottables/husk.json");
        FileUtil.saveResource(!v1_13 ? "loottables/iron_golem.json" : "loottables/iron_golem1_13.json", "loottables/iron_golem.json");
        FileUtil.saveResource("loottables/magma_cube.json");
        FileUtil.saveResource("loottables/mule.json");
        FileUtil.saveResource(!v1_13 ? "loottables/mushroom_cow.json" : "loottables/mushroom_cow1_13.json", "loottables/mushroom_cow.json");
        FileUtil.saveResource("loottables/ocelot.json");
        if(containsEntity("PARROT"))
            FileUtil.saveResource("loottables/parrot.json");
        if(containsEntity("PHANTOM"))
            FileUtil.saveResource("loottables/phantom.json");
        FileUtil.saveResource(!v1_13 ? "loottables/pig.json" : "loottables/pig1_13.json", "loottables/pig.json");
        if(containsEntity("POLAR_BEAR"))
            FileUtil.saveResource(v1_13 ? "loottables/polar_bear1_13.json" : "loottables/polar_bear.json", "loottables/polar_bear.json");
        if(containsEntity("PUFFERFISH"))
            FileUtil.saveResource("loottables/pufferfish.json");
        FileUtil.saveResource("loottables/rabbit.json");
        if(containsEntity("SALMON"))
            FileUtil.saveResource("loottables/salmon.json");
        FileUtil.saveResource(!v1_13 ? "loottables/sheep.json" : "loottables/sheep1_13.json", "loottables/sheep.json");
        if(containsEntity("SHULKER"))
            FileUtil.saveResource("loottables/shulker.json");
        FileUtil.saveResource("loottables/silverfish.json");
        FileUtil.saveResource("loottables/skeleton.json");
        FileUtil.saveResource("loottables/skeleton_horse.json");
        FileUtil.saveResource("loottables/slime.json");
        FileUtil.saveResource(!v1_13 ? "loottables/snowman.json" : "loottables/snowman1_13.json", "loottables/snowman.json");
        FileUtil.saveResource("loottables/spider.json");
        FileUtil.saveResource(!v1_13 ? "loottables/squid.json" : "loottables/squid1_13.json", "loottables/squid.json");
        if(containsEntity("STRAY"))
            FileUtil.saveResource("loottables/stray.json");
        if(containsEntity("TROPICAL_FISH"))
            FileUtil.saveResource("loottables/tropical_fish.json");
        if(containsEntity("TURTLE"))
            FileUtil.saveResource("loottables/turtle.json");
        if(containsEntity("VEX"))
            FileUtil.saveResource("loottables/vex.json");
        FileUtil.saveResource("loottables/villager.json");
        if(containsEntity("VINDICATOR"))
            FileUtil.saveResource("loottables/vindicator.json");
        FileUtil.saveResource(!v1_13 ? "loottables/witch.json" : "loottables/witch1_13.json", "loottables/witch.json");
        FileUtil.saveResource(!v1_13 ? "loottables/wither_skeleton.json" : "loottables/wither_skeleton1_13.json", "loottables/wither_skeleton.json");
        FileUtil.saveResource("loottables/wolf.json");
        FileUtil.saveResource("loottables/zombie.json");
        FileUtil.saveResource("loottables/zombie_horse.json");
        FileUtil.saveResource("loottables/zombie_pigman.json");
        FileUtil.saveResource("loottables/zombie_villager.json");
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
