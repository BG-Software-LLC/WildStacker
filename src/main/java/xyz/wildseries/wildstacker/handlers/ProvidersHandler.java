package xyz.wildseries.wildstacker.handlers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import xyz.wildseries.wildstacker.WildStackerPlugin;
import xyz.wildseries.wildstacker.api.objects.StackedObject;
import xyz.wildseries.wildstacker.hooks.AntiCheatProvider;
import xyz.wildseries.wildstacker.hooks.AntiCheatProvider_AAC;
import xyz.wildseries.wildstacker.hooks.AntiCheatProvider_Default;
import xyz.wildseries.wildstacker.hooks.AntiCheatProvider_NoCheatPlus;
import xyz.wildseries.wildstacker.hooks.AntiCheatProvider_Spartan;
import xyz.wildseries.wildstacker.hooks.HologramsProvider;
import xyz.wildseries.wildstacker.hooks.HologramsProvider_Arconix;
import xyz.wildseries.wildstacker.hooks.HologramsProvider_Default;
import xyz.wildseries.wildstacker.hooks.HologramsProvider_Holograms;
import xyz.wildseries.wildstacker.hooks.HologramsProvider_HolographicDisplays;
import xyz.wildseries.wildstacker.hooks.SpawnersProvider;
import xyz.wildseries.wildstacker.hooks.SpawnersProvider_Default;
import xyz.wildseries.wildstacker.hooks.SpawnersProvider_SilkSpawners;

@SuppressWarnings("unused")
public final class ProvidersHandler {

    private AntiCheatProvider antiCheatProvider;
    private SpawnersProvider spawnersProvider;
    private HologramsProvider hologramsProvider;
    private DropsProvider dropsProvider;

    public ProvidersHandler(WildStackerPlugin plugin){
        WildStackerPlugin.log("Loading providers started...");
        long startTime = System.currentTimeMillis();

        if(Bukkit.getPluginManager().isPluginEnabled("CustomDrops")) {
            WildStackerPlugin.log(" - Using CustomDrops as Custom LootTable.");
            dropsProvider = DropsProvider.CUSTOM_DROPS;
        }else if(Bukkit.getPluginManager().isPluginEnabled("DropEdit2")) {
            WildStackerPlugin.log(" - Using DropEdit2 as Custom LootTable.");
            dropsProvider = DropsProvider.DROP_EDIT;
        }else if(Bukkit.getPluginManager().isPluginEnabled("EditDrops")) {
            WildStackerPlugin.log(" - Using EditDrops as Custom LootTable.");
            dropsProvider = DropsProvider.EDIT_DROPS;
        }else if(Bukkit.getPluginManager().isPluginEnabled("EpicSpawners")) {
            WildStackerPlugin.log(" - Using EpicSpawners as Custom LootTable.");
            dropsProvider = DropsProvider.EPIC_SPAWNERS;
        }else if(Bukkit.getPluginManager().isPluginEnabled("StackSpawners")) {
            WildStackerPlugin.log(" - Using StackSpawners as Custom LootTable.");
            dropsProvider = DropsProvider.STACK_SPAWNERS;
        }else{
            WildStackerPlugin.log(" - Couldn't find any custom loot tables.");
            dropsProvider = DropsProvider.DEFAULT;
        }

        plugin.getLootHandler().initLootTableCustom(dropsProvider);

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

        if(Bukkit.getPluginManager().isPluginEnabled("AAC"))
            antiCheatProvider = new AntiCheatProvider_AAC();
        else if(Bukkit.getPluginManager().isPluginEnabled("NoCheatPlus"))
            antiCheatProvider = new AntiCheatProvider_NoCheatPlus();
        else if(Bukkit.getPluginManager().isPluginEnabled("Spartan"))
            antiCheatProvider = new AntiCheatProvider_Spartan();
        else
            antiCheatProvider = new AntiCheatProvider_Default();

        WildStackerPlugin.log("Loading providers done (Took " + (System.currentTimeMillis() - startTime) + "ms)");
    }

    /*
     * Drops Provider
     */

    public DropsProvider getDropsProvider(){
        return dropsProvider;
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

    /*
     *  AntiCheat Provider
     */

    public void enableBypass(Player player){
        antiCheatProvider.enableBypass(player);
    }

    public void disableBypass(Player player){
        antiCheatProvider.disableBypass(player);
    }

    public enum DropsProvider{
        CUSTOM_DROPS, DROP_EDIT, EDIT_DROPS, EPIC_SPAWNERS, STACK_SPAWNERS, DEFAULT
    }

}
