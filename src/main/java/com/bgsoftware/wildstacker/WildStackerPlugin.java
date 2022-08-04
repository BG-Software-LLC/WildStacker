package com.bgsoftware.wildstacker;

import com.bgsoftware.wildstacker.api.WildStacker;
import com.bgsoftware.wildstacker.api.WildStackerAPI;
import com.bgsoftware.wildstacker.command.CommandsHandler;
import com.bgsoftware.wildstacker.handlers.DataHandler;
import com.bgsoftware.wildstacker.handlers.LootHandler;
import com.bgsoftware.wildstacker.handlers.ProvidersHandler;
import com.bgsoftware.wildstacker.handlers.SettingsHandler;
import com.bgsoftware.wildstacker.handlers.SystemHandler;
import com.bgsoftware.wildstacker.handlers.UpgradesHandler;
import com.bgsoftware.wildstacker.listeners.BarrelsListener;
import com.bgsoftware.wildstacker.listeners.BucketsListener;
import com.bgsoftware.wildstacker.listeners.ChunksListener;
import com.bgsoftware.wildstacker.listeners.EntitiesListener;
import com.bgsoftware.wildstacker.listeners.ItemsListener;
import com.bgsoftware.wildstacker.listeners.MenusListener;
import com.bgsoftware.wildstacker.listeners.NoClaimConflictListener;
import com.bgsoftware.wildstacker.listeners.PlayersListener;
import com.bgsoftware.wildstacker.listeners.ServerTickListener;
import com.bgsoftware.wildstacker.listeners.ShulkerOversizedPatch;
import com.bgsoftware.wildstacker.listeners.SpawnersListener;
import com.bgsoftware.wildstacker.listeners.StewListener;
import com.bgsoftware.wildstacker.listeners.ToolsListener;
import com.bgsoftware.wildstacker.listeners.WorldsListener;
import com.bgsoftware.wildstacker.listeners.events.EventsListener;
import com.bgsoftware.wildstacker.menu.EditorMenu;
import com.bgsoftware.wildstacker.metrics.Metrics;
import com.bgsoftware.wildstacker.nms.NMSAdapter;
import com.bgsoftware.wildstacker.nms.NMSEntities;
import com.bgsoftware.wildstacker.nms.NMSHolograms;
import com.bgsoftware.wildstacker.nms.NMSSpawners;
import com.bgsoftware.wildstacker.utils.ServerVersion;
import com.bgsoftware.wildstacker.utils.entity.EntityStorage;
import com.bgsoftware.wildstacker.utils.items.GlowEnchantment;
import com.bgsoftware.wildstacker.utils.threads.Executor;
import com.bgsoftware.wildstacker.utils.threads.StackService;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

public final class WildStackerPlugin extends JavaPlugin implements WildStacker {

    private static WildStackerPlugin plugin;

    private SettingsHandler settingsHandler;
    private SystemHandler systemManager;
    private UpgradesHandler upgradesHandler;
    private DataHandler dataHandler;
    private ProvidersHandler providersHandler;
    private LootHandler lootHandler;

    private NMSAdapter nmsAdapter;
    private NMSHolograms nmsHolograms;
    private NMSSpawners nmsSpawners;
    private NMSEntities nmsEntities;

    private boolean shouldEnable = true;

    public static void log(String message) {
        message = ChatColor.translateAlternateColorCodes('&', message);
        if (message.contains(ChatColor.COLOR_CHAR + "")) {
            Bukkit.getConsoleSender().sendMessage("[WildStacker] " + message);
        } else {
            plugin.getLogger().info(message);
        }
    }

    public static WildStackerPlugin getPlugin() {
        return plugin;
    }

    @Override
    public void onLoad() {
        plugin = this;

        // Setting the default locale to English will fix issues related to using upper case in Turkish.
        // https://stackoverflow.com/questions/11063102/using-locales-with-javas-tolowercase-and-touppercase
        java.util.Locale.setDefault(java.util.Locale.ENGLISH);

        new Metrics(this);

        loadNMSAdapter();
        loadAPI();

        if (!shouldEnable)
            log("&cThere was an error while loading the plugin.");
    }

    @Override
    public void onDisable() {
        log("Cancelling tasks...");

        try {
            Bukkit.getScheduler().cancelAllTasks();
        } catch (Throwable ex) {
            Bukkit.getScheduler().cancelTasks(this);
        }

        log("Shutting down stacking service...");

        StackService.stop();

        if (shouldEnable) {
            log("Performing entity&items save");

            for (World world : Bukkit.getWorlds()) {
                for (Chunk chunk : world.getLoadedChunks())
                    systemManager.handleChunkUnload(chunk);
            }

            //We need to save the entire database
            systemManager.performCacheSave();

            Executor.stopData();

            log("Clearing database...");
            //We need to close the connection
            dataHandler.clearDatabase();
        }

        log("Stopping executor...");

        Executor.stop();

        EntityStorage.clearCache();
    }

    @Override
    public void onEnable() {
        if (!shouldEnable) {
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        log("******** ENABLE START ********");

        GlowEnchantment.registerEnchantment();

        dataHandler = new DataHandler(this);
        systemManager = new SystemHandler(this);
        upgradesHandler = new UpgradesHandler();
        settingsHandler = new SettingsHandler(this);
        providersHandler = new ProvidersHandler(this);
        lootHandler = new LootHandler(this);

        EditorMenu.init(this);

        Locale.reload();

        if (ServerVersion.isAtLeast(ServerVersion.v1_8))
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
        getServer().getPluginManager().registerEvents(new WorldsListener(this), this);

        try {
            Class.forName("com.destroystokyo.paper.event.server.ServerTickEndEvent");
            getServer().getPluginManager().registerEvents(new ServerTickListener(), this);
        } catch (Throwable ignored) {
        }

        EventsListener.register(this);

        CommandsHandler commandsHandler = new CommandsHandler(this);
        getCommand("stacker").setExecutor(commandsHandler);
        getCommand("stacker").setTabCompleter(commandsHandler);

        if (Updater.isOutdated()) {
            log("");
            log("A new version is available (v" + Updater.getLatestVersion() + ")!");
            log("Version's description: \"" + Updater.getVersionDescription() + "\"");
            log("");
        }

        log("******** ENABLE DONE ********");
    }

    private void loadAPI() {
        try {
            WildStackerAPI.setPluginInstance(this);
        } catch (UnsupportedOperationException ex) {
            log("Failed to set-up API - disabling plugin...");
            ex.printStackTrace();
            shouldEnable = false;
        }
    }

    private void loadNMSAdapter() {
        String bukkitVersion = ServerVersion.getBukkitVersion();
        try {
            nmsAdapter = (NMSAdapter) Class.forName(String.format("com.bgsoftware.wildstacker.nms.%s.NMSAdapter", bukkitVersion)).newInstance();

            if(!nmsAdapter.isMappingsSupported()) {
                log("WildStacker does not support your version mappings... Please contact @Ome_R");
                shouldEnable = false;
                return;
            }

            nmsHolograms = (NMSHolograms) Class.forName(String.format("com.bgsoftware.wildstacker.nms.%s.NMSHolograms", bukkitVersion)).newInstance();
            nmsSpawners = (NMSSpawners) Class.forName(String.format("com.bgsoftware.wildstacker.nms.%s.NMSSpawners", bukkitVersion)).newInstance();
            nmsEntities = (NMSEntities) Class.forName(String.format("com.bgsoftware.wildstacker.nms.%s.NMSEntities", bukkitVersion)).newInstance();
        } catch (Exception ex) {
            log("WildStacker doesn't support " + bukkitVersion + " - shutting down...");
            shouldEnable = false;
        }
    }

    public NMSAdapter getNMSAdapter() {
        return nmsAdapter;
    }

    public NMSHolograms getNMSHolograms() {
        return nmsHolograms;
    }

    public NMSSpawners getNMSSpawners() {
        return nmsSpawners;
    }

    public NMSEntities getNMSEntities() {
        return nmsEntities;
    }

    public LootHandler getLootHandler() {
        return lootHandler;
    }

    public void setLootHandler(LootHandler lootHandler) {
        this.lootHandler = lootHandler;
    }

    public ProvidersHandler getProviders() {
        return providersHandler;
    }

    public DataHandler getDataHandler() {
        return dataHandler;
    }

    @Override
    public SystemHandler getSystemManager() {
        return systemManager;
    }

    @Override
    public UpgradesHandler getUpgradesManager() {
        return upgradesHandler;
    }

    public SettingsHandler getSettings() {
        return settingsHandler;
    }

    public void setSettings(SettingsHandler settingsHandler) {
        this.settingsHandler = settingsHandler;
    }

}
