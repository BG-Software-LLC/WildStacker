package com.bgsoftware.wildstacker.nms;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.listeners.events.EntityBreedEvent;
import com.bgsoftware.wildstacker.objects.WStackedEntity;
import net.minecraft.server.v1_11_R1.EnchantmentManager;
import net.minecraft.server.v1_11_R1.EntityAnimal;
import net.minecraft.server.v1_11_R1.EntityInsentient;
import net.minecraft.server.v1_11_R1.EntityLiving;
import net.minecraft.server.v1_11_R1.EnumItemSlot;
import net.minecraft.server.v1_11_R1.ItemStack;
import net.minecraft.server.v1_11_R1.NBTTagCompound;
import net.minecraft.server.v1_11_R1.PathfinderGoalBreed;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftAnimals;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_11_R1.inventory.CraftItemStack;
import org.bukkit.entity.Animals;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@SuppressWarnings("unused")
public final class NMSAdapter_v1_11_R1 implements NMSAdapter {

    private ThreadLocalRandom random = ThreadLocalRandom.current();

    @Override
    public Object getNBTTagCompound(LivingEntity livingEntity) {
        EntityLiving entityLiving = ((CraftLivingEntity) livingEntity).getHandle();
        NBTTagCompound nbtTagCompound = new NBTTagCompound();
        entityLiving.b(nbtTagCompound);
        nbtTagCompound.setString("SpawnReason", WStackedEntity.spawnReasons.getOrDefault(livingEntity.getUniqueId(),
                CreatureSpawnEvent.SpawnReason.CHUNK_GEN).name());
        nbtTagCompound.setBoolean("Nerfed", WildStackerPlugin.getPlugin().getSettings().nerfedSpawning.contains(nbtTagCompound.getString("SpawnReason")));
        return nbtTagCompound;
    }

    @Override
    public void setNBTTagCompound(LivingEntity livingEntity, Object _nbtTagCompound) {
        if(!(_nbtTagCompound instanceof NBTTagCompound))
            return;

        EntityLiving entityLiving = ((CraftLivingEntity) livingEntity).getHandle();
        NBTTagCompound nbtTagCompound = (NBTTagCompound) _nbtTagCompound;

        nbtTagCompound.setFloat("Health", 20);
        nbtTagCompound.remove("SaddleItem");
        nbtTagCompound.remove("ArmorItem");
        nbtTagCompound.remove("ArmorItems");
        nbtTagCompound.remove("HandItems");
        if(livingEntity instanceof Zombie)
            ((Zombie) livingEntity).setBaby(nbtTagCompound.hasKey("IsBaby") && nbtTagCompound.getBoolean("IsBaby"));

        entityLiving.a(nbtTagCompound);
    }

    @Override
    public boolean isInLove(org.bukkit.entity.Entity entity) {
        EntityAnimal nmsEntity = (EntityAnimal) ((CraftEntity) entity).getHandle();
        return nmsEntity.isInLove();
    }

    @Override
    public void setInLove(org.bukkit.entity.Entity entity, boolean inLove) {
        EntityAnimal nmsEntity = (EntityAnimal) ((CraftEntity) entity).getHandle();
        nmsEntity.resetLove();
    }

    @Override
    public List<org.bukkit.inventory.ItemStack> getEquipment(LivingEntity livingEntity) {
        List<org.bukkit.inventory.ItemStack> equipment = new ArrayList<>();
        EntityInsentient entityLiving = (EntityInsentient) ((CraftLivingEntity) livingEntity).getHandle();

        EnumItemSlot[] enumItemSlots = EnumItemSlot.values();

        for(int i = 0; i < enumItemSlots.length; i++){
            try {
                EnumItemSlot slot = enumItemSlots[i];
                ItemStack itemStack = entityLiving.getEquipment(slot);
                float dropChance = slot.a().ordinal() == 1 ? entityLiving.dropChanceHand[slot.b()] : slot.a().ordinal() == 2 ? entityLiving.dropChanceArmor[slot.b()] : 0;

                if (!itemStack.isEmpty() && !shouldNotDrop(itemStack) && (livingEntity.getKiller() != null || dropChance > 1) &&
                        random.nextFloat() - (float) i * 0.01F < dropChance) {
                    if (dropChance <= 1 && itemStack.f())
                        itemStack.setData(itemStack.k() - random.nextInt(1 + random.nextInt(Math.max(itemStack.k() - 3, 1))));
                    equipment.add(CraftItemStack.asBukkitCopy(itemStack));
                }

                if (dropChance >= 1) {
                    if (slot.a() == EnumItemSlot.Function.HAND)
                        entityLiving.dropChanceHand[slot.b()] = 0;
                    else if (slot.a() == EnumItemSlot.Function.ARMOR)
                        entityLiving.dropChanceArmor[slot.b()] = 0;
                }
            }catch(Exception ignored){}
        }

        return equipment;
    }

    @Override
    public void addCustomPathfinderGoalBreed(LivingEntity livingEntity) {
        if(livingEntity instanceof Animals) {
            EntityAnimal entityLiving = ((CraftAnimals) livingEntity).getHandle();
            entityLiving.goalSelector.a(2, new EventablePathfinderGoalBreed(entityLiving, 1.0D));
        }
    }

    private boolean shouldNotDrop(ItemStack itemStack){
        try{
            return EnchantmentManager.c(itemStack);
        }catch(Exception ex){
            try{
                //noinspection JavaReflectionMemberAccess
                return (Boolean) EnchantmentManager.class.getMethod("e", ItemStack.class).invoke(null, itemStack);
            }catch(Exception ex1){
                ex1.printStackTrace();
            }
        }

        return false;
    }

    private class EventablePathfinderGoalBreed extends PathfinderGoalBreed{

        private EventablePathfinderGoalBreed(EntityAnimal entityanimal, double d0) {
            super(entityanimal, d0);
        }

        @Override
        public void e() {
            super.e();
            try {
                Field bField = PathfinderGoalBreed.class.getDeclaredField("b");
                bField.setAccessible(true);
                int b = (int) bField.get(this);
                bField.setAccessible(false);

                Field animalField = PathfinderGoalBreed.class.getDeclaredField("animal");
                animalField.setAccessible(true);
                EntityAnimal animal = (EntityAnimal) animalField.get(this);
                animalField.setAccessible(false);

                Field partnerField = PathfinderGoalBreed.class.getDeclaredField("partner");
                partnerField.setAccessible(true);
                EntityAnimal partner = (EntityAnimal) partnerField.get(this);
                partnerField.setAccessible(false);

                if (b >= 60 && animal.h(partner) < 9.0D){
                    EntityBreedEvent event = new EntityBreedEvent((LivingEntity) animal.getBukkitEntity(), (LivingEntity) partner.getBukkitEntity());
                    Bukkit.getPluginManager().callEvent(event);
                }
            }catch(Exception ex){
                ex.printStackTrace();
            }
        }
    }

}
