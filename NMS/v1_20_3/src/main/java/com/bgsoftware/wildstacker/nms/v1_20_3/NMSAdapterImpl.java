package com.bgsoftware.wildstacker.nms.v1_20_3;

import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.wildstacker.nms.v1_20_3.algorithms.PaperGlowEnchantment;
import com.bgsoftware.wildstacker.nms.v1_20_3.algorithms.SpigotGlowEnchantment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.craftbukkit.v1_20_R3.CraftRegistry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Map;
import java.util.UUID;

public class NMSAdapterImpl extends com.bgsoftware.wildstacker.nms.v1_20_3.AbstractNMSAdapter {

    private static final ReflectField<Map<NamespacedKey, Enchantment>> REGISTRY_CACHE =
            new ReflectField<>(CraftRegistry.class, Map.class, "cache");

    private static final Enchantment GLOW_ENCHANT = initializeGlowEnchantment();

    @Override
    protected void setTextureForItem(ItemStack itemStack, String texture) {
        CompoundTag compoundTag = itemStack.getOrCreateTag();

        CompoundTag skullOwner = compoundTag.contains("SkullOwner") ?
                compoundTag.getCompound("SkullOwner") : new CompoundTag();

        skullOwner.putString("Id", new UUID(texture.hashCode(), texture.hashCode()).toString());

        CompoundTag properties = new CompoundTag();
        ListTag textures = new ListTag();
        CompoundTag signature = new CompoundTag();

        signature.putString("Value", texture);
        textures.add(signature);

        properties.put("textures", textures);

        skullOwner.put("Properties", properties);

        compoundTag.put("SkullOwner", skullOwner);
    }

    @Override
    protected CompoundTag addAdditionalSaveData(LivingEntity livingEntity) {
        CompoundTag compoundTag = new CompoundTag();
        livingEntity.addAdditionalSaveData(compoundTag);
        return compoundTag;
    }

    @Override
    protected void readAdditionalSaveData(LivingEntity livingEntity, CompoundTag compoundTag) {
        livingEntity.readAdditionalSaveData(compoundTag);
    }

    @Override
    protected boolean getIsBaby(CompoundTag compoundTag) {
        return compoundTag.contains("IsBaby") && compoundTag.getBoolean("IsBaby");
    }

    @Override
    protected CompoundTag saveItemStack(ItemStack itemStack) {
        return itemStack.save(new CompoundTag());
    }

    @Override
    protected ItemStack parseItemStack(CompoundTag compoundTag) {
        return ItemStack.of(compoundTag);
    }

    @Override
    protected void setTagInternal(ItemStack itemStack, String key, Object value) {
        CompoundTag compoundTag = itemStack.getOrCreateTag();

        if (value instanceof Boolean)
            compoundTag.putBoolean(key, (boolean) value);
        else if (value instanceof Integer)
            compoundTag.putInt(key, (int) value);
        else if (value instanceof String)
            compoundTag.putString(key, (String) value);
        else if (value instanceof Double)
            compoundTag.putDouble(key, (double) value);
        else if (value instanceof Short)
            compoundTag.putShort(key, (short) value);
        else if (value instanceof Byte)
            compoundTag.putByte(key, (byte) value);
        else if (value instanceof Float)
            compoundTag.putFloat(key, (float) value);
        else if (value instanceof Long)
            compoundTag.putLong(key, (long) value);
    }

    @Override
    protected <T> T getTagInternal(ItemStack itemStack, String key, Class<T> valueType, Object def) {
        CompoundTag compoundTag = itemStack.getTag();

        if (compoundTag == null || !compoundTag.contains(key))
            return valueType.cast(def);
        else if (valueType.equals(Boolean.class))
            return valueType.cast(compoundTag.getBoolean(key));
        else if (valueType.equals(Integer.class))
            return valueType.cast(compoundTag.getInt(key));
        else if (valueType.equals(String.class))
            return valueType.cast(compoundTag.getString(key));
        else if (valueType.equals(Double.class))
            return valueType.cast(compoundTag.getDouble(key));
        else if (valueType.equals(Short.class))
            return valueType.cast(compoundTag.getShort(key));
        else if (valueType.equals(Byte.class))
            return valueType.cast(compoundTag.getByte(key));
        else if (valueType.equals(Float.class))
            return valueType.cast(compoundTag.getFloat(key));
        else if (valueType.equals(Long.class))
            return valueType.cast(compoundTag.getLong(key));

        throw new IllegalArgumentException("Cannot find nbt class type: " + valueType);
    }

    @Override
    public void makeItemGlow(ItemMeta itemMeta) {
        itemMeta.addEnchant(GLOW_ENCHANT, 1, true);
    }

    @Override
    public void setOminousBottleAmplifier(ItemMeta itemMeta, int amplifier) {
        // Do nothing
    }

    private static Enchantment initializeGlowEnchantment() {
        Enchantment glowEnchant;

        try {
            glowEnchant = new PaperGlowEnchantment("wildstacker_glowing_enchant");
        } catch (Throwable error) {
            glowEnchant = new SpigotGlowEnchantment("wildstacker_glowing_enchant");
        }

        Map<NamespacedKey, Enchantment> registryCache = REGISTRY_CACHE.get(Registry.ENCHANTMENT);

        registryCache.put(glowEnchant.getKey(), glowEnchant);

        return glowEnchant;
    }

}
