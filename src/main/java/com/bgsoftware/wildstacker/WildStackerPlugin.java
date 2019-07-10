package com.bgsoftware.wildstacker;

import com.bgsoftware.wildstacker.api.WildStacker;
import com.bgsoftware.wildstacker.api.WildStackerAPI;
import com.bgsoftware.wildstacker.api.objects.StackedBarrel;
import com.bgsoftware.wildstacker.api.objects.StackedSpawner;
import com.bgsoftware.wildstacker.command.CommandsHandler;
import com.bgsoftware.wildstacker.handlers.BreakMenuHandler;
import com.bgsoftware.wildstacker.handlers.DataHandler;
import com.bgsoftware.wildstacker.handlers.EditorHandler;
import com.bgsoftware.wildstacker.handlers.LootHandler;
import com.bgsoftware.wildstacker.handlers.ProvidersHandler;
import com.bgsoftware.wildstacker.handlers.SettingsHandler;
import com.bgsoftware.wildstacker.handlers.SystemHandler;
import com.bgsoftware.wildstacker.hooks.EconomyHook;
import com.bgsoftware.wildstacker.hooks.FastAsyncWEHook;
import com.bgsoftware.wildstacker.hooks.PluginHook_Novucs;
import com.bgsoftware.wildstacker.hooks.PluginHook_SpawnerProvider;
import com.bgsoftware.wildstacker.hooks.ProtocolLibHook;
import com.bgsoftware.wildstacker.listeners.BarrelsListener;
import com.bgsoftware.wildstacker.listeners.BucketsListener;
import com.bgsoftware.wildstacker.listeners.ChunksListener;
import com.bgsoftware.wildstacker.listeners.EditorListener;
import com.bgsoftware.wildstacker.listeners.EntitiesListener;
import com.bgsoftware.wildstacker.listeners.ItemsListener;
import com.bgsoftware.wildstacker.listeners.NoClaimConflictListener;
import com.bgsoftware.wildstacker.listeners.PlayersListener;
import com.bgsoftware.wildstacker.listeners.SpawnersListener;
import com.bgsoftware.wildstacker.listeners.events.EventsListener;
import com.bgsoftware.wildstacker.listeners.plugins.ClearLaggListener;
import com.bgsoftware.wildstacker.listeners.plugins.CustomBossesListener;
import com.bgsoftware.wildstacker.listeners.plugins.EpicSpawnersListener;
import com.bgsoftware.wildstacker.listeners.plugins.MythicMobsListener;
import com.bgsoftware.wildstacker.listeners.plugins.SilkSpawnersListener;
import com.bgsoftware.wildstacker.metrics.Metrics;
import com.bgsoftware.wildstacker.nms.NMSAdapter;
import com.bgsoftware.wildstacker.utils.Executor;
import com.bgsoftware.wildstacker.utils.ReflectionUtil;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public final class WildStackerPlugin extends JavaPlugin implements WildStacker {

    private static WildStackerPlugin plugin;

    private SettingsHandler settingsHandler;
    private SystemHandler systemManager;
    private DataHandler dataHandler;
    private ProvidersHandler providersHandler;
    private EditorHandler editorHandler;
    private BreakMenuHandler breakMenuHandler;
    private LootHandler lootHandler;

    private NMSAdapter nmsAdapter;

    @Override
    public void onEnable() {
        plugin = this;
        new Metrics(this);

        log("******** ENABLE START ********");

        loadNMSAdapter();

        breakMenuHandler = new BreakMenuHandler();
        settingsHandler = new SettingsHandler(this);
        dataHandler = new DataHandler(this);
        systemManager = new SystemHandler(this);
        editorHandler = new EditorHandler(this);
        lootHandler = new LootHandler(this);

        Locale.reload();
        loadAPI();

        getServer().getPluginManager().registerEvents(new PlayersListener(this), this);
        getServer().getPluginManager().registerEvents(new EntitiesListener(this), this);
        getServer().getPluginManager().registerEvents(new ItemsListener(this), this);
        getServer().getPluginManager().registerEvents(new SpawnersListener(this), this);
        getServer().getPluginManager().registerEvents(new BarrelsListener(this), this);
        getServer().getPluginManager().registerEvents(new EditorListener(this), this);
        getServer().getPluginManager().registerEvents(new BucketsListener(this), this);
        getServer().getPluginManager().registerEvents(new NoClaimConflictListener(this), this);
        getServer().getPluginManager().registerEvents(new ChunksListener(this), this);
        EventsListener.register(this);

        CommandsHandler commandsHandler = new CommandsHandler(this);
        getCommand("stacker").setExecutor(commandsHandler);
        getCommand("stacker").setTabCompleter(commandsHandler);

        fixConflicts();

        if(Updater.isOutdated()) {
            log("");
            log("A new version is available (v" + Updater.getLatestVersion() + ")!");
            log("Version's description: \"" + Updater.getVersionDescription() + "\"");
            log("");
        }

        log("******** ENABLE DONE ********");

        //Set WildStacker as SpawnersProvider with ShopGUIPlus
        try {
            if (ReflectionUtil.isPluginEnabled("net.brcdev.shopgui.ShopGuiPlugin"))
                PluginHook_SpawnerProvider.register();
        }catch(Throwable ignored){}
        //Enable economy hook
        if(EconomyHook.isVaultEnabled())
            EconomyHook.register();
        if(getServer().getPluginManager().isPluginEnabled("ProtocolLib"))
            ProtocolLibHook.register();

        Bukkit.getScheduler().runTask(this, () -> {
            providersHandler = new ProvidersHandler(this);

            if(getServer().getPluginManager().isPluginEnabled("ClearLag"))
                getServer().getPluginManager().registerEvents(new ClearLaggListener(this), this);
            if(getServer().getPluginManager().isPluginEnabled("SilkSpawners"))
                getServer().getPluginManager().registerEvents(new SilkSpawnersListener(this), this);
            if(getServer().getPluginManager().isPluginEnabled("EpicSpawners"))
                getServer().getPluginManager().registerEvents(new EpicSpawnersListener(), this);
            if(getServer().getPluginManager().isPluginEnabled("CustomBosses"))
                getServer().getPluginManager().registerEvents(new CustomBossesListener(), this);
            if(getServer().getPluginManager().isPluginEnabled("MythicMobs"))
                getServer().getPluginManager().registerEvents(new MythicMobsListener(), this);

            //Set WildStacker as SpawnersProvider with Novucs
            if(getServer().getPluginManager().isPluginEnabled("FactionsTop") &&
                    getServer().getPluginManager().getPlugin("FactionsTop").getDescription().getAuthors().contains("novucs"))
                PluginHook_Novucs.register(this);
        });
    }

    @Override
    public void onDisable() {
        //We need to save the entire database
        systemManager.performCacheSave();

        //We need to close the connection
        dataHandler.clearDatabase();

        for(StackedSpawner stackedSpawner : systemManager.getStackedSpawners())
            providersHandler.deleteHologram(stackedSpawner);
        for (StackedBarrel stackedBarrel : systemManager.getStackedBarrels()) {
            providersHandler.deleteHologram(stackedBarrel);
            stackedBarrel.getLocation().getChunk().load(true);
            stackedBarrel.removeDisplayBlock();
        }

        try{
            Bukkit.getScheduler().cancelAllTasks();
        }catch(Throwable ex){
            try {
                BukkitScheduler.class.getMethod("cancelTasks", Plugin.class).invoke(Bukkit.getScheduler(), this);
            } catch (Exception ignored) { }
        }

        log("Terminating all database threads...");
        Executor.stop();
    }

    private void loadAPI(){
        try{
            Field instance = WildStackerAPI.class.getDeclaredField("instance");
            instance.setAccessible(true);
            instance.set(null, this);
        }catch(Exception ex){
            log("Failed to set-up API - disabling plugin...");
            setEnabled(false);
            ex.printStackTrace();
        }
    }

    private void loadNMSAdapter(){
        String version = getServer().getClass().getPackage().getName().split("\\.")[3];
        try{
            nmsAdapter = (NMSAdapter) Class.forName("com.bgsoftware.wildstacker.nms.NMSAdapter_" + version).newInstance();
        }catch(Exception ex){
            log("WildStacker doesn't support " + version + " - shutting down...");
            Executor.sync(() -> getServer().getPluginManager().disablePlugin(this));
        }
    }

    private void fixConflicts(){
        List<String> messages = new ArrayList<>();
        if(Bukkit.getPluginManager().isPluginEnabled("EpicSpawners")){
            messages.add("Detected EpicSpawners - Disabling spawners stacking...");
        }
        if(Bukkit.getPluginManager().isPluginEnabled("MergedSpawner")){
            messages.add("Detected MergedSpawner - Disabling spawners stacking...");
        }
        if(Bukkit.getPluginManager().isPluginEnabled("ASkyBlock") && Bukkit.getPluginManager().getPlugin("ASkyBlock").getDescription().getAuthors().contains("Ome_R")){
            messages.add("Detected SuperiorSkyblock - Disabling barrels stacking...");
        }
        if(Bukkit.getPluginManager().isPluginEnabled("FastAsyncWorldEdit") && settingsHandler.itemsStackingEnabled){
            //WildStacker disabled the tick limiter for items.
            try {
                FastAsyncWEHook.disableTicksLimiter();
                messages.add("Detected FastAsyncWorldEdit - Disabling ticks limiter for items...");
            }catch(Throwable ignored){}
        }

        if(!messages.isEmpty()){
            log("");
            for(String msg : messages)
                log(msg);
        }

    }

    public NMSAdapter getNMSAdapter() {
        return nmsAdapter;
    }

    public LootHandler getLootHandler() {
        return lootHandler;
    }

    public BreakMenuHandler getBreakMenuHandler() {
        return breakMenuHandler;
    }

    public EditorHandler getEditor() {
        return editorHandler;
    }

    public ProvidersHandler getProviders(){
        return providersHandler;
    }

    public DataHandler getDataHandler(){
        return dataHandler;
    }

    public SystemHandler getSystemManager(){
        return systemManager;
    }

    public SettingsHandler getSettings(){
        return settingsHandler;
    }

    public static void log(final String message){
        plugin.getLogger().info(message);
    }

    public static WildStackerPlugin getPlugin(){
        return plugin;
    }

}
