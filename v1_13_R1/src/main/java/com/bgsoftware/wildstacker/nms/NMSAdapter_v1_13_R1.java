package com.bgsoftware.wildstacker.nms;

import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.listeners.events.EntityBreedEvent;
import com.bgsoftware.wildstacker.objects.WStackedEntity;
import com.google.common.base.Predicate;
import net.minecraft.server.v1_13_R1.EnchantmentManager;
import net.minecraft.server.v1_13_R1.Entity;
import net.minecraft.server.v1_13_R1.EntityAnimal;
import net.minecraft.server.v1_13_R1.EntityInsentient;
import net.minecraft.server.v1_13_R1.EntityLiving;
import net.minecraft.server.v1_13_R1.EnumItemSlot;
import net.minecraft.server.v1_13_R1.ItemStack;
import net.minecraft.server.v1_13_R1.NBTTagCompound;
import net.minecraft.server.v1_13_R1.PathfinderGoalBreed;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_13_R1.entity.CraftAnimals;
import org.bukkit.craftbukkit.v1_13_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_13_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_13_R1.inventory.CraftItemStack;
import org.bukkit.entity.Animals;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Zombie;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public final class NMSAdapter_v1_13_R1 implements NMSAdapter {

    private ThreadLocalRandom random = ThreadLocalRandom.current();

    @Override
    public Object getNBTTagCompound(LivingEntity livingEntity) {
        EntityLiving entityLiving = ((CraftLivingEntity) livingEntity).getHandle();
        NBTTagCompound nbtTagCompound = new NBTTagCompound();
        entityLiving.b(nbtTagCompound);
        StackedEntity stackedEntity = WStackedEntity.of(livingEntity);
        nbtTagCompound.setString("SpawnReason", stackedEntity.getSpawnReason().name());
        nbtTagCompound.setBoolean("Nerfed", stackedEntity.isNerfed());
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
                float dropChance = slot.a() == EnumItemSlot.Function.HAND ? entityLiving.dropChanceHand[slot.b()] : entityLiving.dropChanceArmor[slot.b()];

                if (!itemStack.isEmpty() && !EnchantmentManager.shouldNotDrop(itemStack) && (livingEntity.getKiller() != null || dropChance > 1) &&
                        random.nextFloat() - (float) i * 0.01F < dropChance) {
                    if (dropChance <= 1 && itemStack.e())
                        itemStack.setDamage(itemStack.h() - random.nextInt(1 + random.nextInt(Math.max(itemStack.h() - 3, 1))));
                    equipment.add(CraftItemStack.asBukkitCopy(itemStack));
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

    @Override
    @SuppressWarnings("all")
    public List<org.bukkit.entity.Entity> getNearbyEntities(LivingEntity livingEntity, int range, Predicate<? super org.bukkit.entity.Entity> predicate) {
        EntityLiving entityLiving = ((CraftLivingEntity) livingEntity).getHandle();
        Predicate<? super Entity> wrapper = entity -> predicate.apply(entity.getBukkitEntity());
        return ((List<Entity>) entityLiving.world.getEntities(entityLiving, entityLiving.getBoundingBox().grow(range, range, range), wrapper))
                .stream().map(Entity::getBukkitEntity).collect(Collectors.toList());
    }

    private class EventablePathfinderGoalBreed extends PathfinderGoalBreed{

        private EventablePathfinderGoalBreed(EntityAnimal entityanimal, double d0) {
            super(entityanimal, d0);
        }

        @Override
        protected void g() {
            super.g();
            EntityBreedEvent entityBreedEvent = new EntityBreedEvent((LivingEntity) animal.getBukkitEntity(), (LivingEntity) partner.getBukkitEntity());
            Bukkit.getPluginManager().callEvent(entityBreedEvent);
        }
    }
}
