package com.bgsoftware.wildstacker.hooks;

import com.bgsoftware.wildstacker.api.enums.SpawnCause;
import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.loot.LootTable;
import com.bgsoftware.wildstacker.loot.LootTableTemp;
import com.bgsoftware.wildstacker.objects.WStackedEntity;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitAdapter;
import io.lumine.xikage.mythicmobs.drops.Drop;
import io.lumine.xikage.mythicmobs.drops.DropMetadata;
import io.lumine.xikage.mythicmobs.drops.IItemDrop;
import io.lumine.xikage.mythicmobs.drops.LootBag;
import io.lumine.xikage.mythicmobs.drops.droppables.ExperienceDrop;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class MythicMobsHook {

    public static LivingEntity tryDuplicate(LivingEntity livingEntity){
        if(WStackedEntity.of(livingEntity).getSpawnCause() == SpawnCause.MYTHIC_MOBS){
            ActiveMob activeMob = MythicMobs.inst().getMobManager().getMythicMobInstance(livingEntity);
            ActiveMob duplicate = MythicMobs.inst().getMobManager().spawnMob(activeMob.getType().getInternalName(), livingEntity.getLocation());
            return duplicate.getLivingEntity();
        }

        return null;
    }

    public static boolean isEnabled(){
        return Bukkit.getPluginManager().isPluginEnabled("MythicMobs");
    }

    public static LootTable getLootTable(){
        return new LootTableTemp(){

            @Override
            public List<ItemStack> getDrops(StackedEntity stackedEntity, int lootBonusLevel, int stackAmount) {
                List<ItemStack> drops = new ArrayList<>();
                for(int i = 0; i < stackAmount; i++)
                    drops.addAll(getMythicDrops(stackedEntity.getLivingEntity()));
                return drops;
            }

            @Override
            public int getExp(StackedEntity stackedEntity, int stackAmount) {
                double totalExp = 0;
                for(int i = 0; i < stackAmount; i++)
                    totalExp += getMythicExp(stackedEntity.getLivingEntity());
                return (int) totalExp;
            }

        };
    }

    private static List<ItemStack> getMythicDrops(LivingEntity livingEntity){
        List<ItemStack> drops = new ArrayList<>();

        ActiveMob activeMob = MythicMobs.inst().getMobManager().getMythicMobInstance(livingEntity);
        AbstractEntity aKiller = BukkitAdapter.adapt(livingEntity.getKiller());
        DropMetadata metadata = new DropMetadata(activeMob, aKiller);
        LootBag lootBag = activeMob.getType().getDropTable().generate(metadata);
        lootBag.getDrops().stream().filter(drop -> drop instanceof IItemDrop).forEach(drop ->
                drops.add(BukkitAdapter.adapt(((IItemDrop) drop).getDrop(metadata)).clone()));

        return drops;
    }

    private static double getMythicExp(LivingEntity livingEntity){
        double totalExp = 0;

        ActiveMob activeMob = MythicMobs.inst().getMobManager().getMythicMobInstance(livingEntity);
        AbstractEntity aKiller = BukkitAdapter.adapt(livingEntity.getKiller());
        DropMetadata metadata = new DropMetadata(activeMob, aKiller);
        LootBag lootBag = activeMob.getType().getDropTable().generate(metadata);
        for(Drop drop : lootBag.getDrops()){
            if(drop instanceof ExperienceDrop)
                totalExp += drop.getAmount();
        }

        return totalExp;
    }

}
