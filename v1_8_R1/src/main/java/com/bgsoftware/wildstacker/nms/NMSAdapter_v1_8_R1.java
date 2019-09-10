package com.bgsoftware.wildstacker.nms;

import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.objects.WStackedEntity;
import com.bgsoftware.wildstacker.utils.reflection.Fields;
import com.bgsoftware.wildstacker.utils.reflection.Methods;
import net.minecraft.server.v1_8_R1.BlockPosition;
import net.minecraft.server.v1_8_R1.Entity;
import net.minecraft.server.v1_8_R1.EntityAnimal;
import net.minecraft.server.v1_8_R1.EntityInsentient;
import net.minecraft.server.v1_8_R1.EntityItem;
import net.minecraft.server.v1_8_R1.EntityLiving;
import net.minecraft.server.v1_8_R1.EntityPlayer;
import net.minecraft.server.v1_8_R1.EnumParticle;
import net.minecraft.server.v1_8_R1.ItemStack;
import net.minecraft.server.v1_8_R1.NBTCompressedStreamTools;
import net.minecraft.server.v1_8_R1.NBTTagCompound;
import net.minecraft.server.v1_8_R1.NBTTagInt;
import net.minecraft.server.v1_8_R1.NBTTagShort;
import net.minecraft.server.v1_8_R1.PacketPlayOutCollect;
import net.minecraft.server.v1_8_R1.WorldServer;
import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.v1_8_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_8_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftChicken;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftItem;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R1.inventory.CraftItemStack;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("unused")
public final class NMSAdapter_v1_8_R1 implements NMSAdapter {

    private ThreadLocalRandom random = ThreadLocalRandom.current();

    @Override
    public Object getNBTTagCompound(LivingEntity livingEntity) {
        EntityLiving entityLiving = ((CraftLivingEntity) livingEntity).getHandle();
        NBTTagCompound nbtTagCompound = new NBTTagCompound();
        entityLiving.b(nbtTagCompound);
        StackedEntity stackedEntity = WStackedEntity.of(livingEntity);
        nbtTagCompound.setString("SpawnReason", stackedEntity.getSpawnCause().toSpawnReason().name());
        return nbtTagCompound;
    }

    @Override
    public void setNBTTagCompound(LivingEntity livingEntity, Object _nbtTagCompound) {
        if(!(_nbtTagCompound instanceof NBTTagCompound))
            return;

        EntityLiving entityLiving = ((CraftLivingEntity) livingEntity).getHandle();
        NBTTagCompound nbtTagCompound = (NBTTagCompound) _nbtTagCompound;

        nbtTagCompound.setFloat("HealF", 20);
        nbtTagCompound.setFloat("Health", 20);
        nbtTagCompound.remove("SaddleItem");
        nbtTagCompound.remove("ArmorItem");
        nbtTagCompound.remove("Equipment");
        nbtTagCompound.remove("DropChances");
        if(livingEntity instanceof Zombie)
            ((Zombie) livingEntity).setBaby(nbtTagCompound.hasKey("IsBaby") && nbtTagCompound.getBoolean("IsBaby"));

        entityLiving.a(nbtTagCompound);
    }

    @Override
    public boolean isInLove(org.bukkit.entity.Entity entity) {
        EntityAnimal nmsEntity = (EntityAnimal) ((CraftEntity) entity).getHandle();
        return nmsEntity.cp();
    }

    @Override
    public void setInLove(org.bukkit.entity.Entity entity, Player breeder, boolean inLove) {
        EntityAnimal nmsEntity = (EntityAnimal) ((CraftEntity) entity).getHandle();
        EntityPlayer entityPlayer = ((CraftPlayer) breeder).getHandle();
        if(inLove)
            nmsEntity.c(entityPlayer);
        else
            nmsEntity.cq();
    }

    @Override
    public List<org.bukkit.inventory.ItemStack> getEquipment(LivingEntity livingEntity) {
        List<org.bukkit.inventory.ItemStack> equipment = new ArrayList<>();
        EntityInsentient entityLiving = (EntityInsentient) ((CraftLivingEntity) livingEntity).getHandle();

        for(int i = 0; i < entityLiving.getEquipment().length; i++){
            try {
                ItemStack itemStack = entityLiving.getEquipment(i);
                double dropChance = entityLiving.dropChances[i];

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
    @SuppressWarnings("all")
    public List<org.bukkit.entity.Entity> getNearbyEntities(org.bukkit.entity.Entity bukkitEntity, int xRange, int yRange, int zRange, Predicate<? super org.bukkit.entity.Entity> predicate) {
        Entity entityLiving = ((CraftEntity) bukkitEntity).getHandle();
        com.google.common.base.Predicate<? super Entity> wrapper = entity -> predicate.test(entity.getBukkitEntity());
        return ((List<Entity>) entityLiving.world.a(entityLiving, entityLiving.getBoundingBox().grow(xRange, yRange, zRange), wrapper))
                .stream().map(Entity::getBukkitEntity).collect(Collectors.toList());
    }

    @Override
    public String serialize(org.bukkit.inventory.ItemStack itemStack) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        DataOutput dataOutput = new DataOutputStream(outputStream);

        NBTTagCompound tagCompound = new NBTTagCompound();

        ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);

        nmsItem.save(tagCompound);

        NBTCompressedStreamTools.a(tagCompound, dataOutput);

        return new BigInteger(1, outputStream.toByteArray()).toString(32);
    }

    @Override
    public org.bukkit.inventory.ItemStack deserialize(String serialized) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(new BigInteger(serialized, 32).toByteArray());

        NBTTagCompound nbtTagCompoundRoot = NBTCompressedStreamTools.a(new DataInputStream(inputStream));

        ItemStack nmsItem = ItemStack.createStack(nbtTagCompoundRoot);

        return CraftItemStack.asBukkitCopy(nmsItem);
    }

    @Override
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

    @Override
    public <T> T getTag(org.bukkit.inventory.ItemStack itemStack, String key, Class<T> valueType) {
        ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound tagCompound = nmsItem.hasTag() ? nmsItem.getTag() : new NBTTagCompound();

        if(valueType.equals(Boolean.class))
            return valueType.cast(tagCompound.getBoolean(key));
        else if(valueType.equals(Integer.class))
            return valueType.cast(tagCompound.getInt(key));
        else if(valueType.equals(String.class))
            return valueType.cast(tagCompound.getString(key));
        else if(valueType.equals(Double.class))
            return valueType.cast(tagCompound.getDouble(key));
        else if(valueType.equals(Short.class))
            return valueType.cast(tagCompound.getShort(key));
        else if(valueType.equals(Byte.class))
            return valueType.cast(tagCompound.getByte(key));
        else if(valueType.equals(Float.class))
            return valueType.cast(tagCompound.getFloat(key));
        else if(valueType.equals(Long.class))
            return valueType.cast(tagCompound.getLong(key));

        throw new IllegalArgumentException("Cannot find nbt class type: " + valueType);
    }

    @Override
    public int getEntityExp(LivingEntity livingEntity) {
        EntityInsentient entityLiving = (EntityInsentient) ((CraftLivingEntity) livingEntity).getHandle();

        int defaultEntityExp = Fields.ENTITY_EXP.get(entityLiving, Integer.class);
        int exp = entityLiving.getExpReward();

        Fields.ENTITY_EXP.set(entityLiving, defaultEntityExp);

        return exp;
    }

    @Override
    public void updateLastDamageTime(LivingEntity livingEntity) {
        EntityLiving entityLiving = ((CraftLivingEntity) livingEntity).getHandle();
        Fields.ENTITY_LAST_DAMAGE_BY_PLAYER_TIME.set(entityLiving, 100);
    }

    @Override
    public void setHealthDirectly(LivingEntity livingEntity, double health) {
        EntityLiving entityLiving = ((CraftLivingEntity) livingEntity).getHandle();
        entityLiving.setHealth((float) health);
    }

    @Override
    public void setEntityDead(LivingEntity livingEntity, boolean dead) {
        EntityLiving entityLiving = ((CraftLivingEntity) livingEntity).getHandle();
        Fields.ENTITY_DEAD.set(entityLiving, dead);
    }

    @Override
    public int getNBTInteger(Object nbtTag) {
        return nbtTag instanceof NBTTagShort ? ((NBTTagShort) nbtTag).d() : ((NBTTagInt) nbtTag).d();
    }

    @Override
    public int getEggLayTime(Chicken chicken) {
        return ((CraftChicken) chicken).getHandle().bq;
    }

    @Override
    public Stream<BlockState> getTileEntities(org.bukkit.Chunk chunk, Predicate<BlockState> condition) {
        //noinspection unchecked
        return ((Stream<BlockPosition>) ((CraftChunk) chunk).getHandle().tileEntities.keySet().stream())
                .map(blockPosition -> chunk.getWorld().getBlockAt(blockPosition.getX(), blockPosition.getY(), blockPosition.getZ()).getState())
                .filter(condition);
    }

    @Override
    public void playPickupAnimation(LivingEntity livingEntity, Item item) {
        EntityLiving entityLiving = ((CraftLivingEntity) livingEntity).getHandle();
        EntityItem entityItem = (EntityItem) ((CraftItem) item).getHandle();
        ((WorldServer) entityLiving.world).getTracker().a(entityItem, new PacketPlayOutCollect(entityItem.getId(), entityLiving.getId()));
    }

    @Override
    public void playDeathSound(LivingEntity livingEntity) {
        EntityLiving entityLiving = ((CraftLivingEntity) livingEntity).getHandle();
        Object soundEffect = Methods.ENTITY_SOUND_DEATH.invoke(entityLiving);
        if (soundEffect != null) {
            float soundVolume = (float) Methods.ENTITY_SOUND_VOLUME.invoke(entityLiving);
            float soundPitch = (float) Methods.ENTITY_SOUND_PITCH.invoke(entityLiving);
            entityLiving.makeSound((String) soundEffect, soundVolume, soundPitch);
        }
    }

    @Override
    public void setNerfedEntity(LivingEntity livingEntity, boolean nerfed) {
        EntityLiving entityLiving = ((CraftLivingEntity) livingEntity).getHandle();
        entityLiving.fromMobSpawner = nerfed;
    }

    @Override
    public void playParticle(String particle, Location location, int count, int offsetX, int offsetY, int offsetZ, double extra) {
        WorldServer world = ((CraftWorld) location.getWorld()).getHandle();
        world.sendParticles(null, EnumParticle.valueOf(particle), true, location.getBlockX(), location.getBlockY(), location.getBlockZ(),
                count, offsetX, offsetY, offsetZ, extra);
    }

}
