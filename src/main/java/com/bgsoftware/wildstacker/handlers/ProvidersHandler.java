package com.bgsoftware.wildstacker.handlers;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.objects.StackedObject;
import com.bgsoftware.wildstacker.hooks.ClaimsProvider;
import com.bgsoftware.wildstacker.hooks.ClaimsProvider_FactionsUUID;
import com.bgsoftware.wildstacker.hooks.ClaimsProvider_MassiveFactions;
import com.bgsoftware.wildstacker.hooks.ClaimsProvider_PlotSquared;
import com.bgsoftware.wildstacker.hooks.ClaimsProvider_PlotSquaredLegacy;
import com.bgsoftware.wildstacker.hooks.ClaimsProvider_WorldGuard;
import com.bgsoftware.wildstacker.hooks.CoreProtectHook;
import com.bgsoftware.wildstacker.hooks.CrazyEnchantmentsHook;
import com.bgsoftware.wildstacker.hooks.EconomyHook;
import com.bgsoftware.wildstacker.hooks.FastAsyncWEHook;
import com.bgsoftware.wildstacker.hooks.HologramsProvider;
import com.bgsoftware.wildstacker.hooks.HologramsProvider_Arconix;
import com.bgsoftware.wildstacker.hooks.HologramsProvider_CMI;
import com.bgsoftware.wildstacker.hooks.HologramsProvider_Default;
import com.bgsoftware.wildstacker.hooks.HologramsProvider_Holograms;
import com.bgsoftware.wildstacker.hooks.HologramsProvider_HolographicDisplays;
import com.bgsoftware.wildstacker.hooks.McMMOHook;
import com.bgsoftware.wildstacker.hooks.PluginHook_Novucs;
import com.bgsoftware.wildstacker.hooks.PluginHooks;
import com.bgsoftware.wildstacker.hooks.ProtocolLibHook;
import com.bgsoftware.wildstacker.hooks.ShopGUIPlusHook;
import com.bgsoftware.wildstacker.hooks.SpawnersProvider;
import com.bgsoftware.wildstacker.hooks.SpawnersProvider_Default;
import com.bgsoftware.wildstacker.hooks.SpawnersProvider_MineableSpawners;
import com.bgsoftware.wildstacker.hooks.SpawnersProvider_SilkSpawners;
import com.bgsoftware.wildstacker.listeners.ProvidersListener;
import com.bgsoftware.wildstacker.listeners.plugins.BossListener;
import com.bgsoftware.wildstacker.listeners.plugins.ClearLaggListener;
import com.bgsoftware.wildstacker.listeners.plugins.CustomBossesListener;
import com.bgsoftware.wildstacker.listeners.plugins.EpicBossesListener;
import com.bgsoftware.wildstacker.listeners.plugins.EpicSpawnersListener;
import com.bgsoftware.wildstacker.listeners.plugins.MyPetListener;
import com.bgsoftware.wildstacker.listeners.plugins.MythicMobsListener;
import com.bgsoftware.wildstacker.listeners.plugins.SilkSpawnersListener;
import com.bgsoftware.wildstacker.utils.reflection.ReflectionUtils;
import com.bgsoftware.wildstacker.utils.threads.Executor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SuppressWarnings({"unused", "WeakerAccess"})
public final class ProvidersHandler {

    private SpawnersProvider spawnersProvider;
    private HologramsProvider hologramsProvider;
    private List<ClaimsProvider> claimsProviders;

    public ProvidersHandler(WildStackerPlugin plugin){
        Executor.sync(() -> {
            WildStackerPlugin.log("Loading providers started...");
            long startTime = System.currentTimeMillis();

            loadSpawnersProvider();
            loadHologramsProvider();
            loadClaimsProvider();
            loadPluginHooks(plugin, null, true);

            Bukkit.getPluginManager().registerEvents(new ProvidersListener(plugin), plugin);

            fixConflicts(plugin);

            WildStackerPlugin.log("Loading providers done (Took " + (System.currentTimeMillis() - startTime) + "ms)");
        }, 0L);

        Executor.sync(() -> {
            if(Bukkit.getPluginManager().isPluginEnabled("ASkyBlock") &&
                    !Bukkit.getPluginManager().getPlugin("ASkyBlock").getDescription().getAuthors().contains("Ome_R")){
                WildStackerPlugin.log("&c#################################################################");
                WildStackerPlugin.log("&c##                                                             ##");
                WildStackerPlugin.log("&c## Seems like you're using ASkyBlock, but not the custom fork. ##");
                WildStackerPlugin.log("&c##            <The custom fork supports WildStacker>           ##");
                WildStackerPlugin.log("&c##           https://github.com/OmerBenGera/askyblock          ##");
                WildStackerPlugin.log("&c##                                                             ##");
                WildStackerPlugin.log("&c#################################################################");
            }
        }, 10L);

    }

    private void loadSpawnersProvider(){
        if (Bukkit.getPluginManager().isPluginEnabled("SilkSpawners") &&
                Bukkit.getPluginManager().getPlugin("SilkSpawners").getDescription().getAuthors().contains("xGhOsTkiLLeRx"))
            spawnersProvider = new SpawnersProvider_SilkSpawners();
        else if(Bukkit.getPluginManager().isPluginEnabled("MineableSpawners"))
            spawnersProvider = new SpawnersProvider_MineableSpawners();
        else spawnersProvider = new SpawnersProvider_Default();
    }

    private void loadHologramsProvider(){
        if(Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays"))
            hologramsProvider = new HologramsProvider_HolographicDisplays();
        else if(Bukkit.getPluginManager().isPluginEnabled("Holograms"))
            hologramsProvider = new HologramsProvider_Holograms();
        else if(Bukkit.getPluginManager().isPluginEnabled("Arconix"))
            hologramsProvider = new HologramsProvider_Arconix();
        else if(Bukkit.getPluginManager().isPluginEnabled("CMI"))
            hologramsProvider = new HologramsProvider_CMI();
        else hologramsProvider = new HologramsProvider_Default();
    }

    private void loadClaimsProvider(){
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
    }

    public void loadPluginHooks(WildStackerPlugin plugin, Plugin toCheck, boolean enable){
        PluginManager pluginManager = plugin.getServer().getPluginManager();

        // Load listeners
        if(enable && isPlugin(toCheck, "ClearLag") && pluginManager.isPluginEnabled("ClearLag"))
            pluginManager.registerEvents(new ClearLaggListener(plugin), plugin);
        if(enable && isPlugin(toCheck, "SilkSpawners") && pluginManager.isPluginEnabled("SilkSpawners"))
            pluginManager.registerEvents(new SilkSpawnersListener(plugin), plugin);
        if(enable && isPlugin(toCheck, "CustomBosses") && pluginManager.isPluginEnabled("CustomBosses"))
            pluginManager.registerEvents(new CustomBossesListener(), plugin);
        if(enable && isPlugin(toCheck, "EpicBosses") && pluginManager.isPluginEnabled("EpicBosses"))
            pluginManager.registerEvents(new EpicBossesListener(), plugin);
        if(enable && isPlugin(toCheck, "MythicMobs") && pluginManager.isPluginEnabled("MythicMobs"))
            pluginManager.registerEvents(new MythicMobsListener(), plugin);
        if(enable && isPlugin(toCheck, "MyPet") && pluginManager.isPluginEnabled("MyPet"))
            pluginManager.registerEvents(new MyPetListener(), plugin);
        if(enable && isPlugin(toCheck, "EpicSpawners") && pluginManager.isPluginEnabled("EpicSpawners"))
            EpicSpawnersListener.register(plugin);
        if(enable && isPlugin(toCheck, "CrazyEnchantments") && pluginManager.isPluginEnabled("CrazyEnchantments"))
            CrazyEnchantmentsHook.register();
        if(enable && isPlugin(toCheck, "Boss") && pluginManager.isPluginEnabled("Boss"))
            pluginManager.registerEvents(new BossListener(), plugin);

        //Load plugin hooks
        if(isPlugin(toCheck, "mcMMO") && pluginManager.isPluginEnabled("mcMMO"))
            McMMOHook.setEnabled(enable);
        if(isPlugin(toCheck, "CoreProtect") && pluginManager.isPluginEnabled("CoreProtect"))
            CoreProtectHook.setEnabled(enable);
        if(isPlugin(toCheck, "WorldGuard") && pluginManager.isPluginEnabled("WorldGuard"))
            PluginHooks.isWorldGuardEnabled = enable;
        if(isPlugin(toCheck, "WildTools") && pluginManager.isPluginEnabled("WildTools"))
            PluginHooks.isWildToolsEnabled = enable;
        if(isPlugin(toCheck, "ProtocolLib") && pluginManager.isPluginEnabled("ProtocolLib"))
            ProtocolLibHook.setEnabled(enable);
        if(isPlugin(toCheck, "Vault") && pluginManager.isPluginEnabled("Vault"))
            EconomyHook.setEnabled(enable);
        if(isPlugin(toCheck, "MergedSpawner") && pluginManager.isPluginEnabled("MergedSpawner"))
            PluginHooks.isMergedSpawnersEnabled = enable;
        if(isPlugin(toCheck, "ASkyBlock") && pluginManager.isPluginEnabled("ASkyBlock") && pluginManager.getPlugin("ASkyBlock").getDescription().getAuthors().contains("Ome_R"))
            PluginHooks.isASkyBlockEnabled = enable;
        if(isPlugin(toCheck, "FastAsyncWorldEdit") && pluginManager.isPluginEnabled("FastAsyncWorldEdit"))
            PluginHooks.isFastAsyncWorldEditEnabled = enable;
        if(isPlugin(toCheck, "PickupSpawners") && pluginManager.isPluginEnabled("PickupSpawners"))
            PluginHooks.isPickupSpawnersEnabled = enable;
        if(enable && isPlugin(toCheck, "FactionsTop") && ReflectionUtils.isPluginEnabled("net.novucs.ftop.FactionsTopPlugin"))
            PluginHook_Novucs.setEnabled(plugin);
        if (enable && isPlugin(toCheck, "ShopGUIPlus") && ReflectionUtils.isPluginEnabled("net.brcdev.shopgui.ShopGuiPlugin"))
            ShopGUIPlusHook.setEnabled();
    }

    private boolean isPlugin(Plugin plugin, String pluginName){
        return plugin == null || plugin.getName().equals(pluginName);
    }

    private void fixConflicts(WildStackerPlugin plugin){
        List<String> messages = new ArrayList<>();
        if(PluginHooks.isEpicSpawnersEnabled){
            messages.add("Detected EpicSpawners - Disabling spawners stacking...");
        }
        if(PluginHooks.isMergedSpawnersEnabled){
            messages.add("Detected MergedSpawner - Disabling spawners stacking...");
        }
        if(Bukkit.getPluginManager().isPluginEnabled("ASkyBlock") && Bukkit.getPluginManager().getPlugin("ASkyBlock").getDescription().getAuthors().contains("Ome_R")){
            messages.add("Detected SuperiorSkyblock - Disabling barrels stacking...");
        }
        if(PluginHooks.isFastAsyncWorldEditEnabled && plugin.getSettings().itemsStackingEnabled){
            //WildStacker disabled the tick limiter for items.
            try {
                FastAsyncWEHook.disableTicksLimiter();
                messages.add("Detected FastAsyncWorldEdit - Disabling ticks limiter for items...");
            }catch(Throwable ignored){}
        }

        if(!messages.isEmpty()){
            WildStackerPlugin.log("");
            for(String msg : messages)
                WildStackerPlugin.log(msg);
            WildStackerPlugin.log("");
        }

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
