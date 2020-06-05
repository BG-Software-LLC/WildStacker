package com.bgsoftware.wildstacker.command.commands;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.command.ICommand;
import com.bgsoftware.wildstacker.utils.threads.Executor;
import com.bgsoftware.wildstacker.utils.threads.StackService;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public final class CommandTest implements ICommand {

    @Override
    public String getLabel() {
        return "test";
    }

    @Override
    public String getUsage() {
        return "stacker test";
    }

    @Override
    public String getPermission() {
        return "wildstacker.test";
    }

    @Override
    public String getDescription() {
        return "Test the status of the stacking thread.";
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
        World world = sender instanceof Player ? ((Player) sender).getWorld() : Bukkit.getWorlds().get(0);
        //noinspection unchecked
        CompletableFuture<Boolean>[] completableFutures = new CompletableFuture[2];

        for(StackService.StackType stackType : StackService.StackType.values()){
            completableFutures[stackType.getId()] = new CompletableFuture<>();
            StackService.execute(world, stackType, () -> completableFutures[stackType.getId()].complete(true));
        }

        Executor.async(() -> {
            StringBuilder stringBuilder = new StringBuilder();
            boolean shouldRestart = false;

            for(StackService.StackType stackType : StackService.StackType.values()){
                boolean success = false;

                try {
                    success = completableFutures[stackType.getId()].get(1, TimeUnit.SECONDS);
                }catch(Exception ignored){}

                
                stringBuilder.append("\n").append(ChatColor.YELLOW).append(stackType).append(" Stacking Thread Status: ")
                        .append(success ? ChatColor.GREEN + "ACTIVE" : ChatColor.RED + "INACTIVE");

                if(!success)
                    shouldRestart = true;
            }

            if(shouldRestart){
                stringBuilder.append("\n").append(ChatColor.YELLOW).append("Performing thread restart...");
                StackService.restart(world);
            }

            sender.sendMessage(stringBuilder.substring(1));
        });
    }

    @Override
    public List<String> tabComplete(WildStackerPlugin plugin, CommandSender sender, String[] args) {
        return null;
    }
}
