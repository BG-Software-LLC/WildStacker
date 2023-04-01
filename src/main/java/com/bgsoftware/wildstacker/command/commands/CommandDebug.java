package com.bgsoftware.wildstacker.command.commands;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.command.ICommand;
import com.bgsoftware.wildstacker.stacker.spawners.WStackedSpawner;
import com.bgsoftware.wildstacker.utils.legacy.Materials;
import com.google.common.collect.Sets;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class CommandDebug implements ICommand {

    @Override
    public String getLabel() {
        return "debug";
    }

    @Override
    public String getUsage() {
        return "stacker debug";
    }

    @Override
    public String getPermission() {
        return "wildstacker.debug";
    }

    @Override
    public String getDescription() {
        return "Toggle debug mode for a spawner.";
    }

    @Override
    public int getMinArgs() {
        return 1;
    }

    @Override
    public int getMaxArgs() {
        return 1;
    }

    @Override
    public void perform(WildStackerPlugin plugin, CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can perform this command.");
            return;
        }

        Player pl = (Player) sender;

        Block targetBlock = pl.getTargetBlock(getMaterials("AIR", "WATER", "STATIONARY_WATER"), 10);

        if (targetBlock == null || targetBlock.getType() != Materials.SPAWNER.toBukkitType()) {
            sender.sendMessage(ChatColor.RED + "You must look directly at a spawner.");
            return;
        }

        WStackedSpawner stackedSpawner = (WStackedSpawner) WStackedSpawner.of(targetBlock);

        if(stackedSpawner.isDebug()) {
            stackedSpawner.setDebug(false);
            sender.sendMessage("" + ChatColor.GOLD + ChatColor.BOLD + "WildStacker " + ChatColor.GRAY + "Toggled debug mode " + ChatColor.RED + "OFF" + ChatColor.GRAY + ".");
        } else {
            stackedSpawner.setDebug(true);
            sender.sendMessage("" + ChatColor.GOLD + ChatColor.BOLD + "WildStacker " + ChatColor.GRAY + "Toggled debug mode " + ChatColor.GREEN + "ON" + ChatColor.GRAY + ".");
        }
    }

    @Override
    public List<String> tabComplete(WildStackerPlugin plugin, CommandSender sender, String[] args) {
        return new ArrayList<>();
    }

    private Set<Material> getMaterials(String... materials) {
        Set<Material> materialsSet = Sets.newHashSet();

        for (String material : materials) {
            try {
                materialsSet.add(Material.valueOf(material));
            } catch (IllegalArgumentException ignored) {
            }
        }

        return materialsSet;
    }

}
