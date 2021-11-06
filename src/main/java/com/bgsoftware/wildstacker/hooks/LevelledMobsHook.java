package com.bgsoftware.wildstacker.hooks;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.Objects;

public final class LevelledMobsHook {

    private static final WildStackerPlugin plugin = WildStackerPlugin.getPlugin();
    private static final Plugin levelledMobs = Bukkit.getPluginManager().getPlugin("LevelledMobs");

    private static final NamespacedKey levelKey = new NamespacedKey(levelledMobs, "level");
    private static final NamespacedKey isLevelledKey = new NamespacedKey(levelledMobs, "isLevelled");

    public static boolean isLevelledMob(LivingEntity livingEntity) {
        Object dataContainer = plugin.getNMSAdapter().getPersistentDataContainer(livingEntity);
        return dataContainer != null && ((PersistentDataContainer) dataContainer).has(isLevelledKey, PersistentDataType.STRING);
    }

    public static boolean areSimilar(Entity en1, Entity en2) {
        return getLevelledMobLevel(en1) == getLevelledMobLevel(en2);
    }

    private static int getLevelledMobLevel(Entity livingEntity) {
        Object dataContainer = plugin.getNMSAdapter().getPersistentDataContainer(livingEntity);
        return dataContainer == null || !((PersistentDataContainer) dataContainer).has(levelKey, PersistentDataType.INTEGER) ? -1 :
                Objects.requireNonNull(((PersistentDataContainer) dataContainer).get(levelKey, PersistentDataType.INTEGER));
    }

}
