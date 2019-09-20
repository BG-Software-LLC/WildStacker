package com.bgsoftware.wildstacker.command.commands;

import com.bgsoftware.wildstacker.Locale;
import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.command.ICommand;
import com.bgsoftware.wildstacker.utils.items.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CommandSimulate implements ICommand {

    @Override
    public String getLabel() {
        return "simulate";
    }

    @Override
    public String getUsage() {
        return "stacker simulate <player-name> [amount]";
    }

    @Override
    public String getPermission() {
        return "wildstacker.simulate.give";
    }

    @Override
    public String getDescription() {
        return "Give a simulate tool to a player.";
    }

    @Override
    public int getMinArgs() {
        return 2;
    }

    @Override
    public int getMaxArgs() {
        return 3;
    }

    @Override
    public void perform(WildStackerPlugin plugin, CommandSender sender, String[] args) {
        Player targetPlayer = Bukkit.getPlayer(args[1]);

        if(targetPlayer == null){
            Locale.INVALID_PLAYER.send(sender, args[1]);
            return;
        }

        int amount = 1;
        if(args.length == 3){
            try{
                amount = Integer.parseInt(args[2]);
            }catch(IllegalArgumentException ex){
                Locale.INVALID_NUMBER.send(sender, args[2]);
                return;
            }
        }

        targetPlayer.getInventory().addItem(new ItemBuilder(plugin.getSettings().simulateTool).build(amount));
        Locale.SIMULATE_GIVE_PLAYER.send(sender, targetPlayer.getName(), amount);
        if(!targetPlayer.equals(sender))
            Locale.SIMULATE_RECEIVE.send(targetPlayer, amount, sender.getName());
    }

    @Override
    public List<String> tabComplete(WildStackerPlugin plugin, CommandSender sender, String[] args) {
        return args.length == 2 ? null : new ArrayList<>();
    }
}
