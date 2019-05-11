package com.bgsoftware.wildstacker.nms;

import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.listeners.events.EntityBreedEvent;
import com.bgsoftware.wildstacker.objects.WStackedEntity;
import com.google.common.base.Predicate;
import net.minecraft.server.v1_9_R2.Entity;
import net.minecraft.server.v1_9_R2.EntityAnimal;
import net.minecraft.server.v1_9_R2.EntityHuman;
import net.minecraft.server.v1_9_R2.EntityInsentient;
import net.minecraft.server.v1_9_R2.EntityLiving;
import net.minecraft.server.v1_9_R2.EntityPlayer;
import net.minecraft.server.v1_9_R2.EnumItemSlot;
import net.minecraft.server.v1_9_R2.ItemStack;
import net.minecraft.server.v1_9_R2.NBTCompressedStreamTools;
import net.minecraft.server.v1_9_R2.NBTTagCompound;
import net.minecraft.server.v1_9_R2.PathfinderGoalBreed;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftAnimals;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.Animals;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public final class NMSAdapter_v1_9_R2 implements NMSAdapter {

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
    public void setInLove(org.bukkit.entity.Entity entity, Player breeder, boolean inLove) {
        EntityAnimal nmsEntity = (EntityAnimal) ((CraftEntity) entity).getHandle();
        EntityPlayer entityPlayer = ((CraftPlayer) breeder).getHandle();
        if(inLove)
            nmsEntity.c((EntityHuman) entityPlayer);
        else
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

                if (itemStack != null && (livingEntity.getKiller() != null || dropChance > 1) && random.nextFloat() - (float) i * 0.01F < dropChance) {
                    if (dropChance <= 1 && itemStack.e()) {
                        int maxData = Math.max(itemStack.j() - 25, 1);
                        int data = itemStack.j() - this.random.nextInt(this.random.nextInt(maxData) + 1);

                        if (data > maxData) {
                            data = maxData;
                        }

                        if (data < 1) {
                            data = 1;
                        }
                        itemStack.setData(data);
                    }
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

    @Override
    public String serialize(org.bukkit.inventory.ItemStack itemStack) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        DataOutput dataOutput = new DataOutputStream(outputStream);

        NBTTagCompound tagCompound = new NBTTagCompound();

        ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);

        nmsItem.save(tagCompound);

        try {
            NBTCompressedStreamTools.a(tagCompound, dataOutput);
        }catch(Exception ex){
            return null;
        }

        return new BigInteger(1, outputStream.toByteArray()).toString(32);
    }

    @Override
    public org.bukkit.inventory.ItemStack deserialize(String serialized) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(new BigInteger(serialized, 32).toByteArray());

        try {
            NBTTagCompound nbtTagCompoundRoot = NBTCompressedStreamTools.a(new DataInputStream(inputStream));

            ItemStack nmsItem = ItemStack.createStack(nbtTagCompoundRoot);

            return CraftItemStack.asBukkitCopy(nmsItem);
        }catch(Exception ex){
            return null;
        }

    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public org.bukkit.inventory.ItemStack setTag(org.bukkit.inventory.ItemStack itemStack, String key, Object value) {
        ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound tagCompound = nmsItem.hasTag() ? nmsItem.getTag() : new NBTTagCompound();

        if(value instanceof Boolean)
            tagCompound.setBoolean(key, (boolean) value);
        else if(value instanceof Integer)
            tagCompound.setInt(key, (int) value);
        else if(value instanceof String)
            tagCompound.setString(key, (String) value);
        else if(value instanceof Double)
            tagCompound.setDouble(key, (double) value);
        else if(value instanceof Short)
            tagCompound.setShort(key, (short) value);
        else if(value instanceof Byte)
            tagCompound.setByte(key, (byte) value);
        else if(value instanceof Float)
            tagCompound.setFloat(key, (float) value);
        else if(value instanceof Long)
            tagCompound.setLong(key, (long) value);

        nmsItem.setTag(tagCompound);

        return CraftItemStack.asBukkitCopy(nmsItem);
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
