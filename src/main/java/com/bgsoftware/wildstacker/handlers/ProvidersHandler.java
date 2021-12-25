package com.bgsoftware.wildstacker.handlers;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.hooks.ClaimsProvider;
import com.bgsoftware.wildstacker.hooks.ClaimsProvider_PlotSquaredLegacy;
import com.bgsoftware.wildstacker.hooks.ClaimsProvider_WorldGuard;
import com.bgsoftware.wildstacker.hooks.CoreProtectHook;
import com.bgsoftware.wildstacker.hooks.CrazyEnchantmentsHook;
import com.bgsoftware.wildstacker.hooks.DataSerializer_NBTInjector;
import com.bgsoftware.wildstacker.hooks.EconomyHook;
import com.bgsoftware.wildstacker.hooks.EntityTypeProvider;
import com.bgsoftware.wildstacker.hooks.FastAsyncWEHook;
import com.bgsoftware.wildstacker.hooks.JobsHook;
import com.bgsoftware.wildstacker.hooks.McMMOHook;
import com.bgsoftware.wildstacker.hooks.PluginHook_FabledSkyblock;
import com.bgsoftware.wildstacker.hooks.PluginHook_Novucs;
import com.bgsoftware.wildstacker.hooks.PluginHooks;
import com.bgsoftware.wildstacker.hooks.ProtocolLibHook;
import com.bgsoftware.wildstacker.hooks.ShopGUIPlusHook;
import com.bgsoftware.wildstacker.hooks.SlimefunHook;
import com.bgsoftware.wildstacker.hooks.SpawnersProvider;
import com.bgsoftware.wildstacker.hooks.SpawnersProvider_Default;
import com.bgsoftware.wildstacker.hooks.SpawnersProvider_MineableSpawners;
import com.bgsoftware.wildstacker.hooks.SpawnersProvider_SilkSpawners;
import com.bgsoftware.wildstacker.hooks.SuperiorSkyblockHook;
import com.bgsoftware.wildstacker.listeners.PaperListener;
import com.bgsoftware.wildstacker.listeners.ProvidersListener;
import com.bgsoftware.wildstacker.listeners.plugins.BossListener;
import com.bgsoftware.wildstacker.listeners.plugins.ClearLaggListener;
import com.bgsoftware.wildstacker.listeners.plugins.CustomBossesListener;
import com.bgsoftware.wildstacker.listeners.plugins.EchoPetListener;
import com.bgsoftware.wildstacker.listeners.plugins.EliteBossesListener;
import com.bgsoftware.wildstacker.listeners.plugins.EpicBossesListener;
import com.bgsoftware.wildstacker.listeners.plugins.EpicSpawnersListener;
import com.bgsoftware.wildstacker.listeners.plugins.JetsMinionsListener;
import com.bgsoftware.wildstacker.listeners.plugins.MiniaturePetsListener;
import com.bgsoftware.wildstacker.listeners.plugins.MoreBossesListener;
import com.bgsoftware.wildstacker.listeners.plugins.MyPetListener;
import com.bgsoftware.wildstacker.listeners.plugins.MythicMobsListener;
import com.bgsoftware.wildstacker.listeners.plugins.PinataPartyListener;
import com.bgsoftware.wildstacker.listeners.plugins.SilkSpawnersListener;
import com.bgsoftware.wildstacker.listeners.plugins.SuperiorSkyblockListener;
import com.bgsoftware.wildstacker.utils.ServerVersion;
import com.bgsoftware.wildstacker.utils.threads.Executor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@SuppressWarnings({"unused", "WeakerAccess"})
public final class ProvidersHandler {

    private final WildStackerPlugin plugin;

    private SpawnersProvider spawnersProvider;
    private List<ClaimsProvider> claimsProviders;
    private final List<EntityTypeProvider> entityTypeProviders = new ArrayList<>();

    public ProvidersHandler(WildStackerPlugin plugin) {
        this.plugin = plugin;

        Executor.sync(() -> {
            WildStackerPlugin.log("Loading providers started...");
            long startTime = System.currentTimeMillis();

            loadSpawnersProvider();
            loadClaimsProvider();
            loadPluginHooks(plugin, null, true);

            Bukkit.getPluginManager().registerEvents(new ProvidersListener(plugin), plugin);

            fixConflicts(plugin);

            if (hasPaperEntityRemoveSupport())
                Bukkit.getPluginManager().registerEvents(new PaperListener(), plugin);

            WildStackerPlugin.log("Loading providers done (Took " + (System.currentTimeMillis() - startTime) + "ms)");
        }, 0L);

        if (plugin.getSettings().superiorSkyblockHook)
            Bukkit.getPluginManager().registerEvents(new SuperiorSkyblockListener(), plugin);

        Executor.sync(() -> {
            if (Bukkit.getPluginManager().isPluginEnabled("ASkyBlock") &&
                    Bukkit.getPluginManager().getPlugin("ASkyBlock").getDescription().getAuthors().stream().noneMatch(a -> a.contains("Ome_R"))) {
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

    private static boolean hasPaperEntityRemoveSupport() {
        try {
            Class.forName("com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent");
            return true;
        } catch (Throwable ex) {
            return false;
        }
    }

    private static boolean doesClassExist(String clazz) {
        try {
            Class.forName(clazz);
            return true;
        } catch (Throwable ex) {
            return false;
        }
    }

    private void loadSpawnersProvider() {
        if (Bukkit.getPluginManager().isPluginEnabled("SilkSpawners") &&
                Bukkit.getPluginManager().getPlugin("SilkSpawners").getDescription().getAuthors().contains("xGhOsTkiLLeRx"))
            spawnersProvider = new SpawnersProvider_SilkSpawners();
        else if (Bukkit.getPluginManager().isPluginEnabled("MineableSpawners"))
            spawnersProvider = new SpawnersProvider_MineableSpawners();
        else spawnersProvider = new SpawnersProvider_Default();
    }

    private void loadClaimsProvider() {
        claimsProviders = new ArrayList<>();

        if (Bukkit.getPluginManager().isPluginEnabled("Factions")) {
            if (Bukkit.getPluginManager().getPlugin("Factions").getDescription().getAuthors().contains("drtshock")) {
                Optional<ClaimsProvider> claimsProvider = createInstance("ClaimsProvider_FactionsUUID");
                claimsProvider.ifPresent(claimsProviders::add);
            }
            else {
                Optional<ClaimsProvider> claimsProvider = createInstance("ClaimsProvider_MassiveFactions");
                claimsProvider.ifPresent(claimsProviders::add);
            }
        }
        if (Bukkit.getPluginManager().isPluginEnabled("PlotSquared")) {
            Plugin plugin = Bukkit.getPluginManager().getPlugin("PlotSquared");
            if (plugin.getDescription().getVersion().startsWith("6.")) {
                try {
                    claimsProviders.add((ClaimsProvider) Class.forName("com.bgsoftware.wildstacker.hooks.ClaimsProvider_PlotSquaredV6").newInstance());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else if (plugin.getDescription().getVersion().startsWith("5.")) {
                Optional<ClaimsProvider> claimsProvider = createInstance("ClaimsProvider_PlotSquared5");
                claimsProvider.ifPresent(claimsProviders::add);
            } else if (plugin.getDescription().getMain().contains("com.github")) {
                Optional<ClaimsProvider> claimsProvider = createInstance("ClaimsProvider_PlotSquared4");
                claimsProvider.ifPresent(claimsProviders::add);
            } else {
                claimsProviders.add(new ClaimsProvider_PlotSquaredLegacy());
            }
        }
        if (Bukkit.getPluginManager().isPluginEnabled("WorldGuard"))
            claimsProviders.add(new ClaimsProvider_WorldGuard());
    }

    private void loadEntityTypeProviders() {
        entityTypeProviders.clear();
        if (Bukkit.getPluginManager().isPluginEnabled("Citizens")) {
            Optional<EntityTypeProvider> entityTypeProvider = createInstance("EntityTypeProvider_Citizens");
            entityTypeProvider.ifPresent(entityTypeProviders::add);
        }
    }

    public void loadPluginHooks(WildStackerPlugin plugin, Plugin toCheck, boolean enable) {
        PluginManager pluginManager = plugin.getServer().getPluginManager();

        // Load listeners
        if (enable && isPlugin(toCheck, "Citizens") && pluginManager.isPluginEnabled("Citizens"))
            PluginHooks.isCitizensEnabled = true;
        if (enable && isPlugin(toCheck, "ClearLag") && pluginManager.isPluginEnabled("ClearLag"))
            pluginManager.registerEvents(new ClearLaggListener(plugin), plugin);
        if (enable && isPlugin(toCheck, "SilkSpawners") && pluginManager.isPluginEnabled("SilkSpawners"))
            pluginManager.registerEvents(new SilkSpawnersListener(plugin), plugin);
        if (enable && isPlugin(toCheck, "CustomBosses") && pluginManager.isPluginEnabled("CustomBosses"))
            pluginManager.registerEvents(new CustomBossesListener(), plugin);
        if (enable && isPlugin(toCheck, "EpicBosses") && pluginManager.isPluginEnabled("EpicBosses"))
            pluginManager.registerEvents(new EpicBossesListener(), plugin);
        if (enable && isPlugin(toCheck, "MythicMobs") && pluginManager.isPluginEnabled("MythicMobs"))
            pluginManager.registerEvents(new MythicMobsListener(), plugin);
        if (enable && isPlugin(toCheck, "LevelledMobs") && pluginManager.isPluginEnabled("LevelledMobs"))
            PluginHooks.isLevelledMobsEnabled = ServerVersion.isAtLeast(ServerVersion.v1_14);
        if (enable && isPlugin(toCheck, "MyPet") && pluginManager.isPluginEnabled("MyPet"))
            pluginManager.registerEvents(new MyPetListener(), plugin);
        if (enable && isPlugin(toCheck, "EchoPet") && pluginManager.isPluginEnabled("EchoPet"))
            pluginManager.registerEvents(new EchoPetListener(), plugin);
        if (enable && isPlugin(toCheck, "EpicSpawners") && doesClassExist("com.songoda.epicspawners.api.events.SpawnerSpawnEvent"))
            EpicSpawnersListener.register(plugin);
        if (enable && isPlugin(toCheck, "CrazyEnchantments") && pluginManager.isPluginEnabled("CrazyEnchantments"))
            CrazyEnchantmentsHook.register();
        if (enable && isPlugin(toCheck, "Boss") && pluginManager.isPluginEnabled("Boss"))
            BossListener.register(plugin);
        if (enable && isPlugin(toCheck, "EliteBosses") && pluginManager.isPluginEnabled("EliteBosses"))
            pluginManager.registerEvents(new EliteBossesListener(), plugin);
        if (enable && isPlugin(toCheck, "JetsMinions") && pluginManager.isPluginEnabled("JetsMinions"))
            pluginManager.registerEvents(new JetsMinionsListener(), plugin);
        if (enable && isPlugin(toCheck, "Morebosses") && pluginManager.isPluginEnabled("Morebosses"))
            pluginManager.registerEvents(new MoreBossesListener(), plugin);
        if (enable && isPlugin(toCheck, "PinataParty") && pluginManager.isPluginEnabled("PinataParty"))
            pluginManager.registerEvents(new PinataPartyListener(), plugin);
        if (enable && isPlugin(toCheck, "MiniaturePets") && pluginManager.isPluginEnabled("MiniaturePets"))
            pluginManager.registerEvents(new MiniaturePetsListener(), plugin);

        //Load plugin hooks
        if (isPlugin(toCheck, "mcMMO") && pluginManager.isPluginEnabled("mcMMO"))
            McMMOHook.setEnabled(enable);
        if (isPlugin(toCheck, "CoreProtect") && pluginManager.isPluginEnabled("CoreProtect"))
            CoreProtectHook.setEnabled(enable);
        if (isPlugin(toCheck, "WorldGuard") && pluginManager.isPluginEnabled("WorldGuard"))
            PluginHooks.isWorldGuardEnabled = enable;
        if (isPlugin(toCheck, "WildTools") && pluginManager.isPluginEnabled("WildTools"))
            PluginHooks.isWildToolsEnabled = enable;
        if (isPlugin(toCheck, "ProtocolLib") && pluginManager.isPluginEnabled("ProtocolLib"))
            ProtocolLibHook.setEnabled(enable);
        if (isPlugin(toCheck, "Vault") && pluginManager.isPluginEnabled("Vault"))
            EconomyHook.setEnabled(enable);
        if (isPlugin(toCheck, "MergedSpawner") && pluginManager.isPluginEnabled("MergedSpawner"))
            PluginHooks.isMergedSpawnersEnabled = enable;
        if (isPlugin(toCheck, "FastAsyncWorldEdit") && pluginManager.isPluginEnabled("FastAsyncWorldEdit"))
            PluginHooks.isFastAsyncWorldEditEnabled = enable;
        if (enable && isPlugin(toCheck, "FactionsTop") && doesClassExist("net.novucs.ftop.FactionsTopPlugin"))
            PluginHook_Novucs.setEnabled(plugin);
        if (enable && isPlugin(toCheck, "ShopGUIPlus") && doesClassExist("net.brcdev.shopgui.ShopGuiPlugin"))
            ShopGUIPlusHook.setEnabled();
        if (isPlugin(toCheck, "Jobs") && pluginManager.isPluginEnabled("Jobs"))
            JobsHook.setEnabled(enable);
        if (enable && isPlugin(toCheck, "FabledSkyBlock") && pluginManager.isPluginEnabled("FabledSkyBlock"))
            PluginHook_FabledSkyblock.register(plugin);
        if (enable && isPlugin(toCheck, "SuperiorSkyblock2") && pluginManager.isPluginEnabled("SuperiorSkyblock2"))
            SuperiorSkyblockHook.register(plugin);
        if (enable && isPlugin(toCheck, "NBTInjector") && doesClassExist("de.tr7zw.nbtinjector.NBTInjector"))
            DataSerializer_NBTInjector.register(plugin);
        if (isPlugin(toCheck, "Slimefun") && pluginManager.isPluginEnabled("Slimefun"))
            SlimefunHook.setEnabled(enable);
    }

    public SpawnersProvider getSpawnersProvider() {
        return spawnersProvider;
    }

    public boolean hasClaimAccess(Player player, Location location) {
        for (ClaimsProvider claimsProvider : claimsProviders) {
            if (!claimsProvider.hasClaimAccess(player, location))
                return false;
        }

        return true;
    }

    @Nullable
    public String checkStackEntity(Entity entity) {
        for (EntityTypeProvider entityTypeProvider : entityTypeProviders) {
            String failureReason = entityTypeProvider.checkStackEntity(entity);
            if (failureReason != null)
                return failureReason;
        }

        return null;
    }

    private static boolean isPlugin(Plugin plugin, String pluginName) {
        return plugin == null || plugin.getName().equals(pluginName);
    }

    private void fixConflicts(WildStackerPlugin plugin) {
        List<String> messages = new ArrayList<>();
        if (PluginHooks.isEpicSpawnersEnabled) {
            messages.add("Detected EpicSpawners - Disabling spawners stacking...");
        }
        if (PluginHooks.isMergedSpawnersEnabled) {
            messages.add("Detected MergedSpawner - Disabling spawners stacking...");
        }
        if (PluginHooks.isFastAsyncWorldEditEnabled && plugin.getSettings().itemsStackingEnabled) {
            //WildStacker disabled the tick limiter for items.
            try {
                FastAsyncWEHook.disableTicksLimiter();
                messages.add("Detected FastAsyncWorldEdit - Disabling ticks limiter for items...");
            } catch (Throwable ignored) {
            }
        }

        if (!messages.isEmpty()) {
            WildStackerPlugin.log("");
            for (String msg : messages)
                WildStackerPlugin.log(msg);
            WildStackerPlugin.log("");
        }

    }

    private void registerHook(String className) {
        try {
            Class<?> clazz = Class.forName("com.bgsoftware.wildstacker.hooks." + className);
            Method registerMethod = clazz.getMethod("register", WildStackerPlugin.class);
            registerMethod.invoke(null, plugin);
        } catch (Exception ignored) {
        }
    }

    private <T> Optional<T> createInstance(String className) {
        try {
            Class<?> clazz = Class.forName("com.bgsoftware.wildstacker.hooks." + className);
            try {
                Method compatibleMethod = clazz.getDeclaredMethod("isCompatible");
                if (!(boolean) compatibleMethod.invoke(null))
                    return Optional.empty();
            } catch (Exception ignored) {
            }

            try {
                Constructor<?> constructor = clazz.getConstructor(WildStackerPlugin.class);
                // noinspection unchecked
                return Optional.of((T) constructor.newInstance(plugin));
            } catch (Exception error) {
                // noinspection unchecked
                return Optional.of((T) clazz.newInstance());
            }
        } catch (ClassNotFoundException ignored) {
            return Optional.empty();
        } catch (Exception error) {
            error.printStackTrace();
            return Optional.empty();
        }
    }

}
