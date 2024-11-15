package com.bgsoftware.wildstacker.nms;

import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.api.objects.StackedItem;
import com.bgsoftware.wildstacker.nms.entity.INMSEntityEquipment;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nullable;
import java.lang.reflect.Field;

public interface NMSAdapter {

    INMSEntityEquipment createEntityEquipmentWrapper(EntityEquipment bukkitEntityEquipment);

    boolean shouldArmorBeDamaged(ItemStack itemStack);

    boolean isUnbreakable(ItemStack itemStack);

    @Nullable
    Enchantment getGlowEnchant();

    @Nullable
    default Enchantment createGlowEnchantment() {
        Enchantment glowEnchant = getGlowEnchant();

        if (glowEnchant != null) {
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
        }

        return glowEnchant;
    }

    ItemStack getPlayerSkull(ItemStack bukkitItem, String texture);

    void updateEntity(LivingEntity source, LivingEntity target);

    String serialize(ItemStack itemStack);

    ItemStack deserialize(String serialized);

    ItemStack setTag(ItemStack itemStack, String key, Object value);

    <T> T getTag(ItemStack itemStack, String key, Class<T> valueType, Object def);

    default void setOminousBottleAmplifier(ItemMeta itemMeta, int amplifier) {
        // Not implemented
    }

    Object getChatMessage(String message);

    void runAtEndOfTick(Runnable code);

    void saveEntity(StackedEntity stackedEntity);

    void loadEntity(StackedEntity stackedEntity);

    void saveItem(StackedItem stackedItem);

    void loadItem(StackedItem stackedItem);

}
