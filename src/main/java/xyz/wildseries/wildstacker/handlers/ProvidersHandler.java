package xyz.wildseries.wildstacker.handlers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import xyz.wildseries.wildstacker.WildStackerPlugin;
import xyz.wildseries.wildstacker.api.objects.StackedObject;
import xyz.wildseries.wildstacker.hooks.HologramsProvider;
import xyz.wildseries.wildstacker.hooks.HologramsProvider_Arconix;
import xyz.wildseries.wildstacker.hooks.HologramsProvider_Default;
import xyz.wildseries.wildstacker.hooks.HologramsProvider_Holograms;
import xyz.wildseries.wildstacker.hooks.HologramsProvider_HolographicDisplays;
import xyz.wildseries.wildstacker.hooks.SpawnersProvider;
import xyz.wildseries.wildstacker.hooks.SpawnersProvider_Default;
import xyz.wildseries.wildstacker.hooks.SpawnersProvider_SilkSpawners;
import xyz.wildseries.wildstacker.loot.LootTableCustomDrops;
import xyz.wildseries.wildstacker.loot.LootTableDropEdit;
import xyz.wildseries.wildstacker.loot.LootTableEditDrops;
import xyz.wildseries.wildstacker.loot.LootTableEpicSpawners;
import xyz.wildseries.wildstacker.loot.LootTableStackSpawners;

@SuppressWarnings("unused")
public final class ProvidersHandler {

    private SpawnersProvider spawnersProvider;
    private HologramsProvider hologramsProvider;

    public ProvidersHandler(WildStackerPlugin plugin){
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            WildStackerPlugin.log("Loading providers started...");
            long startTime = System.currentTimeMillis();

            if(Bukkit.getPluginManager().isPluginEnabled("CustomDrops"))
                LootTableCustomDrops.register();
            else if(Bukkit.getPluginManager().isPluginEnabled("DropEdit"))
                LootTableDropEdit.register();
            else if(Bukkit.getPluginManager().isPluginEnabled("EditDrops"))
                LootTableEditDrops.register();
            else if(Bukkit.getPluginManager().isPluginEnabled("EpicSpawners"))
                LootTableEpicSpawners.register();
            else if(Bukkit.getPluginManager().isPluginEnabled("StackSpawners"))
                LootTableStackSpawners.register();
            else
                WildStackerPlugin.log(" - Couldn't find any custom loot tables.");

            if (Bukkit.getPluginManager().isPluginEnabled("SilkSpawners"))
                spawnersProvider = new SpawnersProvider_SilkSpawners();
            else spawnersProvider = new SpawnersProvider_Default();

            if(Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays"))
                hologramsProvider = new HologramsProvider_HolographicDisplays();
            else if(Bukkit.getPluginManager().isPluginEnabled("Holograms"))
                hologramsProvider = new HologramsProvider_Holograms();
            else if(Bukkit.getPluginManager().isPluginEnabled("Arconix"))
                hologramsProvider = new HologramsProvider_Arconix();
            else hologramsProvider = new HologramsProvider_Default();

            WildStackerPlugin.log("Loading providers done (Took " + (System.currentTimeMillis() - startTime) + "ms)");
        }, 1L);
    }

    /*
     * Spawners Provider
     */

    public ItemStack getSpawnerItem(CreatureSpawner spawner, int amount){
        return spawnersProvider.getSpawnerItem(spawner, amount);
    }

    public void dropOrGiveItem(Entity entity, CreatureSpawner spawner, int amount){
        spawnersProvider.dropOrGiveItem(entity, spawner, amount);
    }

    public void dropOrGiveItem(Player player, CreatureSpawner spawner, int amount){
        spawnersProvider.dropOrGiveItem(player, spawner, amount);
    }

    public void setSpawnerType(CreatureSpawner spawner, ItemStack itemStack, boolean updateName){
        spawnersProvider.setSpawnerType(spawner, itemStack, updateName);
    }

    /*
     * Holograms Provider
     */

    public void createHologram(StackedObject stackedObject, String line){
        hologramsProvider.createHologram(stackedObject, line);
    }

    public void createHologram(Location location, String line){
        hologramsProvider.createHologram(location, line);
    }

    public void deleteHologram(StackedObject stackedObject){
        hologramsProvider.deleteHologram(stackedObject);
    }

    public void deleteHologram(Location location){
        hologramsProvider.deleteHologram(location);
    }

    public void changeLine(StackedObject stackedObject, String newLine, boolean createIfNull){
        hologramsProvider.changeLine(stackedObject, newLine, createIfNull);
    }

    public void changeLine(Location location, String newLine, boolean createIfNull){
        hologramsProvider.changeLine(location, newLine, createIfNull);
    }

    public boolean isHologram(Location location){
        return hologramsProvider.isHologram(location);
    }

    public void clearHolograms(){
        hologramsProvider.clearHolograms();
    }

}
