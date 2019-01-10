package xyz.wildseries.wildstacker.loot;

import org.bukkit.entity.Ageable;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;
import xyz.wildseries.wildstacker.WildStackerPlugin;
import xyz.wildseries.wildstacker.objects.WStackedEntity;
import xyz.wildseries.wildstacker.utils.legacy.EntityTypes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@SuppressWarnings({"StaticInitializerReferencesSubClass", "WeakerAccess"})
public abstract class LootTable implements xyz.wildseries.wildstacker.api.loot.LootTable {

    private static Map<String, xyz.wildseries.wildstacker.api.loot.LootTable> lootTables = new HashMap<>();

    protected WildStackerPlugin plugin = WildStackerPlugin.getPlugin();
    protected ThreadLocalRandom random = ThreadLocalRandom.current();
    protected LivingEntity livingEntity;

    static {
        //lootTables.put("BLAZE", new LootTableBlaze());
        lootTables.put("CAVE_SPIDER", new LootTableSpider());
        lootTables.put("CHICKEN", new LootTableChicken());
        lootTables.put("COD", new LootTableCod());
        lootTables.put("COW", new LootTableCow());
        lootTables.put("CREEPER", new LootTableCreeper());
        lootTables.put("DOLPHIN", new LootTableDolphin());
        lootTables.put("DONKEY", new LootTableHorse());
        lootTables.put("DROWNED", new LootTableDrowned());
        lootTables.put("ELDER_GUARDIAN", new LootTableElderGuardian());
        lootTables.put("EMPTY", new LootTableEmpty());
        lootTables.put("ENDERMAN", new LootTableEnderman());
        lootTables.put("EVOKER", new LootTableEvoker());
        lootTables.put("GHAST", new LootTableGhast());
        lootTables.put("GUARDIAN", new LootTableGuardian());
        lootTables.put("HORSE", new LootTableHorse());
        lootTables.put("HUSK", new LootTableHusk());
        lootTables.put("IRON_GOLEM", new LootTableIronGolem());
        lootTables.put("LLAMA", new LootTableHorse());
        lootTables.put("MAGMA_CUBE", new LootTableMagmaCube());
        lootTables.put("MULE", new LootTableHorse());
        lootTables.put("MUSHROOM_COW", new LootTableCow());
        lootTables.put("PARROT", new LootTableParrot());
        lootTables.put("PHANTOM", new LootTablePhantom());
        lootTables.put("PIG", new LootTablePig());
        lootTables.put("POLAR_BEAR", new LootTablePolarBear());
        lootTables.put("PUFFERFISH", new LootTablePufferfish());
        lootTables.put("RABBIT", new LootTableRabbit());
        lootTables.put("SALMON", new LootTableSalmon());
        lootTables.put("SHEEP", new LootTableSheep());
        lootTables.put("SHULKER", new LootTableShulker());
        lootTables.put("SKELETON", new LootTableSkeleton());
        lootTables.put("SKELETON_HORSE", new LootTableSkeletonHorse());
        lootTables.put("SLIME", new LootTableSlime());
        lootTables.put("SNOWMAN", new LootTableSnowman());
        lootTables.put("SPIDER", new LootTableSpider());
        lootTables.put("SQUID", new LootTableSquid());
        lootTables.put("STRAY", new LootTableStray());
        lootTables.put("TROPICAL_FISH", new LootTableTropicalFish());
        lootTables.put("TURTLE", new LootTableTurtle());
        lootTables.put("VINDICATOR", new LootTableVindicator());
        lootTables.put("WITCH", new LootTableWitch());
        lootTables.put("WITHER_SKELETON", new LootTableWitherSkeleton());
        lootTables.put("ZOMBIE", new LootTableZombie());
        lootTables.put("ZOMBIE_HORSE", new LootTableZombieHorse());
        lootTables.put("ZOMBIE_PIGMAN", new LootTableZombiePigman());
        lootTables.put("ZOMBIE_VILLAGER", new LootTableZombie());
    }

    protected LootTable(LivingEntity livingEntity){
        this.livingEntity = livingEntity;
    }

    protected LootTable(){
        /* empty */
    }

    protected abstract int getMaximumAmount();

    protected abstract int getMinimumAmount();

    protected abstract ItemStack getLoot();

    public List<ItemStack> getDeathLoot(int lootBonusLevel){
        List<ItemStack> deathLoot = new ArrayList<>();

        if(isBaby())
            return deathLoot;

        ItemStack loot = getLoot();
        ItemStack equipment;

        if(loot != null) {
            int amount = 0;

            for (int i = 0; i < getStackAmount(); i++) {
                int lootAmount = this.random.nextInt(getMaximumAmount() - getMinimumAmount() + 1) + getMinimumAmount();

                if (lootBonusLevel > 0) {
                    lootAmount += this.random.nextInt(lootBonusLevel + 1);
                }

                if(lootAmount > 0)
                    amount += lootAmount;

                if((equipment = getEquipment(lootBonusLevel)) != null)
                    deathLoot.add(equipment);
            }

            if(amount > 0){
                loot.setAmount(amount);
                deathLoot.add(loot);
            }
        }

        return deathLoot;
    }

    protected ItemStack getEquipment(int lootBonusLevel){
        return null;
    }

    protected boolean isBurning(){
        return livingEntity.getFireTicks() > 0;
    }

    protected boolean isKilledByPlayer(){
        return livingEntity.getKiller() != null;
    }

    protected boolean isBaby(){
        if(livingEntity instanceof Zombie)
            return ((Zombie) livingEntity).isBaby();
        return livingEntity instanceof Ageable && !((Ageable) livingEntity).isAdult();
    }

    protected int getStackAmount(){
        return WStackedEntity.of(livingEntity).getStackAmount();
    }

    protected static void registerMythicLootTable(LootTable lootTable){
        if(!lootTables.containsKey("MYTHIC")) {
            lootTables.put("MYTHIC", lootTable);
            return;
        }

        throw new RuntimeException("Custom loot is already signed and you cannot set another one.");
    }

    public static void registerCustomLootTable(xyz.wildseries.wildstacker.api.loot.LootTable lootTable){
        if(!lootTables.containsKey("CUSTOM")) {
            lootTables.put("CUSTOM", lootTable);
            WildStackerPlugin.log(" - Using " + lootTable.getClass().getSimpleName() + " as Custom LootTable.");
            return;
        }

        throw new RuntimeException("Custom loot is already signed and you cannot set another one.");
    }

    protected static boolean fromMythicLootTable = false;

    public static xyz.wildseries.wildstacker.api.loot.LootTable forEntity(LivingEntity livingEntity){
        String key = "CUSTOM";

        if(lootTables.containsKey("MYTHIC")){
            if(fromMythicLootTable){
                fromMythicLootTable = false;
            }else{
                key = "MYTHIC";
            }
        }

        if(lootTables.containsKey(key)){
            try {
                xyz.wildseries.wildstacker.api.loot.LootTable lootTable = lootTables.get(key).getClass().newInstance();
                if(lootTable instanceof LootTable)
                    ((LootTable) lootTable).livingEntity = livingEntity;
                return lootTable;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return forNaturalEntity(livingEntity);
    }

    public static xyz.wildseries.wildstacker.api.loot.LootTable forNaturalEntity(LivingEntity livingEntity){
        xyz.wildseries.wildstacker.api.loot.LootTable lootTable = lootTables.get("EMPTY");
        EntityTypes entityType = EntityTypes.fromEntity(livingEntity);

        if(lootTables.containsKey(entityType.name())) {
            try {
                lootTable = lootTables.get(entityType.name()).getClass().newInstance();
                if(lootTable instanceof LootTable)
                    ((LootTable) lootTable).livingEntity = livingEntity;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return lootTable;
    }

}
