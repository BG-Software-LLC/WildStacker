package xyz.wildseries.wildstacker.loot;

import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitAdapter;
import io.lumine.xikage.mythicmobs.drops.Drop;
import io.lumine.xikage.mythicmobs.drops.DropMetadata;
import io.lumine.xikage.mythicmobs.drops.IItemDrop;
import io.lumine.xikage.mythicmobs.drops.LootBag;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import org.bukkit.inventory.ItemStack;
import xyz.wildseries.wildstacker.hooks.MythicMobsHook;

import java.util.ArrayList;
import java.util.List;

public class LootTableMythicMobs extends LootTable {

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

        if (MythicMobsHook.isMythicMob(livingEntity)) {
            ActiveMob activeMob = MythicMobs.inst().getAPIHelper().getMythicMobInstance(livingEntity);
            DropMetadata metadata = new DropMetadata(activeMob, BukkitAdapter.adapt(livingEntity.getKiller()));
            LootBag lootBag = activeMob.getType().getDropTable().generate(metadata);

            for(Drop drop : lootBag.getDrops()){
                if(drop instanceof IItemDrop){
                    deathLoot.add(BukkitAdapter.adapt(((IItemDrop)drop).getDrop(metadata)));
                }
            }
        }

        else{
            fromMythicLootTable = true;
            deathLoot.addAll(LootTable.forEntity(livingEntity).getDeathLoot(lootBonusLevel));
        }

        return deathLoot;
    }

    public static void register(){
        try{
            registerMythicLootTable(new LootTableMythicMobs());
        }catch(RuntimeException ignored){}
    }

}
