package com.bgsoftware.wildstacker.command;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import org.bukkit.command.CommandSender;

import java.util.List;

public interface ICommand {

    String getLabel();

    String getUsage();

    String getPermission();

    String getDescription();

    int getMinArgs();

    int getMaxArgs();

    void perform(WildStackerPlugin plugin, CommandSender sender, String[] args);

    List<String> tabComplete(WildStackerPlugin plugin, CommandSender sender, String[] args);

}
