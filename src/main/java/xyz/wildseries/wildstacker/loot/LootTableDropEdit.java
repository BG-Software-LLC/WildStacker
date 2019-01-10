package xyz.wildseries.wildstacker.loot;

import de.Linus122.DropEdit.Main;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LootTableDropEdit extends LootTable {

    @Override
    protected int getMaximumAmount() {
        return 0;
    }

    @Override
    protected int getMinimumAmount() {
        return 0;
    }

    @Override
    protected ItemStack getLoot() {
        return null;
    }

    @Override
    public List<ItemStack> getDeathLoot(int lootBonusLevel) {
        List<ItemStack> deathLoot = JavaPlugin.getPlugin(Main.class).getDrops(livingEntity.getType());

        if(deathLoot == null)
            deathLoot = new ArrayList<>();

        deathLoot = deathLoot.stream().filter(itemStack -> itemStack != null && itemStack.getType() != Material.AIR)
                .collect(Collectors.toList());

        for(ItemStack itemStack : deathLoot)
            itemStack.setAmount(itemStack.getAmount() * getStackAmount());

        return deathLoot;
    }

    public static void register(){
        try {
            registerCustomLootTable(new LootTableDropEdit());
        }catch(RuntimeException ignored){}
    }

}
