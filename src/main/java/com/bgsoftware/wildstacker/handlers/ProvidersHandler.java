package com.bgsoftware.wildstacker.handlers;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.objects.StackedObject;
import com.bgsoftware.wildstacker.hooks.AntiCheatProvider;
import com.bgsoftware.wildstacker.hooks.AntiCheatProvider_AAC;
import com.bgsoftware.wildstacker.hooks.AntiCheatProvider_Default;
import com.bgsoftware.wildstacker.hooks.AntiCheatProvider_NoCheatPlus;
import com.bgsoftware.wildstacker.hooks.AntiCheatProvider_Spartan;
import com.bgsoftware.wildstacker.hooks.ClaimsProvider;
import com.bgsoftware.wildstacker.hooks.ClaimsProvider_FactionsUUID;
import com.bgsoftware.wildstacker.hooks.ClaimsProvider_MassiveFactions;
import com.bgsoftware.wildstacker.hooks.ClaimsProvider_PlotSquared;
import com.bgsoftware.wildstacker.hooks.ClaimsProvider_PlotSquaredLegacy;
import com.bgsoftware.wildstacker.hooks.ClaimsProvider_WorldGuard;
import com.bgsoftware.wildstacker.hooks.HologramsProvider;
import com.bgsoftware.wildstacker.hooks.HologramsProvider_Arconix;
import com.bgsoftware.wildstacker.hooks.HologramsProvider_CMI;
import com.bgsoftware.wildstacker.hooks.HologramsProvider_Default;
import com.bgsoftware.wildstacker.hooks.HologramsProvider_Holograms;
import com.bgsoftware.wildstacker.hooks.HologramsProvider_HolographicDisplays;
import com.bgsoftware.wildstacker.hooks.SpawnersProvider;
import com.bgsoftware.wildstacker.hooks.SpawnersProvider_Default;
import com.bgsoftware.wildstacker.hooks.SpawnersProvider_SilkSpawners;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public final class ProvidersHandler {

    private AntiCheatProvider antiCheatProvider;
    private SpawnersProvider spawnersProvider;
    private HologramsProvider hologramsProvider;
    private DropsProvider dropsProvider;
    private List<ClaimsProvider> claimsProviders;

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

        //plugin.getLootHandler().initLootTableCustom(dropsProvider);

        if (Bukkit.getPluginManager().isPluginEnabled("SilkSpawners") &&
                Bukkit.getPluginManager().getPlugin("SilkSpawners").getDescription().getAuthors().contains("xGhOsTkiLLeRx"))
            spawnersProvider = new SpawnersProvider_SilkSpawners();
        else spawnersProvider = new SpawnersProvider_Default();

        if(Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays"))
            hologramsProvider = new HologramsProvider_HolographicDisplays();
        else if(Bukkit.getPluginManager().isPluginEnabled("Holograms"))
            hologramsProvider = new HologramsProvider_Holograms();
        else if(Bukkit.getPluginManager().isPluginEnabled("Arconix"))
            hologramsProvider = new HologramsProvider_Arconix();
        else if(Bukkit.getPluginManager().isPluginEnabled("CMI"))
            hologramsProvider = new HologramsProvider_CMI();
        else hologramsProvider = new HologramsProvider_Default();

        if(Bukkit.getPluginManager().isPluginEnabled("AAC"))
            antiCheatProvider = new AntiCheatProvider_AAC();
        else if(Bukkit.getPluginManager().isPluginEnabled("NoCheatPlus"))
            antiCheatProvider = new AntiCheatProvider_NoCheatPlus();
        else if(Bukkit.getPluginManager().isPluginEnabled("Spartan"))
            antiCheatProvider = new AntiCheatProvider_Spartan();
        else
            antiCheatProvider = new AntiCheatProvider_Default();

        claimsProviders = new ArrayList<>();
        if(Bukkit.getPluginManager().isPluginEnabled("Factions")){
            if(Bukkit.getPluginManager().isPluginEnabled("MassiveCore"))
                claimsProviders.add(new ClaimsProvider_MassiveFactions());
            else
                claimsProviders.add(new ClaimsProvider_FactionsUUID());
        }
        if(Bukkit.getPluginManager().isPluginEnabled("PlotSquared")) {
            try {
                Class.forName("com.intellectualcrafters.plot.api.PlotAPI");
                claimsProviders.add(new ClaimsProvider_PlotSquaredLegacy());
            }catch(Throwable ex){
                claimsProviders.add(new ClaimsProvider_PlotSquared());
            }
        }
        if(Bukkit.getPluginManager().isPluginEnabled("WorldGuard"))
            claimsProviders.add(new ClaimsProvider_WorldGuard());

        WildStackerPlugin.log("Loading providers done (Took " + (System.currentTimeMillis() - startTime) + "ms)");
    }

    /*
     * Drops Provider
     */

    public DropsProvider getDropsProvider() {
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

    public EntityType getSpawnerType(ItemStack itemStack){
        return spawnersProvider.getSpawnerType(itemStack);
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

    /*
     * Claims Provider
     */

    @SuppressWarnings("all")
    public boolean hasClaimAccess(Player player, Location location){
        for(ClaimsProvider claimsProvider : claimsProviders) {
            if (!claimsProvider.hasClaimAccess(player, location))
                return false;
        }

        return true;
    }

    public enum DropsProvider{
        CUSTOM_DROPS, DROP_EDIT, EDIT_DROPS, EPIC_SPAWNERS, STACK_SPAWNERS, DEFAULT
    }

}
