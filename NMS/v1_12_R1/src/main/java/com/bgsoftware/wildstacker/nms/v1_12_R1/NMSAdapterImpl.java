package com.bgsoftware.wildstacker.nms.v1_12_R1;

import com.bgsoftware.wildstacker.api.enums.SpawnCause;
import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.api.objects.StackedItem;
import com.bgsoftware.wildstacker.nms.NMSAdapter;
import com.bgsoftware.wildstacker.nms.entity.INMSEntityEquipment;
import com.bgsoftware.wildstacker.nms.entity.NMSEntityEquipmentImpl;
import com.bgsoftware.wildstacker.objects.WStackedEntity;
import net.minecraft.server.v1_12_R1.EntityItem;
import net.minecraft.server.v1_12_R1.EntityLiving;
import net.minecraft.server.v1_12_R1.ItemStack;
import net.minecraft.server.v1_12_R1.NBTCompressedStreamTools;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import net.minecraft.server.v1_12_R1.NBTTagList;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_12_R1.CraftServer;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftItem;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
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
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.util.UUID;

public final class NMSAdapterImpl implements NMSAdapter {

    private static final Enchantment GLOW_ENCHANT = initializeGlowEnchantment();

    private static final String[] ENTITY_NBT_TAGS_TO_REMOVE = new String[]{
            "SaddleItem", "Saddle", "ArmorItem", "ArmorItems", "HandItems", "Leash",
            "Leashed", "Items", "ChestedHorse", "DecorItem",
    };

    @Override
    public INMSEntityEquipment createEntityEquipmentWrapper(EntityEquipment bukkitEntityEquipment) {
        return new NMSEntityEquipmentImpl(bukkitEntityEquipment);
    }

    @Override
    public boolean shouldArmorBeDamaged(org.bukkit.inventory.ItemStack itemStack) {
        ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
        return nmsItem != null && nmsItem.f();
    }

    @Override
    public boolean isUnbreakable(org.bukkit.inventory.ItemStack itemStack) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        return itemMeta != null && itemMeta.isUnbreakable();
    }

    @Override
    public void makeItemGlow(ItemMeta itemMeta) {
        itemMeta.addEnchant(GLOW_ENCHANT, 1, true);
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

        nbtTagCompound.setFloat("Health", source.getMaxHealth());

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

        try {
            NBTCompressedStreamTools.a(tagCompound, dataOutput);
        } catch (Exception ex) {
            return null;
        }

        return new BigInteger(1, outputStream.toByteArray()).toString(32);
    }

    @Override
    public org.bukkit.inventory.ItemStack deserialize(String serialized) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(new BigInteger(serialized, 32).toByteArray());

        try {
            NBTTagCompound nbtTagCompoundRoot = NBTCompressedStreamTools.a(new DataInputStream(inputStream));

            ItemStack nmsItem = new ItemStack(nbtTagCompoundRoot);

            return CraftItemStack.asBukkitCopy(nmsItem);
        } catch (Exception ex) {
            return null;
        }

    }

    @Override
    @SuppressWarnings("ConstantConditions")
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

        if (tagCompound != null) {
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
        }

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
        EntityLiving entityLiving = ((CraftLivingEntity) stackedEntity.getLivingEntity()).getHandle();

        // Removing old tags
        entityLiving.getScoreboardTags().removeIf(tag -> tag.startsWith("ws:"));

        entityLiving.addScoreboardTag("ws:stack-amount=" + stackedEntity.getStackAmount());
        entityLiving.addScoreboardTag("ws:stack-cause=" + stackedEntity.getSpawnCause().name());
        if (stackedEntity.hasNameTag())
            entityLiving.addScoreboardTag("ws:name-tag=true");
        int upgradeId = ((WStackedEntity) stackedEntity).getUpgradeId();
        if (upgradeId != 0)
            entityLiving.addScoreboardTag("ws:upgrade=" + upgradeId);
    }

    @Override
    public void loadEntity(StackedEntity stackedEntity) {
        EntityLiving entityLiving = ((CraftLivingEntity) stackedEntity.getLivingEntity()).getHandle();
        for (String scoreboardTag : entityLiving.getScoreboardTags()) {
            if (scoreboardTag.startsWith("ws:")) {
                String[] tagSections = scoreboardTag.split("=");
                if (tagSections.length == 2) {
                    try {
                        String key = tagSections[0], value = tagSections[1];
                        if (key.equals("ws:stack-amount")) {
                            stackedEntity.setStackAmount(Integer.parseInt(value), false);
                        } else if (key.equals("ws:stack-cause")) {
                            stackedEntity.setSpawnCause(SpawnCause.valueOf(value));
                        } else if (key.equals("ws:name-tag")) {
                            ((WStackedEntity) stackedEntity).setNameTag();
                        } else if (key.equals("ws:upgrade")) {
                            ((WStackedEntity) stackedEntity).setUpgradeId(Integer.parseInt(value));
                        }
                    } catch (Exception ignored) {
                    }
                }
            }
        }
    }

    @Override
    public void saveItem(StackedItem stackedItem) {
        EntityItem entityItem = (EntityItem) ((CraftItem) stackedItem.getItem()).getHandle();

        // Removing old tags
        entityItem.getScoreboardTags().removeIf(tag -> tag.startsWith("ws:"));

        entityItem.addScoreboardTag("ws:stack-amount=" + stackedItem.getStackAmount());
    }

    @Override
    public void loadItem(StackedItem stackedItem) {
        EntityItem entityItem = (EntityItem) ((CraftItem) stackedItem.getItem()).getHandle();
        for (String scoreboardTag : entityItem.getScoreboardTags()) {
            if (scoreboardTag.startsWith("ws:")) {
                String[] tagSections = scoreboardTag.split("=");
                if (tagSections.length == 2) {
                    try {
                        String key = tagSections[0], value = tagSections[1];
                        if (key.equals("ws:stack-amount")) {
                            stackedItem.setStackAmount(Integer.parseInt(value), false);
                        }
                    } catch (Exception ignored) {
                    }
                }
            }
        }
    }

    private static Enchantment initializeGlowEnchantment() {
        int enchantId = 100;
        while (Enchantment.getById(enchantId) != null)
            ++enchantId;

        Enchantment glowEnchant = new Enchantment(enchantId) {
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
            public boolean isTreasure() {
                return false;
            }

            @Override
            public boolean isCursed() {
                return false;
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

        try {
            Field field = Enchantment.class.getDeclaredField("acceptingNew");
            field.setAccessible(true);
            field.set(null, true);
            field.setAccessible(false);
        } catch (Exception ignored) {
        }

        try {
            Enchantment.registerEnchantment(glowEnchant);
        } catch (Exception ignored) {
        }

        return glowEnchant;
    }

}
