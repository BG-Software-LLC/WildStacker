package com.bgsoftware.wildstacker.nms.v1_21_10.hologram;

import com.bgsoftware.wildstacker.utils.holograms.Hologram;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftArmorStand;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.util.CraftChatMessage;
import org.bukkit.event.entity.EntityRemoveEvent;
import org.jetbrains.annotations.Nullable;

public class EntityHologram extends ArmorStand implements Hologram {

    private static final AABB EMPTY_BOUND = new AABB(0D, 0D, 0D, 0D, 0D, 0D);

    private CraftEntity bukkitEntity;

    public EntityHologram(ServerLevel serverLevel, double x, double y, double z) {
        super(serverLevel, x, y, z);

        setInvisible(true);
        setSmall(true);
        setShowArms(false);
        setNoGravity(true);
        setNoBasePlate(true);
        setMarker(true);
        forceSetBoundingBox(EMPTY_BOUND);

        super.collides = false;
        super.setCustomNameVisible(true);
    }

    @Override
    public void setHologramName(String name) {
        super.setCustomName(CraftChatMessage.fromStringOrNull(name));
    }

    @Override
    public void removeHologram() {
        super.remove(RemovalReason.DISCARDED, null);
    }

    @Override
    public void inactiveTick() {
        // Disable normal ticking for this entity.

        // Workaround to force EntityTrackerEntry to send a teleport packet immediately after spawning this entity.
        if (this.onGround) {
            this.onGround = false;
        }
    }

    @Override
    public boolean repositionEntityAfterLoad() {
        return false;
    }

    public AABB getBoundingBoxForCulling() {
        return EMPTY_BOUND;
    }

    @Override
    public void setItemSlot(EquipmentSlot equipmentSlot, ItemStack itemStack, boolean silence) {
        // Prevent stand being equipped
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        // Do not save NBT.
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output, boolean includeAll) {
        // Do not save NBT.
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        // Do not load NBT.
    }

    @Override
    public InteractionResult interactAt(Player player, Vec3 hitPos, InteractionHand hand) {
        // Prevent stand being equipped
        return InteractionResult.PASS;
    }

    @Override
    public void tick() {
        // Disable normal ticking for this entity.

        // Workaround to force EntityTrackerEntry to send a teleport packet immediately after spawning this entity.
        if (this.onGround) {
            this.onGround = false;
        }
    }

    public void forceSetBoundingBox(AABB boundingBox) {
        super.setBoundingBox(boundingBox);
    }

    @Override
    public CraftEntity getBukkitEntity() {
        if (bukkitEntity == null) {
            bukkitEntity = new CraftArmorStand((CraftServer) Bukkit.getServer(), this);
        }
        return bukkitEntity;
    }

    @Override
    public void remove(RemovalReason removalReason) {
        // Prevent being killed.
    }

    @Override
    public void remove(RemovalReason reason, @Nullable EntityRemoveEvent.Cause eventCause) {
        // Prevent being killed.
    }

    @Override
    public void playSound(SoundEvent soundEvent, float volume, float pitch) {
        // Remove sounds.
    }

    @Override
    public boolean saveAsPassenger(ValueOutput output) {
        // Do not save NBT.
        return false;
    }

    @Override
    public boolean saveAsPassenger(ValueOutput output, boolean includeAll, boolean includeNonSaveable, boolean forceSerialization) {
        // Do not save NBT.
        return false;
    }

    @Override
    public void saveWithoutId(ValueOutput output) {
        // Do not save NBT.
    }

    @Override
    public void saveWithoutId(ValueOutput output, boolean includeAll, boolean includeNonSaveable, boolean forceSerialization) {
        // Do not save NBT.
    }

    @Override
    public void load(ValueInput input) {
        // Do not load NBT.
    }

    @Override
    public boolean isInvulnerableTo(ServerLevel serverLevel, DamageSource source) {
        /*
         * The field Entity.invulnerable is private.
         * It's only used while saving NBTTags, but since the entity would be killed
         * on chunk unload, we prefer to override isInvulnerable().
         */
        return true;
    }

    @Override
    public void setCustomName(Component component) {
        // Locks the custom name.
    }

    @Override
    public void setCustomNameVisible(boolean flag) {
        // Locks the custom name.
    }

}