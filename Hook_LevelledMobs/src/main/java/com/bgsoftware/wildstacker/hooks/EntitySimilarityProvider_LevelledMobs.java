package com.bgsoftware.wildstacker.hooks;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.enums.StackCheckResult;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

@SuppressWarnings("unused")
public final class EntitySimilarityProvider_LevelledMobs implements EntitySimilarityProvider {

    private final WildStackerPlugin plugin;
    private final NamespacedKey levelKey;

    public EntitySimilarityProvider_LevelledMobs(WildStackerPlugin plugin) {
        this.plugin = plugin;
        Plugin levelledMobs = Bukkit.getPluginManager().getPlugin("LevelledMobs");
        assert levelledMobs != null;
        this.levelKey = new NamespacedKey(levelledMobs, "level");
    }

    @Override
    public StackCheckResult areSimilar(Entity entity, Entity other) {
        return getLevelledMobLevel(entity) == getLevelledMobLevel(other) ? StackCheckResult.SUCCESS :
                StackCheckResult.LEVELLED_MOB_LEVEL;
    }

    private int getLevelledMobLevel(Entity livingEntity) {
        PersistentDataContainer dataContainer = (PersistentDataContainer) plugin.getNMSAdapter()
                .getPersistentDataContainer(livingEntity);

        return dataContainer == null ? -1 : dataContainer.getOrDefault(levelKey, PersistentDataType.INTEGER, -1);
    }

}
