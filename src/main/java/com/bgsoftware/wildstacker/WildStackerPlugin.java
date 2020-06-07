package com.bgsoftware.wildstacker;

import com.bgsoftware.wildstacker.api.WildStacker;
import com.bgsoftware.wildstacker.api.WildStackerAPI;
import com.bgsoftware.wildstacker.api.objects.StackedBarrel;
import com.bgsoftware.wildstacker.api.objects.StackedSpawner;
import com.bgsoftware.wildstacker.command.CommandsHandler;
import com.bgsoftware.wildstacker.handlers.DataHandler;
import com.bgsoftware.wildstacker.handlers.LootHandler;
import com.bgsoftware.wildstacker.handlers.ProvidersHandler;
import com.bgsoftware.wildstacker.handlers.SettingsHandler;
import com.bgsoftware.wildstacker.handlers.SystemHandler;
import com.bgsoftware.wildstacker.listeners.BarrelsListener;
import com.bgsoftware.wildstacker.listeners.BucketsListener;
import com.bgsoftware.wildstacker.listeners.ChunksListener;
import com.bgsoftware.wildstacker.listeners.EntitiesListener;
import com.bgsoftware.wildstacker.listeners.ItemsListener;
import com.bgsoftware.wildstacker.listeners.MenusListener;
import com.bgsoftware.wildstacker.listeners.NoClaimConflictListener;
import com.bgsoftware.wildstacker.listeners.PlayersListener;
import com.bgsoftware.wildstacker.listeners.ShulkerOversizedPatch;
import com.bgsoftware.wildstacker.listeners.SpawnersListener;
import com.bgsoftware.wildstacker.listeners.StewListener;
import com.bgsoftware.wildstacker.listeners.ToolsListener;
import com.bgsoftware.wildstacker.listeners.events.EventsListener;
import com.bgsoftware.wildstacker.menu.EditorMenu;
import com.bgsoftware.wildstacker.metrics.Metrics;
import com.bgsoftware.wildstacker.nms.NMSAdapter;
import com.bgsoftware.wildstacker.utils.ServerVersion;
import com.bgsoftware.wildstacker.utils.entity.EntityStorage;
import com.bgsoftware.wildstacker.utils.items.GlowEnchantment;
import com.bgsoftware.wildstacker.utils.reflection.ReflectionUtils;
import com.bgsoftware.wildstacker.utils.threads.Executor;
import com.bgsoftware.wildstacker.utils.threads.StackService;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.lang.reflect.Field;

public final class WildStackerPlugin extends JavaPlugin implements WildStacker {

    private static WildStackerPlugin plugin;

    private SettingsHandler settingsHandler;
    private SystemHandler systemManager;
    private DataHandler dataHandler;
    private ProvidersHandler providersHandler;
    private LootHandler lootHandler;

    private NMSAdapter nmsAdapter;

    private boolean shouldEnable = true;

    @Override
    public void onLoad() {
        plugin = this;
        new Metrics(this);

        if(!ReflectionUtils.init()){
            shouldEnable = false;
        }
        else {
            loadNMSAdapter();
            loadAPI();
        }

        if(!shouldEnable)
            log("&cThere was an error while loading the plugin.");
    }

    @Override
    public void onEnable() {
        if(!shouldEnable) {
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        log("******** ENABLE START ********");

        GlowEnchantment.registerEnchantment();

        settingsHandler = new SettingsHandler(this);
        dataHandler = new DataHandler(this);
        systemManager = new SystemHandler(this);
        providersHandler = new ProvidersHandler(this);
        lootHandler = new LootHandler(this);

        EditorMenu.init(this);

        Locale.reload();

        if(ServerVersion.isAtLeast(ServerVersion.v1_8))
            getServer().getPluginManager().registerEvents(new BarrelsListener(this), this);
        getServer().getPluginManager().registerEvents(new BucketsListener(this), this);
        getServer().getPluginManager().registerEvents(new ChunksListener(this), this);
        getServer().getPluginManager().registerEvents(new EntitiesListener(this), this);
        getServer().getPluginManager().registerEvents(new ItemsListener(this), this);
        getServer().getPluginManager().registerEvents(new MenusListener(), this);
        getServer().getPluginManager().registerEvents(new NoClaimConflictListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayersListener(this), this);
        getServer().getPluginManager().registerEvents(new ShulkerOversizedPatch(), this);
        getServer().getPluginManager().registerEvents(new SpawnersListener(this), this);
        getServer().getPluginManager().registerEvents(new StewListener(this), this);
        getServer().getPluginManager().registerEvents(new ToolsListener(this), this);
        EventsListener.register(this);

        CommandsHandler commandsHandler = new CommandsHandler(this);
        getCommand("stacker").setExecutor(commandsHandler);
        getCommand("stacker").setTabCompleter(commandsHandler);

        if(Updater.isOutdated()) {
            log("");
            log("A new version is available (v" + Updater.getLatestVersion() + ")!");
            log("Version's description: \"" + Updater.getVersionDescription() + "\"");
            log("");
        }

        log("******** ENABLE DONE ********");
    }

    @Override
    public void onDisable() {
        log("Cancelling tasks...");

        try{
            Bukkit.getScheduler().cancelAllTasks();
        }catch(Throwable ex){
            try {
                BukkitScheduler.class.getMethod("cancelTasks", Plugin.class).invoke(Bukkit.getScheduler(), this);
            } catch (Exception ignored) { }
        }

        StackService.stop();
        Executor.stop();

        if(shouldEnable) {
            //We need to save the entire database
            systemManager.performCacheSave();

            log("Clearing database...");
            //We need to close the connection
            dataHandler.clearDatabase();

            log("Deleting spawner holograms...");
            for (StackedSpawner stackedSpawner : systemManager.getStackedSpawners())
                providersHandler.deleteHologram(stackedSpawner);
            log("Deleting barrel holograms...");
            for (StackedBarrel stackedBarrel : systemManager.getStackedBarrels()) {
                providersHandler.deleteHologram(stackedBarrel);
                stackedBarrel.getLocation().getChunk().load(true);
                stackedBarrel.removeDisplayBlock();
            }
        }

        EntityStorage.clearCache();
    }

    private void loadAPI(){
        try{
            Field instance = WildStackerAPI.class.getDeclaredField("instance");
            instance.setAccessible(true);
            instance.set(null, this);
        }catch(Exception ex){
            log("Failed to set-up API - disabling plugin...");
            ex.printStackTrace();
            shouldEnable = false;
        }
    }

    private void loadNMSAdapter(){
        String bukkitVersion = ServerVersion.getBukkitVersion();
        try{
            nmsAdapter = (NMSAdapter) Class.forName("com.bgsoftware.wildstacker.nms.NMSAdapter_" + bukkitVersion).newInstance();
        }catch(Exception ex){
            log("WildStacker doesn't support " + bukkitVersion + " - shutting down...");
            shouldEnable = false;
        }
    }

    public NMSAdapter getNMSAdapter() {
        return nmsAdapter;
    }

    public LootHandler getLootHandler() {
        return lootHandler;
    }

    public void setLootHandler(LootHandler lootHandler){
        this.lootHandler = lootHandler;
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

    public void setSettings(SettingsHandler settingsHandler){
        this.settingsHandler = settingsHandler;
    }

    public static void log(String message){
        message = ChatColor.translateAlternateColorCodes('&', message);
        if(message.contains(ChatColor.COLOR_CHAR + "")){
            Bukkit.getConsoleSender().sendMessage("[WildStacker] " + message);
        }
        else {
            plugin.getLogger().info(message);
        }
    }

    public static WildStackerPlugin getPlugin(){
        return plugin;
    }

}
