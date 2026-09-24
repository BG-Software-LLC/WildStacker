package com.bgsoftware.wildstacker.nms.v26_3;

import com.bgsoftware.common.reflection.ReflectMethod;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

public class NMSUtils {

    private static final ReflectMethod<ItemStack> CRAFT_ITEM_STACK_AS_CRAFT_MIRROR = new ReflectMethod<>(
            CraftItemStack.class, org.bukkit.inventory.ItemStack.class, "asCraftMirror", net.minecraft.world.item.ItemStack.class);

    private NMSUtils() {

    }

    public static org.bukkit.inventory.ItemStack asMirror(net.minecraft.world.item.ItemStack itemStack) {
        // Spigot still uses the CraftItemStack#asCraftMirror(ItemStack).
        if (CRAFT_ITEM_STACK_AS_CRAFT_MIRROR.isValid()) {
            return CRAFT_ITEM_STACK_AS_CRAFT_MIRROR.invoke(null, itemStack);
        }

        return CraftItemStack.asBukkitMirror(itemStack);
    }

}
