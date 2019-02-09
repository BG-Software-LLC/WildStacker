package xyz.wildseries.wildstacker.loot.custom;

import de.Linus122.DropEdit.Main;
import de.Linus122.EntityInfo.EntityKeyInfo;
import de.Linus122.EntityInfo.KeyGetter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.wildseries.wildstacker.api.objects.StackedEntity;
import xyz.wildseries.wildstacker.loot.LootTable;

import java.util.ArrayList;
import java.util.List;

public class LootTableDropEdit extends LootTableCustom {

    private Main main = JavaPlugin.getPlugin(Main.class);

    @Override
    public List<ItemStack> getDrops(LootTable lootTable, StackedEntity stackedEntity, int lootBonusLevel, int stackAmount) {
        List<ItemStack> drops = new ArrayList<>();

        EntityKeyInfo info = (EntityKeyInfo)Main.data.getKeyInfo(KeyGetter.getKey(stackedEntity.getType()));

        if(info != null) {
            if (info.isVanillaDropsEnabled()) {
                drops.addAll(lootTable.getDrops(stackedEntity, lootBonusLevel, stackAmount));
            }

            for(int i = 0; i < stackAmount; i++){
                List<ItemStack> entityDrops = main.getDrops(KeyGetter.getKey(stackedEntity.getType()));
                if(entityDrops != null) {
                    entityDrops.stream()
                            .filter(itemStack -> itemStack != null && itemStack.getType() != Material.AIR)
                            .forEach(drops::add);
                }
            }
        }

        return drops;
    }

}
