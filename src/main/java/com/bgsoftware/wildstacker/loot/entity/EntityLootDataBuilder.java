package com.bgsoftware.wildstacker.loot.entity;

import com.bgsoftware.wildstacker.api.enums.SpawnCause;
import com.bgsoftware.wildstacker.api.loot.LootEntityAttributes;
import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.api.upgrades.SpawnerUpgrade;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.Nullable;

public class EntityLootDataBuilder implements LootEntityAttributes.Builder {

    @Nullable
    private final StackedEntity stackedEntity;
    public final EntityType entityType;
    @Nullable
    public LootEntityAttributes killerEntityData;
    public boolean ignoreEntityKiller;
    @Nullable
    public SpawnerUpgrade upgrade;
    public boolean ignoreUpgrade;
    @Nullable
    public SpawnCause spawnCause;
    public boolean ignoreSpawnCause;
    @Nullable
    public EntityDamageEvent.DamageCause deathCause;
    public boolean ignoreDeathCause;
    public boolean burning;
    public int slimeSize;
    public boolean ignoreSlimeSize;

    public EntityLootDataBuilder(EntityType entityType) {
        this.stackedEntity = null;
        this.entityType = entityType;
    }

    public EntityLootDataBuilder(StackedEntity stackedEntity) {
        this.stackedEntity = stackedEntity;
        this.entityType = stackedEntity.getType();
    }

    @Override
    public LootEntityAttributes.Builder setKiller(LootEntityAttributes killerEntityData) {
        this.killerEntityData = killerEntityData;
        return this;
    }

    @Override
    public LootEntityAttributes.Builder setIgnoreEntityKiller(boolean ignoreEntityKiller) {
        this.ignoreEntityKiller = ignoreEntityKiller;
        return this;
    }

    @Override
    public LootEntityAttributes.Builder setUpgrade(SpawnerUpgrade upgrade) {
        this.upgrade = upgrade;
        return this;
    }

    @Override
    public LootEntityAttributes.Builder setIgnoreUpgrade(boolean ignoreUpgrade) {
        this.ignoreUpgrade = ignoreUpgrade;
        return this;
    }

    @Override
    public LootEntityAttributes.Builder setSpawnCause(SpawnCause spawnCause) {
        this.spawnCause = spawnCause;
        return this;
    }

    @Override
    public LootEntityAttributes.Builder setIgnoreSpawnCause(boolean ignoreSpawnCause) {
        this.ignoreSpawnCause = ignoreSpawnCause;
        return this;
    }

    @Override
    public LootEntityAttributes.Builder setDeathCause(EntityDamageEvent.DamageCause deathCause) {
        this.deathCause = deathCause;
        return this;
    }

    @Override
    public LootEntityAttributes.Builder setIgnoreDeathCause(boolean ignoreDeathCause) {
        this.ignoreDeathCause = ignoreDeathCause;
        return this;
    }

    @Override
    public LootEntityAttributes.Builder setBurning(boolean burning) {
        this.burning = burning;
        return this;
    }

    @Override
    public LootEntityAttributes.Builder setSlimeSize(int slimeSize) {
        if(this.entityType != EntityType.SLIME && this.entityType != EntityType.MAGMA_CUBE)
            throw new UnsupportedOperationException();

        this.slimeSize = slimeSize;
        return this;
    }

    @Override
    public LootEntityAttributes.Builder setIgnoreSlimeSize(boolean ignoreSlimeSize) {
        if(this.entityType != EntityType.SLIME && this.entityType != EntityType.MAGMA_CUBE)
            throw new UnsupportedOperationException();

        this.ignoreSlimeSize = ignoreSlimeSize;
        return this;
    }

    @Override
    public LootEntityAttributes build() {
        if (this.stackedEntity == null) {
            return new CustomLootEntityAttributes(this);
        } else {
            return new LivingLootEntityAttributes(this.stackedEntity, this);
        }
    }

}
