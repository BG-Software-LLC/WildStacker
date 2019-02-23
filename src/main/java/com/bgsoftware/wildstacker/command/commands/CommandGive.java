package com.bgsoftware.wildstacker.command.commands;

import com.bgsoftware.wildstacker.Locale;
import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.command.ICommand;
import com.bgsoftware.wildstacker.utils.EntityUtil;
import com.bgsoftware.wildstacker.utils.ItemUtil;
import com.bgsoftware.wildstacker.utils.legacy.Materials;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public final class CommandGive implements ICommand {

    @Override
    public String getLabel() {
        return "give";
    }

    @Override
    public String getUsage() {
        return "stacker give <player-name> <spawner/egg/barrel> <entity-type/material-type> <stack-size>";
    }

    @Override
    public String getPermission() {
        return "wildstacker.give";
    }

    @Override
    public String getDescription() {
        return "Give a stacked spawn-egg or spawner to players.";
    }

    @Override
    public int getMinArgs() {
        return 5;
    }

    @Override
    public int getMaxArgs() {
        return 5;
    }

    @Override
    public void perform(WildStackerPlugin plugin, CommandSender sender, String[] args) {
        Player target = Bukkit.getPlayer(args[1]);

        if(target == null){
            Locale.INVALID_PLAYER.send(sender, args[1]);
            return;
        }

        ItemStack itemStack;
        String typeName;

        int stackSize;

        try{
            stackSize = Integer.parseInt(args[4]);
        }catch(NumberFormatException ex){
            Locale.INVALID_NUMBER.send(sender);
            return;
        }

        if(args[2].equalsIgnoreCase("egg") ){
            EntityType entityType;

            try{
                entityType = EntityType.valueOf(args[3].toUpperCase());
                typeName = EntityUtil.getFormattedType(entityType.name());
            }catch(IllegalArgumentException ex){
                Locale.INVALID_ENTITY.send(sender, args[3]);
                return;
            }

            itemStack = new ItemStack(Materials.getSpawnEgg(entityType));
            ItemUtil.setEntityType(itemStack, entityType);
            itemStack = ItemUtil.setSpawnerItemAmount(itemStack, stackSize);
        }

        else if(args[2].equalsIgnoreCase("spawner")){
            EntityType entityType;

            try {
                entityType = EntityType.valueOf(args[3].toUpperCase());
                typeName = EntityUtil.getFormattedType(entityType.name());
            }catch(IllegalArgumentException ex){
                Locale.INVALID_ENTITY.send(sender, args[3]);
                return;
            }

            itemStack = ItemUtil.getSpawnerItem(entityType, stackSize);
            if(itemStack.getAmount() != 1)
                stackSize = 1;
        }

        else if(args[2].equalsIgnoreCase("barrel")){
            Material barrelType;

            try {
                barrelType = Material.getMaterial(args[3].toUpperCase());
                typeName = ItemUtil.getFormattedType(new ItemStack(barrelType));
            }catch(IllegalArgumentException | NullPointerException ex){
                Locale.INVALID_BARREL.send(sender, args[3]);
                return;
            }

            if(!WildStackerPlugin.getPlugin().getSettings().whitelistedBarrels.contains(barrelType.name().toUpperCase())){
                Locale.INVALID_BARREL.send(sender, args[3]);
                return;
            }

            itemStack = new ItemStack(barrelType);
            itemStack = ItemUtil.setSpawnerItemAmount(itemStack, stackSize);
        }

        else{
            Locale.INVALID_TYPE.send(sender);
            return;
        }

        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(WildStackerPlugin.getPlugin().getSettings().giveItemName
                .replace("{0}", stackSize + "")
                .replace("{1}", typeName)
                .replace("{2}", args[2].substring(0, 1).toUpperCase() + args[2].substring(1).toLowerCase())
        );
        itemStack.setItemMeta(itemMeta);

        ItemUtil.addItem(itemStack, target.getInventory(), target.getLocation());

        Locale.STACK_GIVE_PLAYER.send(sender, target.getName(), stackSize, args[2]);
        if(!target.equals(sender))
            Locale.STACK_RECEIVE.send(target, stackSize, args[2], sender.getName());
    }

    @Override
    public List<String> tabComplete(WildStackerPlugin plugin, CommandSender sender, String[] args) {
        List<String> list = new ArrayList<>();
        switch(args.length){
            case 2:
                return null;
            case 3:
                Stream.of("egg", "spawner", "barrel")
                        .filter(stack -> stack.toLowerCase().startsWith(args[2]))
                        .forEach(stack -> list.add(stack.toLowerCase()));
                break;
            case 4:
                if(args[2].equalsIgnoreCase("egg") || args[2].equalsIgnoreCase("spawner")) {
                    Arrays.stream(EntityType.values())
                            .filter(entityType -> entityType.name().toLowerCase().startsWith(args[3]) &&
                                    entityType.getEntityClass() != null && LivingEntity.class.isAssignableFrom(entityType.getEntityClass()))
                            .forEach(entityType -> list.add(entityType.name().toLowerCase()));
                }else if(args[2].equalsIgnoreCase("barrel")) {
                    plugin.getSettings().whitelistedBarrels.asStringSet().stream()
                            .filter(this::isValidMaterial)
                            .forEach(materialName -> list.add(materialName.toLowerCase()));
                }
                break;
        }

        return list;
    }

    private boolean isValidMaterial(String materialName){
        try{
            return Material.matchMaterial(materialName) != null;
        }catch(IllegalArgumentException ex){
            return false;
        }
    }

}
