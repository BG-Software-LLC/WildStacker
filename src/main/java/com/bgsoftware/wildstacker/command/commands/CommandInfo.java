package com.bgsoftware.wildstacker.command.commands;

import com.bgsoftware.wildstacker.Locale;
import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.objects.StackedBarrel;
import com.bgsoftware.wildstacker.api.objects.StackedSpawner;
import com.bgsoftware.wildstacker.command.ICommand;
import com.bgsoftware.wildstacker.objects.WStackedBarrel;
import com.bgsoftware.wildstacker.objects.WStackedSpawner;
import com.bgsoftware.wildstacker.utils.EntityUtil;
import com.bgsoftware.wildstacker.utils.ItemUtil;
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

public final class CommandInfo implements ICommand {

    @Override
    public String getLabel() {
        return "info";
    }

    @Override
    public String getUsage() {
        return "stacker info";
    }

    @Override
    public String getPermission() {
        return "wildstacker.info";
    }

    @Override
    public String getDescription() {
        return "Shows information about a spawner.";
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
        if(!(sender instanceof Player)){
            sender.sendMessage(ChatColor.RED + "Only players can perform this command.");
            return;
        }

        Player pl = (Player) sender;

        Block targetBlock = pl.getTargetBlock(getMaterials("AIR", "WATER", "STATIONARY_WATER"), 10);

        if(targetBlock != null && targetBlock.getType() == Materials.SPAWNER.toBukkitType()){
            StackedSpawner stackedSpawner = WStackedSpawner.of(targetBlock);

            Locale.SPAWNER_INFO_HEADER.send(sender);
            Locale.SPAWNER_INFO_TYPE.send(sender, EntityUtil.getFormattedType(stackedSpawner.getSpawnedType().name()));
            Locale.SPAWNER_INFO_AMOUNT.send(sender, stackedSpawner.getStackAmount());
            Locale.SPAWNER_INFO_FOOTER.send(sender);
            return;
        }

        else if(plugin.getSystemManager().isStackedBarrel(targetBlock)){
            StackedBarrel stackedBarrel = WStackedBarrel.of(targetBlock);

            Locale.BARREL_INFO_HEADER.send(sender);
            Locale.BARREL_INFO_TYPE.send(sender, ItemUtil.getFormattedType(stackedBarrel.getBarrelItem(1)));
            Locale.BARREL_INFO_AMOUNT.send(sender, stackedBarrel.getStackAmount());
            Locale.BARREL_INFO_FOOTER.send(sender);
            return;
        }

        Locale.STACK_INFO_INVALID.send(sender);
    }

    @Override
    public List<String> tabComplete(WildStackerPlugin plugin, CommandSender sender, String[] args) {
        return new ArrayList<>();
    }

    private Set<Material> getMaterials(String... materials){
        Set<Material> materialsSet = Sets.newHashSet();

        for(String material : materials){
            try{
                materialsSet.add(Material.valueOf(material));
            }catch(IllegalArgumentException ignored){}
        }

        return materialsSet;
    }

}
