package xyz.wildseries.wildstacker.api.objects;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public interface StackedEntity extends StackedObject<LivingEntity> {

    LivingEntity getLivingEntity();

    UUID getUniqueId();

    EntityType getType();

    void setHealth(double health);

    double getHealth();

    void setCustomName(String customName);

    void setCustomNameVisible(boolean visible);

    LivingEntity trySpawnerStack(StackedSpawner stackedSpawner);

    StackedEntity spawnDuplicate(int amount);

    List<ItemStack> getDrops(int lootBonusLevel);

    void ignoreDeathEvent();

    boolean isIgnoreDeathEvent();

}
