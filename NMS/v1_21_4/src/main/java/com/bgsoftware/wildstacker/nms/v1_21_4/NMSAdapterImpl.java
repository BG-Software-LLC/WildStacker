package com.bgsoftware.wildstacker.nms.v1_21_4;

import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ResolvableProfile;
import org.bukkit.ExplosionResult;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.OminousBottleMeta;

import java.util.Optional;

public class NMSAdapterImpl extends com.bgsoftware.wildstacker.nms.v1_21_4.AbstractNMSAdapter {

    @Override
    protected void setTextureForItem(ItemStack itemStack, String texture) {
        PropertyMap propertyMap = new PropertyMap();
        propertyMap.put("textures", new Property("textures", texture));

        ResolvableProfile resolvableProfile = new ResolvableProfile(Optional.empty(), Optional.empty(), propertyMap);

        itemStack.set(DataComponents.PROFILE, resolvableProfile);
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
        return (CompoundTag) itemStack.save(MinecraftServer.getServer().registryAccess());
    }

    @Override
    protected ItemStack parseItemStack(CompoundTag compoundTag) {
        return ItemStack.parse(MinecraftServer.getServer().registryAccess(), compoundTag).orElseThrow();
    }

    @Override
    protected void setTagInternal(ItemStack itemStack, String key, Object value) {
        CustomData customData = itemStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        customData = customData.update(compoundTag -> {
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
        });

        itemStack.set(DataComponents.CUSTOM_DATA, customData);
    }

    @Override
    protected <T> T getTagInternal(ItemStack itemStack, String key, Class<T> valueType, Object def) {
        CustomData customData = itemStack.get(DataComponents.CUSTOM_DATA);
        CompoundTag compoundTag = customData == null ? null : customData.getUnsafe();

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
        itemMeta.setEnchantmentGlintOverride(true);
    }

    @Override
    public void setOminousBottleAmplifier(ItemMeta itemMeta, int amplifier) {
        if (!(itemMeta instanceof OminousBottleMeta ominousBottleMeta))
            return;

        ominousBottleMeta.setAmplifier(amplifier);
    }

    @Override
    public boolean isSoftExplosion(EntityExplodeEvent event) {
        return event.getExplosionResult() == ExplosionResult.TRIGGER_BLOCK;
    }

}
