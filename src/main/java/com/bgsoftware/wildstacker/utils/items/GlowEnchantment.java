package com.bgsoftware.wildstacker.utils.items;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import org.bukkit.enchantments.Enchantment;

import javax.annotation.Nullable;

public final class GlowEnchantment {

    private static Enchantment glowEnchant;

    public static void registerEnchantment(WildStackerPlugin plugin) {
        glowEnchant = plugin.getNMSAdapter().createGlowEnchantment();
    }

    @Nullable
    public static Enchantment getGlowEnchant() {
        return glowEnchant;
    }
}
