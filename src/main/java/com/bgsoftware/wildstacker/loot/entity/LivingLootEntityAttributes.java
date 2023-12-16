package com.bgsoftware.wildstacker.loot.entity;

import com.bgsoftware.wildstacker.api.enums.EntityFlag;
import com.bgsoftware.wildstacker.api.enums.SpawnCause;
import com.bgsoftware.wildstacker.api.loot.LootEntityAttributes;
import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.api.upgrades.SpawnerUpgrade;
import com.bgsoftware.wildstacker.objects.WStackedEntity;
import com.bgsoftware.wildstacker.utils.entity.EntityUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Slime;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class LivingLootEntityAttributes extends CustomLootEntityAttributes {

    private final StackedEntity stackedEntity;

    public LivingLootEntityAttributes(StackedEntity stackedEntity, EntityLootDataBuilder builder) {
        super(builder);
        this.stackedEntity = stackedEntity;
    }

    public LivingEntity getLivingEntity() {
        return this.stackedEntity.getLivingEntity();
    }

    @Nullable
    @Override
    public LootEntityAttributes getKiller() {
        return Optional.ofNullable(super.getKiller()).orElseGet(this::getKillerFromEntity);
    }

    private LootEntityAttributes getKillerFromEntity() {
        Entity entityKiller = this.stackedEntity.getFlag(EntityFlag.CACHED_KILLER);
        if (entityKiller == null)
            entityKiller = EntityUtils.getDamagerFromEvent(this.stackedEntity.getLivingEntity().getLastDamageCause(), false);
        return !EntityUtils.isStackable(entityKiller) ? null :
                LootEntityAttributes.newBuilder(WStackedEntity.of(entityKiller)).build();
    }

    @Override
    @Nullable
    public SpawnerUpgrade getUpgrade() {
        return Optional.ofNullable(super.getUpgrade()).orElseGet(this::getUpgradeFromEntity);
    }

    private SpawnerUpgrade getUpgradeFromEntity() {
        return this.stackedEntity.getUpgrade();
    }

    @Nullable
    @Override
    public SpawnCause getSpawnCause() {
        return Optional.ofNullable(super.getSpawnCause()).orElseGet(this::getSpawnCauseFromEntity);
    }

    private SpawnCause getSpawnCauseFromEntity() {
        return this.stackedEntity.getSpawnCause();
    }


    @Nullable
    @Override
    public EntityDamageEvent.DamageCause getDeathCause() {
        return Optional.ofNullable(super.getDeathCause()).orElseGet(this::getDeathCauseFromEntity);
    }

    private EntityDamageEvent.DamageCause getDeathCauseFromEntity() {
        EntityDamageEvent lastCause = this.stackedEntity.getLivingEntity().getLastDamageCause();
        return lastCause == null ? null : lastCause.getCause();
    }

    @Override
    public boolean isBurning() {
        return super.isBurning() || this.isBurningFromEntity();
    }

    private boolean isBurningFromEntity() {
        return this.stackedEntity.getLivingEntity().getFireTicks() > 0;
    }

    @Override
    public int getSlimeSize() {
        int customSlimeSize = super.getSlimeSize();
        return customSlimeSize <= 0 ? getSlimeSizeFromEntity() : customSlimeSize;
    }

    private int getSlimeSizeFromEntity() {
        Slime slime = (Slime) this.stackedEntity.getLivingEntity();
        return slime.getSize();
    }

}
