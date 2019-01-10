package xyz.wildseries.wildstacker.loot;

import fr.ullrimax.editdrops.Allitems;
import fr.ullrimax.editdrops.Inventories;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import xyz.wildseries.wildstacker.WildStackerPlugin;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class LootTableEditDrops extends LootTable {

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
        List<ItemStack> deathLoot = new ArrayList<>();

        Inventory mobInventory = getInventory(livingEntity.getType());

        if(mobInventory == null){
            WildStackerPlugin.log("Couldn't find drops for entity: " + livingEntity.getType().name() + " with EditDrops.");
            return deathLoot;
        }

        int rdm = random.nextInt(52);

        if (mobInventory.getItem(52).equals(Allitems.naturalTrue.getItemStack())){
            deathLoot.addAll(LootTable.forNaturalEntity(livingEntity).getDeathLoot(lootBonusLevel));
        }

        if(mobInventory.getItem(rdm) != null) {
            ItemStack itemStack = mobInventory.getItem(rdm).clone();
            itemStack.setAmount(itemStack.getAmount() * getStackAmount());
            deathLoot.add(itemStack);
        }

        return deathLoot;
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

    public static void register(){
        try {
            registerCustomLootTable(new LootTableEditDrops());
        }catch(RuntimeException ignored){}
    }

}
