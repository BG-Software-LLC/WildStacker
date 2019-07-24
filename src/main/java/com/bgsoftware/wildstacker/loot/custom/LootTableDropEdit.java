package com.bgsoftware.wildstacker.loot.custom;

import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.loot.LootTable;
import de.Linus122.DropEdit.DropContainer;
import de.Linus122.DropEdit.Main;
import de.Linus122.EntityInfo.EntityKeyInfo;
import de.Linus122.EntityInfo.KeyGetter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public final class LootTableDropEdit extends LootTableCustom {

    private Main main = JavaPlugin.getPlugin(Main.class);

    private Field itemDropsField, commandDropsField;

    public LootTableDropEdit(){
        try{
            itemDropsField = DropContainer.class.getDeclaredField("itemDrops");
            itemDropsField.setAccessible(true);
            commandDropsField = DropContainer.class.getDeclaredField("commandDrops");
            commandDropsField.setAccessible(true);
        }catch(Exception ignored){ }
    }

    @Override
    public List<ItemStack> getDrops(LootTable lootTable, StackedEntity stackedEntity, int lootBonusLevel, int stackAmount) {
        List<ItemStack> drops = new ArrayList<>();

        EntityKeyInfo info = (EntityKeyInfo)Main.data.getKeyInfo(KeyGetter.getKey(stackedEntity.getType()));

        if(info != null) {
            if (info.isVanillaDropsEnabled()) {
                drops.addAll(lootTable.getDrops(stackedEntity, lootBonusLevel, stackAmount));
            }

            Player killer = stackedEntity.getLivingEntity().getKiller();

            for (int i = 0; i < stackAmount; i++) {
                DropContainer dropContainer = main.getDrops(KeyGetter.getKey(stackedEntity.getType()), stackedEntity.getLivingEntity());
                if (dropContainer != null) {
                    getDrops(dropContainer).stream()
                            .filter(itemStack -> itemStack != null && itemStack.getType() != Material.AIR)
                            .forEach(itemStack -> {
                                itemStack.setAmount(itemStack.getAmount() * (killer == null ? 1 : Main.getDropMultiplier(killer)));
                                drops.add(itemStack);
                            });
                    if(killer != null)
                        getCommands(dropContainer).forEach(cmd -> Main.processCommandTag(cmd, killer));
                }
            }
        }

        return drops;
    }

    private List<ItemStack> getDrops(DropContainer dropContainer){
        try{
            //noinspection unchecked
            return (List<ItemStack>) itemDropsField.get(dropContainer);
        }catch(Exception ex){
            ex.printStackTrace();
            return new ArrayList<>();
        }
    }

    private List<String> getCommands(DropContainer dropContainer){
        try{
            //noinspection unchecked
            return (List<String>) commandDropsField.get(dropContainer);
        }catch(Exception ex){
            ex.printStackTrace();
            return new ArrayList<>();
        }
    }

}