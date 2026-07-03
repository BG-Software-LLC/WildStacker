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
import org.bukkit.entity.Slime;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class LivingLootEntityAttributes extends CustomLootEntityAttributes {

    private final Entity entity;
    private final StackedEntity stackedEntity;

    @Nullable
    public static LootEntityAttributes getSourceKiller(LootEntityAttributes lootEntityAttributes) {
        return lootEntityAttributes instanceof LivingLootEntityAttributes ?
                ((LivingLootEntityAttributes) lootEntityAttributes).getSourceKiller() : null;
    }

    public LivingLootEntityAttributes(Entity entity, EntityLootDataBuilder builder) {
        super(builder);
        this.entity = entity;
        this.stackedEntity = EntityUtils.isStackable(entity) ? WStackedEntity.of(entity) : null;
    }

    public Entity getEntity() {
        return this.entity;
    }

    @Nullable
    @Override
    public LootEntityAttributes getKiller() {
        return Optional.ofNullable(super.getKiller()).orElseGet(this::getKillerFromEntity);
    }

    @Nullable
    public LootEntityAttributes getSourceKiller() {
        LootEntityAttributes directKillerAttributes = getKiller();
        if (!(directKillerAttributes instanceof LivingLootEntityAttributes))
            return null;

        Entity directKiller = ((LivingLootEntityAttributes) directKillerAttributes).getEntity();
        Entity sourceKiller = EntityUtils.getSourceDamager(directKiller, true);

        return sourceKiller == null || sourceKiller == directKiller ? null : LootEntityAttributes.newBuilder(sourceKiller).build();
    }

    @Nullable
    private LootEntityAttributes getKillerFromEntity() {
        Entity entityKiller = this.stackedEntity == null ? null : this.stackedEntity.getFlag(EntityFlag.CACHED_KILLER);

        if (entityKiller == null)
            entityKiller = EntityUtils.getDamagerFromEvent(this.entity.getLastDamageCause(), false, true);

        return entityKiller == null ? null : LootEntityAttributes.newBuilder(entityKiller).build();
    }

    @Override
    @Nullable
    public LootEntityAttributes getVehicle() {
        return Optional.ofNullable(super.getKiller()).orElseGet(this::getVehicleFromEntity);
    }

    @Nullable
    private LootEntityAttributes getVehicleFromEntity() {
        Entity vehicleEntity = this.entity.getVehicle();
        return vehicleEntity == null ? null : LootEntityAttributes.newBuilder(vehicleEntity).build();
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
        EntityDamageEvent lastCause = this.entity.getLastDamageCause();
        return lastCause == null ? null : lastCause.getCause();
    }

    @Override
    public boolean isBurning() {
        return super.isBurning() || this.isBurningFromEntity();
    }

    private boolean isBurningFromEntity() {
        return this.entity.getFireTicks() > 0;
    }

    @Override
    public int getSlimeSize() {
        int customSlimeSize = super.getSlimeSize();
        return customSlimeSize <= 0 ? getSlimeSizeFromEntity() : customSlimeSize;
    }

    private int getSlimeSizeFromEntity() {
        Slime slime = (Slime) this.entity;
        return slime.getSize();
    }

    @Override
    public boolean isCreeperCharged() {
        return super.isCreeperCharged() || isCreeperChargedFromEntity();
    }

    private boolean isCreeperChargedFromEntity() {
        Creeper creeper = (Creeper) this.entity;
        return creeper.isPowered();
    }

    @Override
    public boolean isRaidCaptain() {
        return super.isRaidCaptain() || isRaidCaptainFromEntity();
    }

    private boolean isRaidCaptainFromEntity() {
        org.bukkit.entity.Raider raider = (org.bukkit.entity.Raider) this.entity;
        return raider.isPatrolLeader();
    }
}
