package xyz.wildseries.wildstacker.table;

import com.google.gson.JsonObject;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import xyz.wildseries.wildstacker.api.objects.StackedEntity;

@SuppressWarnings("WeakerAccess")
public class LootItem {

    private Material type, burnableType;
    private short data, burnableData;
    private double chance;
    private int min, max;
    private boolean fortune;

    private LootItem(Material type, Material burnableType, short data, short burnableData, int min, int max, double chance, boolean fortune){
        this.type = type;
        this.burnableType = burnableType;
        this.data = data;
        this.burnableData = burnableData;
        this.min = min;
        this.max = max;
        this.chance = chance;
        this.fortune = fortune;
    }

    public double getChance() {
        return chance;
    }

    public ItemStack getItemStack(StackedEntity stackedEntity, int lootBonusLevel){
        ItemStack itemStack = data == 0 ? new ItemStack(type) : new ItemStack(type, 1, data);
        if(LootTable.isBurning(stackedEntity))
            itemStack = burnableData == 0 ? new ItemStack(burnableType) : new ItemStack(burnableType, 1, burnableData);

        int itemAmount = 0;
        int stackAmount = stackedEntity.getStackAmount();

        for (int i = 0; i < stackAmount; i++) {
            int lootAmount = LootTable.random.nextInt(max - min + 1) + min;

            if (fortune && lootBonusLevel > 0) {
                lootAmount += LootTable.random.nextInt(lootBonusLevel + 1);
            }

            if(lootAmount > 0)
                itemAmount += lootAmount;
        }

        itemStack.setAmount(itemAmount);

        return itemStack;
    }

    public static LootItem fromJson(JsonObject jsonObject){
        Material burnableType;
        Material type = burnableType = Material.valueOf(jsonObject.get("type").getAsString());
        short burnableData;
        short data = burnableData = jsonObject.has("data") ? jsonObject.get("data").getAsShort() : 0;
        double chance = jsonObject.has("chance") ? jsonObject.get("chance").getAsDouble() : 100;
        int min = jsonObject.has("min") ? jsonObject.get("min").getAsInt() : 1;
        int max = jsonObject.has("max") ? jsonObject.get("max").getAsInt() : 1;
        boolean fortune = jsonObject.has("fortune") && jsonObject.get("fortune").getAsBoolean();

        if(jsonObject.has("burnable")){
            JsonObject burnable = jsonObject.get("burnable").getAsJsonObject();
            burnableType = Material.valueOf(burnable.get("type").getAsString());
            burnableData = burnable.has("data") ? burnable.get("data").getAsShort() : 0;
        }

        return new LootItem(type, burnableType, data, burnableData, min, max, chance, fortune);
    }

}
