package com.bgsoftware.wildstacker.handlers;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.objects.StackedObject;
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
import com.bgsoftware.wildstacker.hooks.SpawnersProvider_MineableSpawners;
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
import java.util.UUID;

@SuppressWarnings({"unused", "WeakerAccess"})
public final class ProvidersHandler {

    private SpawnersProvider spawnersProvider;
    private HologramsProvider hologramsProvider;
    private List<ClaimsProvider> claimsProviders;

    public ProvidersHandler(WildStackerPlugin plugin){
        WildStackerPlugin.log("Loading providers started...");
        long startTime = System.currentTimeMillis();

        if (Bukkit.getPluginManager().isPluginEnabled("SilkSpawners") &&
                Bukkit.getPluginManager().getPlugin("SilkSpawners").getDescription().getAuthors().contains("xGhOsTkiLLeRx"))
            spawnersProvider = new SpawnersProvider_SilkSpawners();
        else if(Bukkit.getPluginManager().isPluginEnabled("MineableSpawners"))
            spawnersProvider = new SpawnersProvider_MineableSpawners();
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

        claimsProviders = new ArrayList<>();
        if(Bukkit.getPluginManager().isPluginEnabled("Factions")){
            if(Bukkit.getPluginManager().getPlugin("Factions").getDescription().getAuthors().contains("drtshock"))
                claimsProviders.add(new ClaimsProvider_FactionsUUID());
            else
                claimsProviders.add(new ClaimsProvider_MassiveFactions());
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
     * Spawners Provider
     */

    public ItemStack getSpawnerItem(CreatureSpawner spawner, int amount){
        return spawnersProvider.getSpawnerItem(spawner, amount);
    }

    public void dropOrGiveItem(Entity entity, CreatureSpawner spawner, int amount, UUID explodeSource){
        spawnersProvider.dropOrGiveItem(entity, spawner, amount, explodeSource);
    }

    public void dropOrGiveItem(Player player, CreatureSpawner spawner, int amount, boolean isExplodeSource){
        spawnersProvider.dropOrGiveItem(player, spawner, amount, isExplodeSource);
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

}
