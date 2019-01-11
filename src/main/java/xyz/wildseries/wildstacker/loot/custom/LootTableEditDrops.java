package xyz.wildseries.wildstacker.loot.custom;

import fr.ullrimax.editdrops.Allitems;
import fr.ullrimax.editdrops.Inventories;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import xyz.wildseries.wildstacker.WildStackerPlugin;
import xyz.wildseries.wildstacker.api.objects.StackedEntity;
import xyz.wildseries.wildstacker.loot.LootTable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class LootTableEditDrops extends LootTableCustom {

    private ThreadLocalRandom random = ThreadLocalRandom.current();

    @Override
    public List<ItemStack> getDrops(LootTable lootTable, StackedEntity stackedEntity, int lootBonusLevel) {
        List<ItemStack> drops = new ArrayList<>();

        Inventory mobInventory = getInventory(stackedEntity.getType());

        if(mobInventory == null){
            WildStackerPlugin.log("Couldn't find drops for entity: " + stackedEntity.getType().name() + " with EditDrops.");
            return drops;
        }

        int rdm = random.nextInt(52);

        if (mobInventory.getItem(52).equals(Allitems.naturalTrue.getItemStack())){
            drops.addAll(lootTable.getDrops(stackedEntity, lootBonusLevel));
        }

        if(mobInventory.getItem(rdm) != null) {
            ItemStack itemStack = mobInventory.getItem(rdm).clone();
            itemStack.setAmount(itemStack.getAmount() * stackedEntity.getStackAmount());
            drops.add(itemStack);
        }

        return drops;
    }

    private Inventory getInventory(EntityType entityType){
        String fieldName;
        switch (entityType.name()){
            case "IRON_GOLEM":
                fieldName = "golem";
                break;
            case "MUSHROOM_COW":
                fieldName = "mooshroom";
                break;
            case "WITHER_SKELETON":
                fieldName = "wskeleton";
                break;
            case "PIG_ZOMBIE":
                fieldName = "zombiepigman";
                break;
            default:
                fieldName = entityType.name().toLowerCase().replace("_", "");
                break;
        }

        try{
            Field inventoryField = Inventories.class.getDeclaredField(fieldName);
            return (Inventory) inventoryField.get(null);
        }catch(Exception ex){
            return null;
        }
    }

}
