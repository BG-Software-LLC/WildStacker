package com.bgsoftware.wildstacker.utils.items;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import org.bukkit.enchantments.Enchantment;

import java.lang.reflect.Field;

public final class GlowEnchantment {

    private static WildStackerPlugin plugin = WildStackerPlugin.getPlugin();
    private static Enchantment glowEnchant;

    public static void registerEnchantment(){
        glowEnchant = plugin.getNMSAdapter().getGlowEnchant();

        try{
            Field field = Enchantment.class.getDeclaredField("acceptingNew");
            field.setAccessible(true);
            field.set(null, true);
            field.setAccessible(false);
        }catch(Exception ignored){}

        try{
            Enchantment.registerEnchantment(glowEnchant);
        }catch(Exception ignored){}
    }

    public static Enchantment getGlowEnchant() {
        return glowEnchant;
    }
}
