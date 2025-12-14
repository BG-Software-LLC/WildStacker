package com.bgsoftware.wildstacker.nms.v1_21_10;

import com.bgsoftware.common.reflection.ReflectMethod;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.OminousBottleMeta;
import org.slf4j.Logger;

import java.util.Optional;

public class NMSAdapterImpl extends com.bgsoftware.wildstacker.nms.v1_21_10.AbstractNMSAdapter {

    private static final ReflectMethod<Void> ENTITY_ADD_ADDITIONAL_SAVE_DATA = new ReflectMethod<>(
            Entity.class, "a", ValueOutput.class);
    private static final ReflectMethod<Void> ENTITY_READ_ADDITIONAL_SAVE_DATA = new ReflectMethod<>(
            Entity.class, "a", ValueInput.class);

    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    protected void setTextureForItem(ItemStack itemStack, String texture) {
        Multimap<String, Property> properties = HashMultimap.create();
        properties.put("textures", new Property("textures", texture));

        ResolvableProfile.Partial partialProfile = new ResolvableProfile.Partial(
                Optional.empty(), Optional.empty(), new PropertyMap(properties));
        ResolvableProfile resolvableProfile = new ResolvableProfile.Static(Either.right(partialProfile), PlayerSkin.Patch.EMPTY);

        itemStack.set(DataComponents.PROFILE, resolvableProfile);
    }

    @Override
    protected CompoundTag addAdditionalSaveData(LivingEntity livingEntity) {
        try (ProblemReporter.ScopedCollector scopedCollector = new ProblemReporter.ScopedCollector(livingEntity.problemPath(), LOGGER)) {
            TagValueOutput tagValueOutput = TagValueOutput.createWithContext(scopedCollector, livingEntity.registryAccess());
            ENTITY_ADD_ADDITIONAL_SAVE_DATA.invoke(livingEntity, tagValueOutput);
            return tagValueOutput.buildResult();
        }
    }

    @Override
    protected void readAdditionalSaveData(LivingEntity livingEntity, CompoundTag compoundTag) {
        try (ProblemReporter.ScopedCollector scopedCollector = new ProblemReporter.ScopedCollector(livingEntity.problemPath(), LOGGER)) {
            ValueInput valueInput = TagValueInput.create(scopedCollector, livingEntity.registryAccess(), compoundTag);
            ENTITY_READ_ADDITIONAL_SAVE_DATA.invoke(livingEntity, valueInput);
        }
    }

    @Override
    protected boolean getIsBaby(CompoundTag compoundTag) {
        return compoundTag.getBooleanOr("IsBaby", false);
    }

    @Override
    protected CompoundTag saveItemStack(ItemStack itemStack) {
        RegistryOps<Tag> context = MinecraftServer.getServer().registryAccess().createSerializationContext(NbtOps.INSTANCE);
        return (CompoundTag) ItemStack.CODEC.encodeStart(context, itemStack).getOrThrow();
    }

    @Override
    protected ItemStack parseItemStack(CompoundTag compoundTag) {
        RegistryOps<Tag> context = MinecraftServer.getServer().registryAccess().createSerializationContext(NbtOps.INSTANCE);
        return ItemStack.CODEC.parse(context, compoundTag)
                .resultOrPartial((itemId) -> LOGGER.error("Tried to load invalid item: '{}'", itemId))
                .orElseThrow();
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
            return valueType.cast(compoundTag.getBooleanOr(key, false));
        else if (valueType.equals(Integer.class))
            return valueType.cast(compoundTag.getIntOr(key, 0));
        else if (valueType.equals(String.class))
            return valueType.cast(compoundTag.getStringOr(key, ""));
        else if (valueType.equals(Double.class))
            return valueType.cast(compoundTag.getDoubleOr(key, 0D));
        else if (valueType.equals(Short.class))
            return valueType.cast(compoundTag.getShortOr(key, (short) 0));
        else if (valueType.equals(Byte.class))
            return valueType.cast(compoundTag.getByteOr(key, (byte) 0));
        else if (valueType.equals(Float.class))
            return valueType.cast(compoundTag.getFloatOr(key, 0f));
        else if (valueType.equals(Long.class))
            return valueType.cast(compoundTag.getLongOr(key, 0L));

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

}
