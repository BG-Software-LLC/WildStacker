package com.bgsoftware.wildstacker.hooks;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.objects.StackedBarrel;
import com.bgsoftware.wildstacker.objects.WStackedBarrel;
import com.bgsoftware.wildstacker.objects.WStackedSpawner;
import com.songoda.skyblock.SkyBlock;
import com.songoda.skyblock.api.SkyBlockAPI;
import com.songoda.skyblock.core.compatibility.CompatibleMaterial;
import com.songoda.skyblock.island.Island;
import com.songoda.skyblock.levelling.rework.IslandLevelManager;
import com.songoda.skyblock.levelling.rework.IslandScan;
import com.songoda.skyblock.levelling.rework.amount.BlockAmount;
import com.songoda.skyblock.levelling.rework.calculator.Calculator;
import com.songoda.skyblock.levelling.rework.calculator.CalculatorRegistry;
import com.songoda.skyblock.permission.BasicPermission;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public final class PluginHook_FabledSkyblock implements Calculator {

    private final WildStackerPlugin plugin;
    private Map<Island, IslandScan> inScan = new HashMap<>();
    private Field amountsField;

    private PluginHook_FabledSkyblock(WildStackerPlugin plugin){
        this.plugin = plugin;
        try{
            Field scanField = IslandLevelManager.class.getDeclaredField("inScan");
            scanField.setAccessible(true);
            //noinspection unchecked
            inScan = (Map<Island, IslandScan>) scanField.get(SkyBlock.getInstance().getLevellingManager());

            amountsField = IslandScan.class.getDeclaredField("amounts");
            amountsField.setAccessible(true);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    @Override
    public long getAmount(Block block) {
        if(plugin.getSystemManager().isStackedSpawner(block)){
            return WStackedSpawner.of(block).getStackAmount();
        }
        else if(plugin.getSystemManager().isStackedBarrel(block)){
            Island island = SkyBlockAPI.getIslandManager().getIslandAtLocation(block.getLocation()).getIsland();
            IslandScan islandScan = inScan.get(island);
            if(islandScan != null) {
                try {
                    //noinspection unchecked
                    Map<CompatibleMaterial, BlockAmount> amounts = (Map<CompatibleMaterial, BlockAmount>) amountsField.get(islandScan);
                    StackedBarrel stackedBarrel = WStackedBarrel.of(block);
                    CompatibleMaterial barrelMaterial = CompatibleMaterial.getMaterial(stackedBarrel.getBarrelItem(1));
                    amounts.computeIfAbsent(barrelMaterial, s -> new BlockAmount(0)).increaseAmount(stackedBarrel.getStackAmount());
                    BlockAmount cauldronAmount = amounts.computeIfAbsent(CompatibleMaterial.CAULDRON, s -> new BlockAmount(0));
                    cauldronAmount.setAmount(cauldronAmount.getAmount() - 1);
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        }

        return 0;
    }

    public static void register(WildStackerPlugin plugin){
        PluginHook_FabledSkyblock calculator = new PluginHook_FabledSkyblock(plugin);
        CalculatorRegistry.registerCalculator(calculator, CompatibleMaterial.SPAWNER);
        CalculatorRegistry.registerCalculator(calculator, CompatibleMaterial.CAULDRON);
        Bukkit.getPluginManager().registerEvents(new FabledListener(), plugin);
    }

    private static final class FabledListener implements Listener{

        @EventHandler(priority = EventPriority.LOW)
        public void onCauldronInteract(PlayerInteractEvent e){
            if(e.getClickedBlock() == null || (!e.getClickedBlock().getType().name().contains("CAULDRON") &&
                    !e.getClickedBlock().getType().name().contains("SPAWNER")))
                return;

            Island island = SkyBlock.getInstance().getIslandManager().getIslandAtLocation(e.getClickedBlock().getLocation());
            BasicPermission destroyPermission = SkyBlock.getInstance().getPermissionManager().getPermission("Destroy");

            if(island != null && !island.hasPermission(island.getRole(e.getPlayer()), destroyPermission))
                e.setCancelled(true);
        }

    }

}
