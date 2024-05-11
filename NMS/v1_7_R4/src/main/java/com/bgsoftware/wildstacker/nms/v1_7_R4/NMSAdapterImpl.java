package com.bgsoftware.wildstacker.nms.v1_7_R4;

import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.api.objects.StackedItem;
import com.bgsoftware.wildstacker.nms.NMSAdapter;
import com.bgsoftware.wildstacker.nms.entity.INMSEntityEquipment;
import com.bgsoftware.wildstacker.nms.v1_7_R4.entity.NMSEntityEquipmentImpl;
import com.bgsoftware.wildstacker.nms.v1_7_R4.serializer.DataSerializerImpl;
import net.minecraft.server.v1_7_R4.EntityLiving;
import net.minecraft.server.v1_7_R4.ItemStack;
import net.minecraft.server.v1_7_R4.NBTCompressedStreamTools;
import net.minecraft.server.v1_7_R4.NBTTagCompound;
import net.minecraft.server.v1_7_R4.NBTTagList;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_7_R4.CraftServer;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_7_R4.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.math.BigInteger;
import java.util.UUID;

@SuppressWarnings({"unused"})
public final class NMSAdapterImpl implements NMSAdapter {

    private static final String[] ENTITY_NBT_TAGS_TO_REMOVE = new String[]{
            "SaddleItem", "Saddle", "ArmorItem", "ArmorItems", "HandItems", "Leash",
            "Leashed", "Items", "ChestedHorse",
    };

    @Override
    public INMSEntityEquipment createEntityEquipmentWrapper(EntityEquipment bukkitEntityEquipment) {
        return new NMSEntityEquipmentImpl(bukkitEntityEquipment);
    }

    @Override
    public boolean shouldArmorBeDamaged(org.bukkit.inventory.ItemStack itemStack) {
        ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
        return nmsItem != null && nmsItem.g();
    }

    @Override
    public boolean isUnbreakable(org.bukkit.inventory.ItemStack itemStack) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        return itemMeta != null && itemMeta.spigot().isUnbreakable();
    }

    @Override
    public Enchantment getGlowEnchant() {
        return new Enchantment(101) {
            @Override
            public String getName() {
                return "WildStackerGlow";
            }

            @Override
            public int getMaxLevel() {
                return 1;
            }

            @Override
            public int getStartLevel() {
                return 0;
            }

            @Override
            public EnchantmentTarget getItemTarget() {
                return null;
            }

            @Override
            public boolean conflictsWith(Enchantment enchantment) {
                return false;
            }

            @Override
            public boolean canEnchantItem(org.bukkit.inventory.ItemStack itemStack) {
                return true;
            }
        };
    }

    @Override
    public org.bukkit.inventory.ItemStack getPlayerSkull(org.bukkit.inventory.ItemStack bukkitItem, String texture) {
        ItemStack itemStack = CraftItemStack.asNMSCopy(bukkitItem);
        NBTTagCompound nbtTagCompound = itemStack.getTag() != null ? itemStack.getTag() : new NBTTagCompound();

        NBTTagCompound skullOwner = nbtTagCompound.hasKey("SkullOwner") ? nbtTagCompound.getCompound("SkullOwner") : new NBTTagCompound();

        skullOwner.setString("Id", new UUID(texture.hashCode(), texture.hashCode()).toString());

        NBTTagCompound properties = new NBTTagCompound();

        NBTTagList textures = new NBTTagList();
        NBTTagCompound signature = new NBTTagCompound();
        signature.setString("Value", texture);
        textures.add(signature);

        properties.set("textures", textures);

        skullOwner.set("Properties", properties);

        nbtTagCompound.set("SkullOwner", skullOwner);

        itemStack.setTag(nbtTagCompound);

        return CraftItemStack.asBukkitCopy(itemStack);
    }

    @Override
    public void updateEntity(org.bukkit.entity.LivingEntity sourceBukkit, org.bukkit.entity.LivingEntity targetBukkit) {
        EntityLiving source = ((CraftLivingEntity) sourceBukkit).getHandle();
        EntityLiving target = ((CraftLivingEntity) targetBukkit).getHandle();

        NBTTagCompound nbtTagCompound = new NBTTagCompound();
        source.b(nbtTagCompound);

        nbtTagCompound.setFloat("HealF", source.getMaxHealth());
        nbtTagCompound.setShort("Health", (short) Math.ceil(source.getMaxHealth()));

        if (targetBukkit instanceof Zombie)
            ((Zombie) targetBukkit).setBaby(nbtTagCompound.hasKey("IsBaby") && nbtTagCompound.getBoolean("IsBaby"));

        for (String key : ENTITY_NBT_TAGS_TO_REMOVE)
            nbtTagCompound.remove(key);

        target.a(nbtTagCompound);
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

        if (value instanceof Boolean)
            tagCompound.setBoolean(key, (boolean) value);
        else if (value instanceof Integer)
            tagCompound.setInt(key, (int) value);
        else if (value instanceof String)
            tagCompound.setString(key, (String) value);
        else if (value instanceof Double)
            tagCompound.setDouble(key, (double) value);
        else if (value instanceof Short)
            tagCompound.setShort(key, (short) value);
        else if (value instanceof Byte)
            tagCompound.setByte(key, (byte) value);
        else if (value instanceof Float)
            tagCompound.setFloat(key, (float) value);
        else if (value instanceof Long)
            tagCompound.setLong(key, (long) value);

        nmsItem.setTag(tagCompound);

        return CraftItemStack.asBukkitCopy(nmsItem);
    }

    @Override
    public <T> T getTag(org.bukkit.inventory.ItemStack itemStack, String key, Class<T> valueType, Object def) {
        ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);

        if (nmsItem == null)
            return valueType.cast(def);

        NBTTagCompound tagCompound = nmsItem.hasTag() ? nmsItem.getTag() : new NBTTagCompound();

        if (!tagCompound.hasKey(key))
            return valueType.cast(def);
        else if (valueType.equals(Boolean.class))
            return valueType.cast(tagCompound.getBoolean(key));
        else if (valueType.equals(Integer.class))
            return valueType.cast(tagCompound.getInt(key));
        else if (valueType.equals(String.class))
            return valueType.cast(tagCompound.getString(key));
        else if (valueType.equals(Double.class))
            return valueType.cast(tagCompound.getDouble(key));
        else if (valueType.equals(Short.class))
            return valueType.cast(tagCompound.getShort(key));
        else if (valueType.equals(Byte.class))
            return valueType.cast(tagCompound.getByte(key));
        else if (valueType.equals(Float.class))
            return valueType.cast(tagCompound.getFloat(key));
        else if (valueType.equals(Long.class))
            return valueType.cast(tagCompound.getLong(key));

        throw new IllegalArgumentException("Cannot find nbt class type: " + valueType);
    }

    @Override
    public Object getChatMessage(String message) {
        return message;
    }

    @Override
    public void runAtEndOfTick(Runnable code) {
        ((CraftServer) Bukkit.getServer()).getServer().a(code::run);
    }

    @Override
    public void saveEntity(StackedEntity stackedEntity) {
        DataSerializerImpl.getInstance().saveEntity(stackedEntity);
    }

    @Override
    public void loadEntity(StackedEntity stackedEntity) {
        DataSerializerImpl.getInstance().loadEntity(stackedEntity);
    }

    @Override
    public void saveItem(StackedItem stackedItem) {
        DataSerializerImpl.getInstance().saveItem(stackedItem);
    }

    @Override
    public void loadItem(StackedItem stackedItem) {
        DataSerializerImpl.getInstance().loadItem(stackedItem);
    }

}
