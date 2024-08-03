package com.bgsoftware.wildstacker.nms.v1_21;

import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.enums.SpawnCause;
import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.api.objects.StackedItem;
import com.bgsoftware.wildstacker.listeners.ServerTickListener;
import com.bgsoftware.wildstacker.nms.NMSAdapter;
import com.bgsoftware.wildstacker.nms.entity.INMSEntityEquipment;
import com.bgsoftware.wildstacker.nms.v1_20_3.entity.NMSEntityEquipmentImpl;
import com.bgsoftware.wildstacker.nms.v1_21.enchantment.GlowEnchantment;
import com.bgsoftware.wildstacker.objects.WStackedEntity;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ResolvableProfile;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.craftbukkit.CraftRegistry;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.math.BigInteger;
import java.util.Map;
import java.util.Optional;

public final class NMSAdapterImpl implements NMSAdapter {

    private static final String[] ENTITY_NBT_TAGS_TO_REMOVE = new String[]{
            "SaddleItem", "Saddle", "ArmorItem", "ArmorItems", "HandItems",
            "Items", "ChestedHorse", "DecorItem", "Leash", "leash"
    };

    private static final ReflectField<Map<NamespacedKey, Enchantment>> REGISTRY_CACHE =
            new ReflectField<>(CraftRegistry.class, Map.class, "cache");

    private static final WildStackerPlugin plugin = WildStackerPlugin.getPlugin();

    private static final NamespacedKey STACK_AMOUNT = new NamespacedKey(plugin, "stackAmount");
    private static final NamespacedKey SPAWN_CAUSE = new NamespacedKey(plugin, "spawnCause");
    private static final NamespacedKey NAME_TAG = new NamespacedKey(plugin, "nameTag");
    private static final NamespacedKey UPGRADE = new NamespacedKey(plugin, "upgrade");

    @Override
    public INMSEntityEquipment createEntityEquipmentWrapper(EntityEquipment bukkitEntityEquipment) {
        return new NMSEntityEquipmentImpl(bukkitEntityEquipment);
    }

    @Override
    public boolean shouldArmorBeDamaged(org.bukkit.inventory.ItemStack bukkitItem) {
        return bukkitItem != null && CraftItemStack.asNMSCopy(bukkitItem).isDamageableItem();
    }

    @Override
    public boolean isUnbreakable(org.bukkit.inventory.ItemStack itemStack) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        return itemMeta != null && itemMeta.isUnbreakable();
    }

    @Override
    public Enchantment getGlowEnchant() {
        return new GlowEnchantment();
    }

    @Override
    public Enchantment createGlowEnchantment() {
        Enchantment enchantment = getGlowEnchant();

        Map<NamespacedKey, Enchantment> registryCache = REGISTRY_CACHE.get(Registry.ENCHANTMENT);

        registryCache.put(enchantment.getKey(), enchantment);

        return enchantment;
    }

    @Override
    public org.bukkit.inventory.ItemStack getPlayerSkull(org.bukkit.inventory.ItemStack bukkitItem, String texture) {
        ItemStack itemStack = CraftItemStack.asNMSCopy(bukkitItem);

        PropertyMap propertyMap = new PropertyMap();
        propertyMap.put("textures", new Property("textures", texture));

        ResolvableProfile resolvableProfile = new ResolvableProfile(Optional.empty(), Optional.empty(), propertyMap);

        itemStack.set(DataComponents.PROFILE, resolvableProfile);

        return CraftItemStack.asBukkitCopy(itemStack);
    }

    @Override
    public void updateEntity(org.bukkit.entity.LivingEntity sourceBukkit, org.bukkit.entity.LivingEntity targetBukkit) {
        LivingEntity source = ((CraftLivingEntity) sourceBukkit).getHandle();
        LivingEntity target = ((CraftLivingEntity) targetBukkit).getHandle();

        CompoundTag compoundTag = new CompoundTag();
        source.addAdditionalSaveData(compoundTag);

        compoundTag.putFloat("Health", source.getMaxHealth());

        if (targetBukkit instanceof Zombie) {
            //noinspection deprecation
            ((Zombie) targetBukkit).setBaby(compoundTag.contains("IsBaby") && compoundTag.getBoolean("IsBaby"));
        }

        for (String key : ENTITY_NBT_TAGS_TO_REMOVE)
            compoundTag.remove(key);

        target.readAdditionalSaveData(compoundTag);
    }

    @Override
    public String serialize(org.bukkit.inventory.ItemStack bukkitItem) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        DataOutput dataOutput = new DataOutputStream(outputStream);

        ItemStack itemStack = CraftItemStack.asNMSCopy(bukkitItem);
        CompoundTag compoundTag = (CompoundTag) itemStack.save(MinecraftServer.getServer().registryAccess());

        try {
            NbtIo.write(compoundTag, dataOutput);
        } catch (Exception ex) {
            return null;
        }

        return new BigInteger(1, outputStream.toByteArray()).toString(32);
    }

    @Override
    public org.bukkit.inventory.ItemStack deserialize(String serialized) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(new BigInteger(serialized, 32).toByteArray());

        try {
            CompoundTag compoundTag = NbtIo.read(new DataInputStream(inputStream));
            ItemStack itemStack = ItemStack.parse(MinecraftServer.getServer().registryAccess(), compoundTag).orElseThrow();
            return CraftItemStack.asBukkitCopy(itemStack);
        } catch (Exception ex) {
            return null;
        }
    }

    @Override
    public org.bukkit.inventory.ItemStack setTag(org.bukkit.inventory.ItemStack bukkitItem, String key, Object value) {
        ItemStack itemStack = CraftItemStack.asNMSCopy(bukkitItem);

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

        return CraftItemStack.asBukkitCopy(itemStack);
    }

    @Override
    public <T> T getTag(org.bukkit.inventory.ItemStack bukkitItem, String key, Class<T> valueType, Object def) {
        ItemStack itemStack = CraftItemStack.asNMSCopy(bukkitItem);

        if (itemStack == null || itemStack.isEmpty())
            return valueType.cast(def);

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
    public Object getChatMessage(String message) {
        return Component.nullToEmpty(message);
    }

    @Override
    public void runAtEndOfTick(Runnable code) {
        ServerTickListener.addTickEndTask(code);
    }

    @Override
    public void saveEntity(StackedEntity stackedEntity) {
        org.bukkit.entity.LivingEntity livingEntity = stackedEntity.getLivingEntity();
        PersistentDataContainer dataContainer = livingEntity.getPersistentDataContainer();

        dataContainer.set(STACK_AMOUNT, PersistentDataType.INTEGER, stackedEntity.getStackAmount());
        dataContainer.set(SPAWN_CAUSE, PersistentDataType.STRING, stackedEntity.getSpawnCause().name());

        if (stackedEntity.hasNameTag())
            dataContainer.set(NAME_TAG, PersistentDataType.BYTE, (byte) 1);

        int upgradeId = ((WStackedEntity) stackedEntity).getUpgradeId();
        if (upgradeId != 0)
            dataContainer.set(UPGRADE, PersistentDataType.INTEGER, upgradeId);
    }

    @Override
    public void loadEntity(StackedEntity stackedEntity) {
        org.bukkit.entity.LivingEntity livingEntity = stackedEntity.getLivingEntity();
        PersistentDataContainer dataContainer = livingEntity.getPersistentDataContainer();

        if (dataContainer.has(STACK_AMOUNT, PersistentDataType.INTEGER)) {
            try {
                Integer stackAmount = dataContainer.get(STACK_AMOUNT, PersistentDataType.INTEGER);
                stackedEntity.setStackAmount(stackAmount, false);

                String spawnCause = dataContainer.get(SPAWN_CAUSE, PersistentDataType.STRING);
                if (spawnCause != null)
                    stackedEntity.setSpawnCause(SpawnCause.valueOf(spawnCause));

                if (dataContainer.has(NAME_TAG, PersistentDataType.BYTE))
                    ((WStackedEntity) stackedEntity).setNameTag();

                Integer upgradeId = dataContainer.get(UPGRADE, PersistentDataType.INTEGER);
                if (upgradeId != null && upgradeId > 0)
                    ((WStackedEntity) stackedEntity).setUpgradeId(upgradeId);
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public void saveItem(StackedItem stackedItem) {
        org.bukkit.entity.Item item = stackedItem.getItem();
        PersistentDataContainer dataContainer = item.getPersistentDataContainer();
        dataContainer.set(STACK_AMOUNT, PersistentDataType.INTEGER, stackedItem.getStackAmount());
    }

    @Override
    public void loadItem(StackedItem stackedItem) {
        org.bukkit.entity.Item item = stackedItem.getItem();
        PersistentDataContainer dataContainer = item.getPersistentDataContainer();
        if (dataContainer.has(STACK_AMOUNT, PersistentDataType.INTEGER)) {
            Integer stackAmount = dataContainer.get(STACK_AMOUNT, PersistentDataType.INTEGER);
            stackedItem.setStackAmount(stackAmount, false);
        }
    }

}
