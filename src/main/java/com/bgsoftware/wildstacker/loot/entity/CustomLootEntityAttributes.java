package com.bgsoftware.wildstacker.loot.entity;

import com.bgsoftware.wildstacker.api.enums.SpawnCause;
import com.bgsoftware.wildstacker.api.loot.LootEntityAttributes;
import com.bgsoftware.wildstacker.api.upgrades.SpawnerUpgrade;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.Nullable;

public class CustomLootEntityAttributes implements LootEntityAttributes {

    private final EntityType entityType;
    @Nullable
    private final LootEntityAttributes killerEntityData;
    private final boolean ignoreEntityKiller;
    @Nullable
    private final SpawnerUpgrade upgrade;
    private final boolean ignoreUpgrade;
    @Nullable
    private final SpawnCause spawnCause;
    private final boolean ignoreSpawnCause;
    @Nullable
    private final EntityDamageEvent.DamageCause deathCause;
    private final boolean ignoreDeathCause;
    private final boolean burning;

    public CustomLootEntityAttributes(EntityLootDataBuilder builder) {
        this.entityType = builder.entityType;
        this.killerEntityData = builder.killerEntityData;
        this.ignoreEntityKiller = builder.ignoreEntityKiller;
        this.upgrade = builder.upgrade;
        this.ignoreUpgrade = builder.ignoreUpgrade;
        this.spawnCause = builder.spawnCause;
        this.ignoreSpawnCause = builder.ignoreSpawnCause;
        this.deathCause = builder.deathCause;
        this.ignoreDeathCause = builder.ignoreDeathCause;
        this.burning = builder.burning;
    }

    @Override
    public EntityType getEntityType() {
        return this.entityType;
    }

    @Nullable
    @Override
    public LootEntityAttributes getKiller() {
        return this.killerEntityData;
    }

    @Override
    public boolean isIgnoreEntityKiller() {
        return this.ignoreEntityKiller;
    }

    @Nullable
    @Override
    public SpawnerUpgrade getUpgrade() {
        return this.upgrade;
    }

    @Override
    public boolean isIgnoreUpgrade() {
        return this.ignoreUpgrade;
    }

    @Nullable
    @Override
    public SpawnCause getSpawnCause() {
        return this.spawnCause;
    }

    @Override
    public boolean isIgnoreSpawnCause() {
        return this.ignoreSpawnCause;
    }

    @Nullable
    @Override
    public EntityDamageEvent.DamageCause getDeathCause() {
        return this.deathCause;
    }

    @Override
    public boolean isIgnoreDeathCause() {
        return this.ignoreDeathCause;
    }

    @Override
    public boolean isBurning() {
        return this.burning;
    }

}
