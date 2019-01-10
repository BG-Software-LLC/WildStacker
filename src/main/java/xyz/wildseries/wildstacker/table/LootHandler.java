package xyz.wildseries.wildstacker.table;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.bukkit.entity.LivingEntity;
import xyz.wildseries.wildstacker.WildStackerPlugin;
import xyz.wildseries.wildstacker.utils.legacy.EntityTypes;

import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings({"ResultOfMethodCallIgnored", "ConstantConditions"})
public class LootHandler {

    private final Map<String, LootTable> lootTables = new HashMap<>();
    static final Gson gson = new Gson();

    public LootHandler(WildStackerPlugin plugin){
        File folderFile = new File(plugin.getDataFolder(), "loottables");

        if(!folderFile.exists()){
            folderFile.mkdirs();
            initAllLootTables();
        }
        else{
            for(File file : folderFile.listFiles()){
                try {
                    JsonObject jsonObject = gson.fromJson(new FileReader(file), JsonObject.class);
                    String key = file.getName().replace(".json", "").toUpperCase();
                    lootTables.put(key, LootTable.fromJson(jsonObject));
                }catch(Exception ex){
                    ex.printStackTrace();
                }
            }
        }
    }

    private void initAllLootTables(){

    }

    public LootTable getLootTable(LivingEntity livingEntity){
        if(lootTables.containsKey("CUSTOM")){
            return lootTables.get("CUSTOM");
        }
        return getNaturalLootTable(livingEntity);
    }

    public LootTable getNaturalLootTable(LivingEntity livingEntity){
        EntityTypes entityType = EntityTypes.fromEntity(livingEntity);
        return !lootTables.containsKey(entityType.name()) ? lootTables.get("EMPTY") : lootTables.get(entityType.name());
    }

}
