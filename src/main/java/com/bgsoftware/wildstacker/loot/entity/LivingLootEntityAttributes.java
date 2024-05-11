package com.bgsoftware.wildstacker.loot.entity;

import com.bgsoftware.wildstacker.api.enums.EntityFlag;
import com.bgsoftware.wildstacker.api.enums.SpawnCause;
import com.bgsoftware.wildstacker.api.loot.LootEntityAttributes;
import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.api.upgrades.SpawnerUpgrade;
import com.bgsoftware.wildstacker.objects.WStackedEntity;
import com.bgsoftware.wildstacker.utils.entity.EntityUtils;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Slime;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class LivingLootEntityAttributes extends CustomLootEntityAttributes {

    private final LivingEntity livingEntity;
    private final StackedEntity stackedEntity;

    public LivingLootEntityAttributes(LivingEntity livingEntity, EntityLootDataBuilder builder) {
        super(builder);
        this.livingEntity = livingEntity;
        this.stackedEntity = EntityUtils.isStackable(livingEntity) ? WStackedEntity.of(livingEntity) : null;
    }

    public LivingEntity getLivingEntity() {
        return this.livingEntity;
    }

    @Nullable
    @Override
    public LootEntityAttributes getKiller() {
        return Optional.ofNullable(super.getKiller()).orElseGet(this::getKillerFromEntity);
    }

    private LootEntityAttributes getKillerFromEntity() {
        Entity entityKiller = this.stackedEntity == null ? null : this.stackedEntity.getFlag(EntityFlag.CACHED_KILLER);

        if (entityKiller == null)
            entityKiller = EntityUtils.getDamagerFromEvent(this.livingEntity.getLastDamageCause(), false);

        return !(entityKiller instanceof LivingEntity) ? null :
                LootEntityAttributes.newBuilder((LivingEntity) entityKiller).build();
    }

    @Override
    @Nullable
    public SpawnerUpgrade getUpgrade() {
        return Optional.ofNullable(super.getUpgrade()).orElseGet(this::getUpgradeFromEntity);
    }

    @Nullable
    private SpawnerUpgrade getUpgradeFromEntity() {
        return this.stackedEntity == null ? null : this.stackedEntity.getUpgrade();
    }

    @Nullable
    @Override
    public SpawnCause getSpawnCause() {
        return Optional.ofNullable(super.getSpawnCause()).orElseGet(this::getSpawnCauseFromEntity);
    }

    @Nullable
    private SpawnCause getSpawnCauseFromEntity() {
        return this.stackedEntity == null ? null : this.stackedEntity.getSpawnCause();
    }


    @Nullable
    @Override
    public EntityDamageEvent.DamageCause getDeathCause() {
        return Optional.ofNullable(super.getDeathCause()).orElseGet(this::getDeathCauseFromEntity);
    }

    @Nullable
    private EntityDamageEvent.DamageCause getDeathCauseFromEntity() {
        EntityDamageEvent lastCause = this.livingEntity.getLastDamageCause();
        return lastCause == null ? null : lastCause.getCause();
    }

    @Override
    public boolean isBurning() {
        return super.isBurning() || this.isBurningFromEntity();
    }

    private boolean isBurningFromEntity() {
        return this.livingEntity.getFireTicks() > 0;
    }

    @Override
    public int getSlimeSize() {
        int customSlimeSize = super.getSlimeSize();
        return customSlimeSize <= 0 ? getSlimeSizeFromEntity() : customSlimeSize;
    }

    private int getSlimeSizeFromEntity() {
        Slime slime = (Slime) this.livingEntity;
        return slime.getSize();
    }

    @Override
    public boolean isCreeperCharged() {
        return super.isCreeperCharged() || isCreeperChargedFromEntity();
    }

    private boolean isCreeperChargedFromEntity() {
        Creeper creeper = (Creeper) this.livingEntity;
        return creeper.isPowered();
    }

}
