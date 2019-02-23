package com.bgsoftware.wildstacker;

import com.bgsoftware.wildstacker.api.WildStacker;
import com.bgsoftware.wildstacker.api.WildStackerAPI;
import com.bgsoftware.wildstacker.api.handlers.SystemManager;
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
import com.bgsoftware.wildstacker.hooks.CrazyEnchantmentsHook;
import com.bgsoftware.wildstacker.hooks.EconomyHook;
import com.bgsoftware.wildstacker.hooks.PluginHook_Novucs;
import com.bgsoftware.wildstacker.hooks.PluginHook_SpawnerProvider;
import com.bgsoftware.wildstacker.listeners.BarrelsListener;
import com.bgsoftware.wildstacker.listeners.BucketsListener;
import com.bgsoftware.wildstacker.listeners.EditorListener;
import com.bgsoftware.wildstacker.listeners.EntitiesListener;
import com.bgsoftware.wildstacker.listeners.ItemsListener;
import com.bgsoftware.wildstacker.listeners.PlayersListener;
import com.bgsoftware.wildstacker.listeners.SpawnersListener;
import com.bgsoftware.wildstacker.listeners.plugins.ClearLaggListener;
import com.bgsoftware.wildstacker.listeners.plugins.EpicSpawnersListener;
import com.bgsoftware.wildstacker.listeners.plugins.SilkSpawnersListener;
import com.bgsoftware.wildstacker.metrics.Metrics;
import com.bgsoftware.wildstacker.nms.NMSAdapter;
import com.bgsoftware.wildstacker.utils.ReflectionUtil;
import com.boydti.fawe.config.Settings;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

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

        breakMenuHandler = new BreakMenuHandler();
        settingsHandler = new SettingsHandler(this);
        dataHandler = new DataHandler(this);
        systemManager = new SystemHandler(this);
        editorHandler = new EditorHandler(this);
        lootHandler = new LootHandler(this);

        Locale.reload();
        loadAPI();
        loadNMSAdapter();

        getServer().getPluginManager().registerEvents(new PlayersListener(this), this);
        getServer().getPluginManager().registerEvents(new EntitiesListener(this), this);
        getServer().getPluginManager().registerEvents(new ItemsListener(this), this);
        getServer().getPluginManager().registerEvents(new SpawnersListener(this), this);
        getServer().getPluginManager().registerEvents(new BarrelsListener(this), this);
        getServer().getPluginManager().registerEvents(new EditorListener(this), this);
        getServer().getPluginManager().registerEvents(new BucketsListener(this), this);

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
        //Enable CrazyEnchantments hook
        CrazyEnchantmentsHook.register();

        runOnFirstTick(() -> {
            providersHandler = new ProvidersHandler(this);

            if(getServer().getPluginManager().isPluginEnabled("ClearLag"))
                getServer().getPluginManager().registerEvents(new ClearLaggListener(this), this);
            if(getServer().getPluginManager().isPluginEnabled("SilkSpawners"))
                getServer().getPluginManager().registerEvents(new SilkSpawnersListener(this), this);
            if(getServer().getPluginManager().isPluginEnabled("EpicSpawners"))
                getServer().getPluginManager().registerEvents(new EpicSpawnersListener(this), this);
//            if(getServer().getPluginManager().isPluginEnabled("mcMMO"))
//                getServer().getPluginManager().registerEvents(new McMMOListener(), this);

            //Set all holograms of spawners
            for (StackedSpawner stackedSpawner : systemManager.getStackedSpawners())
                stackedSpawner.updateName();

            //Set all holograms and block displays of barrlels
            for (StackedBarrel stackedBarrel : systemManager.getStackedBarrels()) {
                stackedBarrel.updateName();
                stackedBarrel.getLocation().getChunk().load(true);
                stackedBarrel.createDisplayBlock();
            }

            //Set WildStacker as SpawnersProvider with Novucs
            if(getServer().getPluginManager().isPluginEnabled("FactionsTop") &&
                    getServer().getPluginManager().getPlugin("FactionsTop").getDescription().getAuthors().contains("novucs"))
                PluginHook_Novucs.register(this);
        });
    }

    @Override
    public void onDisable() {
        //We need to remove all holograms of spawners
        for(StackedSpawner stackedSpawner : systemManager.getStackedSpawners())
            providersHandler.deleteHologram(stackedSpawner);
        for (StackedBarrel stackedBarrel : systemManager.getStackedBarrels()) {
            providersHandler.deleteHologram(stackedBarrel);
            stackedBarrel.getLocation().getChunk().load(true);
            stackedBarrel.removeDisplayBlock();
        }
        dataHandler.saveDatabase();
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
            runOnFirstTick(() -> getServer().getPluginManager().disablePlugin(this));
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
            messages.add("Detected FastAsyncWorldEdit - Disabling ticks limiter for items...");
            //WildStacker disabled the tick limiter for items.
            Settings.IMP.TICK_LIMITER.ITEMS = Integer.MAX_VALUE;
        }

        if(!messages.isEmpty()){
            log("");
            for(String msg : messages)
                log(msg);
        }

    }

    private void runOnFirstTick(final Runnable runnable){
        Bukkit.getScheduler().runTaskLater(this, runnable, 1L);
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

    public SystemManager getSystemManager(){
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
