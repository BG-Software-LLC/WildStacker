package xyz.wildseries.wildstacker.command.commands;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.wildseries.wildstacker.Locale;
import xyz.wildseries.wildstacker.WildStackerPlugin;
import xyz.wildseries.wildstacker.api.objects.StackedBarrel;
import xyz.wildseries.wildstacker.api.objects.StackedSpawner;
import xyz.wildseries.wildstacker.command.ICommand;
import xyz.wildseries.wildstacker.objects.WStackedBarrel;
import xyz.wildseries.wildstacker.objects.WStackedSpawner;
import xyz.wildseries.wildstacker.utils.EntityUtil;
import xyz.wildseries.wildstacker.utils.ItemUtil;
import xyz.wildseries.wildstacker.utils.legacy.Materials;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

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

        Block targetBlock = pl.getTargetBlock((HashSet<Material>) null, 10);

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
}
