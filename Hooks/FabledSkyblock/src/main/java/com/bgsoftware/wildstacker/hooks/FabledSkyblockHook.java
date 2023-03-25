package com.bgsoftware.wildstacker.hooks;

import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.objects.StackedBarrel;
import com.bgsoftware.wildstacker.objects.WStackedBarrel;
import com.bgsoftware.wildstacker.objects.WStackedSpawner;
import com.songoda.skyblock.SkyBlock;
import com.songoda.skyblock.api.SkyBlockAPI;
import com.songoda.skyblock.core.compatibility.CompatibleMaterial;
import com.songoda.skyblock.island.Island;
import com.songoda.skyblock.levelling.IslandLevelManager;
import com.songoda.skyblock.levelling.QueuedIslandScan;
import com.songoda.skyblock.levelling.amount.BlockAmount;
import com.songoda.skyblock.levelling.calculator.Calculator;
import com.songoda.skyblock.levelling.calculator.CalculatorRegistry;
import com.songoda.skyblock.permission.BasicPermission;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Map;

@SuppressWarnings("unused")
public final class FabledSkyblockHook {

    private static final ReflectField<Map<CompatibleMaterial, BlockAmount>> ISLAND_SCAN_AMOUNTS =
            new ReflectField<>(QueuedIslandScan.class, Map.class, "amounts");

    private static WildStackerPlugin plugin;
    private static Map<Island, QueuedIslandScan> inScan;

    public static void register(WildStackerPlugin plugin) {
        FabledSkyblockHook.plugin = plugin;
        WildStackerCalculator calculator = new WildStackerCalculator();
        CalculatorRegistry.registerCalculator(calculator, CompatibleMaterial.SPAWNER);
        CalculatorRegistry.registerCalculator(calculator, CompatibleMaterial.CAULDRON);
        Bukkit.getPluginManager().registerEvents(new FabledListener(), plugin);
        inScan = new ReflectField<Map<Island, QueuedIslandScan>>(IslandLevelManager.class,
                Map.class, "inScan").get(SkyBlock.getInstance().getLevellingManager());
    }

    private static final class WildStackerCalculator implements Calculator {

        @Override
        public long getAmount(Block block) {
            if (plugin.getSystemManager().isStackedSpawner(block)) {
                return WStackedSpawner.of(block).getStackAmount();
            } else if (plugin.getSystemManager().isStackedBarrel(block)) {
                Island island = SkyBlockAPI.getIslandManager().getIslandAtLocation(block.getLocation()).getIsland();
                QueuedIslandScan islandScan = inScan.get(island);
                if (islandScan != null) {
                    Map<CompatibleMaterial, BlockAmount> amounts = ISLAND_SCAN_AMOUNTS.get(islandScan);
                    StackedBarrel stackedBarrel = WStackedBarrel.of(block);
                    CompatibleMaterial barrelMaterial = CompatibleMaterial.getMaterial(stackedBarrel.getBarrelItem(1));
                    amounts.computeIfAbsent(barrelMaterial, s -> new BlockAmount(0)).increaseAmount(stackedBarrel.getStackAmount());
                    BlockAmount cauldronAmount = amounts.computeIfAbsent(CompatibleMaterial.CAULDRON, s -> new BlockAmount(0));
                    cauldronAmount.setAmount(cauldronAmount.getAmount() - 1);
                }
            }

            return 0;
        }

    }

    private static final class FabledListener implements Listener {

        @EventHandler(priority = EventPriority.LOW)
        public void onCauldronInteract(PlayerInteractEvent e) {
            if (e.getClickedBlock() == null || (!e.getClickedBlock().getType().name().contains("CAULDRON") &&
                    !e.getClickedBlock().getType().name().contains("SPAWNER")))
                return;

            Island island = SkyBlock.getInstance().getIslandManager().getIslandAtLocation(e.getClickedBlock().getLocation());
            BasicPermission destroyPermission = SkyBlock.getInstance().getPermissionManager().getPermission("Destroy");

            if (island != null && !island.hasPermission(island.getRole(e.getPlayer()), destroyPermission))
                e.setCancelled(true);
        }

    }

}
