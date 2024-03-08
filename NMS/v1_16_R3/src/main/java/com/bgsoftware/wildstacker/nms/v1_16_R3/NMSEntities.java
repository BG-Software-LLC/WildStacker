package com.bgsoftware.wildstacker.nms.v1_16_R3;


import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.enums.SpawnCause;
import com.bgsoftware.wildstacker.api.enums.StackCheckResult;
import com.bgsoftware.wildstacker.api.objects.StackedItem;
import com.bgsoftware.wildstacker.listeners.EntitiesListener;
import com.bgsoftware.wildstacker.nms.entity.IEntityWrapper;
import com.bgsoftware.wildstacker.objects.WStackedEntity;
import com.bgsoftware.wildstacker.objects.WStackedItem;
import com.bgsoftware.wildstacker.utils.entity.EntityUtils;
import com.bgsoftware.wildstacker.utils.threads.Executor;
import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.Chunk;
import net.minecraft.server.v1_16_R3.ChunkProviderServer;
import net.minecraft.server.v1_16_R3.CriterionTriggers;
import net.minecraft.server.v1_16_R3.DamageSource;
import net.minecraft.server.v1_16_R3.DataWatcher;
import net.minecraft.server.v1_16_R3.DataWatcherObject;
import net.minecraft.server.v1_16_R3.DynamicOpsNBT;
import net.minecraft.server.v1_16_R3.EnchantmentManager;
import net.minecraft.server.v1_16_R3.Entity;
import net.minecraft.server.v1_16_R3.EntityAnimal;
import net.minecraft.server.v1_16_R3.EntityArmorStand;
import net.minecraft.server.v1_16_R3.EntityArrow;
import net.minecraft.server.v1_16_R3.EntityExperienceOrb;
import net.minecraft.server.v1_16_R3.EntityFireballFireball;
import net.minecraft.server.v1_16_R3.EntityFox;
import net.minecraft.server.v1_16_R3.EntityHuman;
import net.minecraft.server.v1_16_R3.EntityInsentient;
import net.minecraft.server.v1_16_R3.EntityItem;
import net.minecraft.server.v1_16_R3.EntityLiving;
import net.minecraft.server.v1_16_R3.EntityPlayer;
import net.minecraft.server.v1_16_R3.EntityThrownTrident;
import net.minecraft.server.v1_16_R3.EntityTurtle;
import net.minecraft.server.v1_16_R3.EntityTypes;
import net.minecraft.server.v1_16_R3.EntityVillager;
import net.minecraft.server.v1_16_R3.EntityZombieVillager;
import net.minecraft.server.v1_16_R3.EnumHand;
import net.minecraft.server.v1_16_R3.GameRules;
import net.minecraft.server.v1_16_R3.IChatBaseComponent;
import net.minecraft.server.v1_16_R3.ItemStack;
import net.minecraft.server.v1_16_R3.ItemSword;
import net.minecraft.server.v1_16_R3.Items;
import net.minecraft.server.v1_16_R3.MobEffect;
import net.minecraft.server.v1_16_R3.MobEffects;
import net.minecraft.server.v1_16_R3.NBTBase;
import net.minecraft.server.v1_16_R3.NBTNumber;
import net.minecraft.server.v1_16_R3.NBTTagCompound;
import net.minecraft.server.v1_16_R3.NBTTagString;
import net.minecraft.server.v1_16_R3.PacketPlayOutCollect;
import net.minecraft.server.v1_16_R3.PacketPlayOutEntityMetadata;
import net.minecraft.server.v1_16_R3.PacketPlayOutSpawnEntity;
import net.minecraft.server.v1_16_R3.SoundEffect;
import net.minecraft.server.v1_16_R3.StatisticList;
import net.minecraft.server.v1_16_R3.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_16_R3.CraftChunk;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftAnimals;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftChicken;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftExperienceOrb;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftItem;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftStrider;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftTurtle;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftVehicle;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftVillager;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.MushroomCow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Strider;
import org.bukkit.entity.Turtle;
import org.bukkit.entity.Vehicle;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.entity.EntityTransformEvent;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public final class NMSEntities implements com.bgsoftware.wildstacker.nms.NMSEntities {

    private static final WildStackerPlugin plugin = WildStackerPlugin.getPlugin();

    private static final ReflectField<Integer> ENTITY_EXP = new ReflectField<>(EntityInsentient.class, int.class, "f");
    private static final ReflectField<Integer> LAST_DAMAGE_BY_PLAYER_TIME = new ReflectField<>(EntityLiving.class, int.class, "lastDamageByPlayerTime");
    private static final ReflectField<Boolean> ENTITY_LIVING_DEAD = new ReflectField<>(EntityLiving.class, boolean.class, "killed");
    private static final ReflectMethod<Boolean> ALWAYS_GIVES_EXP = new ReflectMethod<>(EntityLiving.class, "alwaysGivesExp");
    private static final ReflectMethod<Boolean> IS_DROP_EXPERIENCE = new ReflectMethod<>(EntityLiving.class, "isDropExperience");
    private static final ReflectMethod<SoundEffect> GET_SOUND_DEATH = new ReflectMethod<>(EntityLiving.class, "getSoundDeath");
    private static final ReflectMethod<Float> GET_SOUND_VOLUME = new ReflectMethod<>(EntityLiving.class, "getSoundVolume");
    private static final ReflectMethod<Float> GET_SOUND_PITCH = new ReflectMethod<>(EntityLiving.class, "dH");
    private static final ReflectMethod<BlockPosition> TURTLE_SET_HAS_EGG = new ReflectMethod<>(EntityTurtle.class, "setHasEgg", boolean.class);
    private static final ReflectMethod<BlockPosition> TURTLE_HOME_POS = new ReflectMethod<>(EntityTurtle.class, "getHomePos");
    private static final ReflectField<Boolean> FROM_MOB_SPAWNER = new ReflectField<>(net.minecraft.server.v1_16_R3.Entity.class, boolean.class, "fromMobSpawner");
    private static final ReflectMethod<Void> INSENTIENT_PICK_UP_ITEM = new ReflectMethod<>(EntityInsentient.class, "b", EntityItem.class);
    private static final ReflectMethod<DataWatcher.Item<?>> DATA_WATCHER_GET_ITEM = new ReflectMethod<>(DataWatcher.class, "b", DataWatcherObject.class);

    @Override
    public <T extends org.bukkit.entity.Entity> T createEntity(Location location, Class<T> type, SpawnCause spawnCause, Consumer<T> beforeSpawnConsumer, Consumer<T> afterSpawnConsumer) {
        CraftWorld world = (CraftWorld) location.getWorld();

        assert world != null;

        Entity nmsEntity = world.createEntity(location, type);
        org.bukkit.entity.Entity bukkitEntity = nmsEntity.getBukkitEntity();

        if (beforeSpawnConsumer != null) {
            //noinspection unchecked
            beforeSpawnConsumer.accept((T) bukkitEntity);
        }

        world.addEntity(nmsEntity, spawnCause.toSpawnReason());

        if (EntityUtils.isStackable(bukkitEntity))
            WStackedEntity.of(bukkitEntity).setSpawnCause(spawnCause);

        if (afterSpawnConsumer != null) {
            //noinspection unchecked
            afterSpawnConsumer.accept((T) bukkitEntity);
        }

        return type.cast(bukkitEntity);
    }

    @Override
    public StackedItem createItem(Location location, org.bukkit.inventory.ItemStack itemStack, SpawnCause spawnCause, Consumer<StackedItem> itemConsumer) {
        CraftWorld craftWorld = (CraftWorld) location.getWorld();
        Chunk chunk = ((CraftChunk) location.getChunk()).getHandle();

        EntityItem entityItem = new EntityItem(craftWorld.getHandle(), location.getX(), location.getY(), location.getZ(), CraftItemStack.asNMSCopy(itemStack));

        entityItem.pickupDelay = 10;

        try {
            entityItem.canMobPickup = false;
            Executor.sync(() -> entityItem.canMobPickup = true, 20L);
        } catch (Throwable ignored) {
        }

        StackedItem stackedItem = WStackedItem.ofBypass((Item) entityItem.getBukkitEntity());

        itemConsumer.accept(stackedItem);

        craftWorld.addEntity(entityItem, spawnCause.toSpawnReason());

        return stackedItem;
    }

    @Override
    public ExperienceOrb spawnExpOrb(Location location, SpawnCause spawnCause, int value) {
        return createEntity(location, ExperienceOrb.class, spawnCause, bukkitOrb -> {
            EntityExperienceOrb orb = ((CraftExperienceOrb) bukkitOrb).getHandle();
            orb.value = value;
            try {
                // Paper only
                orb.spawnReason = ExperienceOrb.SpawnReason.ENTITY_DEATH;
            } catch (Throwable ignored) {
            }
        }, null);
    }

    @Override
    public Zombie spawnZombieVillager(Villager villager) {
        EntityVillager entityVillager = ((CraftVillager) villager).getHandle();
        EntityZombieVillager entityZombieVillager = EntityTypes.ZOMBIE_VILLAGER.a(entityVillager.world);

        assert entityZombieVillager != null;
        entityZombieVillager.u(entityVillager);
        entityZombieVillager.setVillagerData(entityVillager.getVillagerData());
        entityZombieVillager.a(entityVillager.fj().a(DynamicOpsNBT.a).getValue());
        entityZombieVillager.setOffers(entityVillager.getOffers().a());
        entityZombieVillager.a(entityVillager.getExperience());
        entityZombieVillager.setBaby(entityVillager.isBaby());
        entityZombieVillager.setNoAI(entityVillager.isNoAI());

        if (entityVillager.hasCustomName()) {
            entityZombieVillager.setCustomName(entityVillager.getCustomName());
            entityZombieVillager.setCustomNameVisible(entityVillager.getCustomNameVisible());
        }

        EntityTransformEvent entityTransformEvent = new EntityTransformEvent(entityVillager.getBukkitEntity(), Collections.singletonList(entityZombieVillager.getBukkitEntity()), EntityTransformEvent.TransformReason.INFECTION);
        Bukkit.getPluginManager().callEvent(entityTransformEvent);

        if (entityTransformEvent.isCancelled())
            return null;

        entityVillager.world.addEntity(entityZombieVillager, CreatureSpawnEvent.SpawnReason.INFECTION);
        entityVillager.world.a(null, 1026,
                new BlockPosition(entityVillager.locX(), entityVillager.locY(), entityVillager.locZ()), 0);

        return (Zombie) entityZombieVillager.getBukkitEntity();
    }

    @Override
    public void setInLove(Animals entity, Player breeder, boolean inLove) {
        EntityAnimal nmsEntity = ((CraftAnimals) entity).getHandle();
        EntityPlayer entityPlayer = ((CraftPlayer) breeder).getHandle();
        if (inLove)
            nmsEntity.g(entityPlayer);
        else
            nmsEntity.resetLove();
    }

    @Override
    public boolean isInLove(Animals entity) {
        return ((EntityAnimal) ((CraftEntity) entity).getHandle()).isInLove();
    }

    @Override
    public boolean isAnimalFood(Animals animal, org.bukkit.inventory.ItemStack itemStack) {
        EntityAnimal nmsEntity = ((CraftAnimals) animal).getHandle();
        return itemStack != null && nmsEntity.k(CraftItemStack.asNMSCopy(itemStack));
    }

    @Override
    public int getEntityExp(LivingEntity livingEntity) {
        EntityLiving entityLiving = ((CraftLivingEntity) livingEntity).getHandle();

        if (!(entityLiving instanceof EntityInsentient))
            return 0;

        EntityInsentient entityInsentient = (EntityInsentient) entityLiving;

        int defaultEntityExp = ENTITY_EXP.get(entityInsentient);
        int exp = entityInsentient.getExpReward();

        ENTITY_EXP.set(entityInsentient, defaultEntityExp);

        return exp;
    }

    @Override
    public boolean canDropExp(LivingEntity livingEntity) {
        EntityLiving entityLiving = ((CraftLivingEntity) livingEntity).getHandle();
        int lastDamageByPlayerTime = LAST_DAMAGE_BY_PLAYER_TIME.get(entityLiving);
        boolean alwaysGivesExp = ALWAYS_GIVES_EXP.invoke(entityLiving);
        boolean isDropExperience = IS_DROP_EXPERIENCE.invoke(entityLiving);
        return (alwaysGivesExp || lastDamageByPlayerTime > 0) && isDropExperience &&
                entityLiving.world.getGameRules().getBoolean(GameRules.DO_MOB_LOOT);
    }

    @Override
    public void updateLastDamageTime(LivingEntity livingEntity) {
        EntityLiving entityLiving = ((CraftLivingEntity) livingEntity).getHandle();
        LAST_DAMAGE_BY_PLAYER_TIME.set(entityLiving, 100);
    }

    @Override
    public void setHealthDirectly(LivingEntity bukkitLivingEntity, double health, boolean preventUpdate) {
        EntityLiving entityLiving = ((CraftLivingEntity) bukkitLivingEntity).getHandle();
        DataWatcher dataWatcher = entityLiving.getDataWatcher();
        entityLiving.setHealth((float) health);
        if (preventUpdate) {
            DataWatcher.Item<?> item = DATA_WATCHER_GET_ITEM.invoke(dataWatcher, EntityLiving.HEALTH);
            if (item != null)
                item.a(false);
        } else {
            // We make sure health is marked dirty
            dataWatcher.markDirty(EntityLiving.HEALTH);
        }
    }

    @Override
    public int getEggLayTime(Chicken chicken) {
        return ((CraftChicken) chicken).getHandle().eggLayTime;
    }

    @Override
    public void setNerfedEntity(LivingEntity livingEntity, boolean nerfed) {
        EntityLiving entityLiving = ((CraftLivingEntity) livingEntity).getHandle();

        if (!(entityLiving instanceof EntityInsentient))
            return;

        EntityInsentient entityInsentient = (EntityInsentient) entityLiving;

        try {
            entityInsentient.aware = !nerfed;
        } catch (Throwable ignored) {
        }
        try {
            entityInsentient.spawnedViaMobSpawner = nerfed;
        } catch (Throwable ignored) {
        }

        if (FROM_MOB_SPAWNER.isValid())
            FROM_MOB_SPAWNER.set(entityInsentient, nerfed);
    }

    @Override
    public void setKiller(LivingEntity livingEntity, Player killer) {
        EntityLiving entityLiving = ((CraftLivingEntity) livingEntity).getHandle();
        entityLiving.killer = killer == null ? null : ((CraftPlayer) killer).getHandle();
    }

    @Override
    public String getEndermanCarried(Enderman enderman) {
        BlockData carriedData = enderman.getCarriedBlock();
        return carriedData == null ? "AIR" : carriedData.getMaterial() + "";
    }

    @Override
    public byte getMooshroomType(MushroomCow mushroomCow) {
        return (byte) mushroomCow.getVariant().ordinal();
    }

    @Override
    public boolean doesStriderHaveSaddle(org.bukkit.entity.Entity strider) {
        return ((CraftStrider) strider).getHandle().hasSaddle();
    }

    @Override
    public void removeStriderSaddle(org.bukkit.entity.Entity strider) {
        try {
            ((Strider) strider).setSaddle(false);
        } catch (Throwable ex) {
            ((CraftStrider) strider).getHandle().saddleStorage.setSaddle(false);
        }
    }

    @Override
    public void setTurtleEgg(org.bukkit.entity.Entity turtle) {
        try {
            ((Turtle) turtle).setHasEgg(true);
        } catch (Throwable ex) {
            EntityTurtle entityTurtle = ((CraftTurtle) turtle).getHandle();
            TURTLE_SET_HAS_EGG.invoke(entityTurtle, true);
        }
    }

    @Override
    public Location getTurtleHome(org.bukkit.entity.Entity turtle) {
        try {
            return ((Turtle) turtle).getHome();
        } catch (Throwable ex) {
            EntityTurtle entityTurtle = ((CraftTurtle) turtle).getHandle();
            BlockPosition homePosition = TURTLE_HOME_POS.invoke(entityTurtle);
            return new Location(entityTurtle.getWorld().getWorld(), homePosition.getX(),
                    homePosition.getY(), homePosition.getZ());
        }
    }

    @Override
    public boolean handleTotemOfUndying(LivingEntity livingEntity) {
        EntityLiving entityLiving = ((CraftLivingEntity) livingEntity).getHandle();

        ItemStack totemOfUndying = ItemStack.b;

        for (EnumHand enumHand : EnumHand.values()) {
            ItemStack handItem = entityLiving.b(enumHand);
            if (handItem.getItem() == Items.TOTEM_OF_UNDYING) {
                totemOfUndying = handItem;
                break;
            }
        }

        EntityResurrectEvent event = new EntityResurrectEvent(livingEntity);
        event.setCancelled(totemOfUndying.isEmpty());
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled())
            return false;

        if (!totemOfUndying.isEmpty()) {
            totemOfUndying.subtract(1);

            if (entityLiving instanceof EntityPlayer) {
                ((EntityPlayer) entityLiving).b(StatisticList.ITEM_USED.b(Items.TOTEM_OF_UNDYING));
                CriterionTriggers.B.a((EntityPlayer) entityLiving, totemOfUndying);
            }

            entityLiving.setHealth(1.0F);
            entityLiving.removeAllEffects(EntityPotionEffectEvent.Cause.TOTEM);
            entityLiving.addEffect(new MobEffect(MobEffects.REGENERATION, 900, 1), EntityPotionEffectEvent.Cause.TOTEM);
            entityLiving.addEffect(new MobEffect(MobEffects.ABSORBTION, 100, 1), EntityPotionEffectEvent.Cause.TOTEM);
            entityLiving.addEffect(new MobEffect(MobEffects.FIRE_RESISTANCE, 800, 0), EntityPotionEffectEvent.Cause.TOTEM);
            entityLiving.world.broadcastEntityEffect(entityLiving, (byte) 35);
        }

        return true;
    }

    @Override
    public void sendEntityDieEvent(LivingEntity livingEntity) {
        // Do nothing.
    }

    @Override
    public boolean callEntityBreedEvent(LivingEntity child, LivingEntity mother, LivingEntity father, @Nullable LivingEntity breeder,
                                        @Nullable org.bukkit.inventory.ItemStack bredWith, int experience) {
        EntityBreedEvent entityBreedEvent = new EntityBreedEvent(child, mother, father, breeder, bredWith, experience);
        Bukkit.getPluginManager().callEvent(entityBreedEvent);
        return !entityBreedEvent.isCancelled();
    }

    @Override
    public StackCheckResult areSimilar(com.bgsoftware.wildstacker.utils.legacy.EntityTypes entityType, LivingEntity en1, LivingEntity en2) {
        return StackCheckResult.SUCCESS;
    }

    @Override
    public boolean checkEntityAttributes(LivingEntity bukkitEntity, Map<String, Object> attributes) {
        Entity entity = ((CraftEntity) bukkitEntity).getHandle();
        NBTTagCompound entityCompound = entity.save(new NBTTagCompound());

        for (Map.Entry<String, Object> attribute : attributes.entrySet()) {
            NBTBase nbtBase = entityCompound.get(attribute.getKey());
            if (nbtBase instanceof NBTNumber) {
                if (!Objects.equals(attribute.getValue(), ((NBTNumber) nbtBase).k()))
                    return false;
            } else if (nbtBase instanceof NBTTagString) {
                if (!Objects.equals(attribute.getValue(), nbtBase.asString()))
                    return false;
            } else {
                return false;
            }
        }

        return true;
    }

    @Override
    public void awardKillScore(org.bukkit.entity.Entity bukkitDamaged,
                               org.bukkit.entity.Entity damagerEntity) {
        Entity damaged = ((CraftEntity) bukkitDamaged).getHandle();
        Entity damager = ((CraftEntity) damagerEntity).getHandle();

        DamageSource damageSource = null;

        if (damager instanceof EntityHuman) {
            damageSource = DamageSource.playerAttack((EntityHuman) damager);
        } else if (damager instanceof EntityThrownTrident) {
            EntityThrownTrident entityThrownTrident = (EntityThrownTrident) damager;
            damageSource = DamageSource.a(damager, entityThrownTrident.getShooter());
        } else if (damager instanceof EntityArrow) {
            EntityArrow entityArrow = (EntityArrow) damager;
            damageSource = DamageSource.arrow(entityArrow, entityArrow.getShooter());
        } else if (damager instanceof EntityFireballFireball) {
            EntityFireballFireball entityFireballFireball = (EntityFireballFireball) damager;
            damageSource = DamageSource.fireball(entityFireballFireball, entityFireballFireball.getShooter());
        }

        if (damageSource == null) {
            if (damager instanceof EntityLiving) {
                damageSource = DamageSource.mobAttack((EntityLiving) damager);
            } else {
                return;
            }
        }

        damager.a(damaged, 0, damageSource);
    }

    @Override
    public void awardPickupScore(Player player, Item pickItem) {
        // Do nothing.
    }

    @Override
    public void awardCrossbowShot(Player player, LivingEntity target) {
        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
        EntityLiving targetEntity = ((CraftLivingEntity) target).getHandle();
        CriterionTriggers.G.a(entityPlayer, Arrays.asList(targetEntity));
    }

    @Override
    public void playPickupAnimation(LivingEntity livingEntity, Item item) {
        EntityLiving entityLiving = ((CraftLivingEntity) livingEntity).getHandle();
        EntityItem entityItem = (EntityItem) ((CraftItem) item).getHandle();
        ChunkProviderServer chunkProvider = ((WorldServer) entityLiving.world).getChunkProvider();
        chunkProvider.broadcast(entityItem, new PacketPlayOutCollect(entityItem.getId(), entityLiving.getId(), item.getItemStack().getAmount()));
        //Makes sure the entity is still there.
        chunkProvider.broadcast(entityItem, new PacketPlayOutSpawnEntity(entityItem));
        chunkProvider.broadcast(entityItem, new PacketPlayOutEntityMetadata(entityItem.getId(), entityItem.getDataWatcher(), true));
    }

    @Override
    public void playDeathSound(LivingEntity livingEntity) {
        EntityLiving entityLiving = ((CraftLivingEntity) livingEntity).getHandle();

        SoundEffect deathSound = GET_SOUND_DEATH.invoke(entityLiving);
        float soundVolume = GET_SOUND_VOLUME.invoke(entityLiving);
        float soundPitch = GET_SOUND_PITCH.invoke(entityLiving);

        if (deathSound != null)
            entityLiving.playSound(deathSound, soundVolume, soundPitch);
    }

    @Override
    public void playSpawnEffect(LivingEntity livingEntity) {
        EntityLiving entityLiving = ((CraftLivingEntity) livingEntity).getHandle();
        if (entityLiving instanceof EntityInsentient)
            ((EntityInsentient) entityLiving).doSpawnEffect();
    }

    @Override
    public void handleItemPickup(org.bukkit.entity.LivingEntity bukkitLivingEntity, StackedItem stackedItem, int remaining) {
        EntityLiving entityLiving = ((CraftLivingEntity) bukkitLivingEntity).getHandle();
        boolean isPlayerPickup = entityLiving instanceof EntityHuman;

        if (!isPlayerPickup && !(entityLiving instanceof EntityInsentient))
            return;

        EntityItem entityItem = (EntityItem) ((CraftItem) stackedItem.getItem()).getHandle();
        if (remaining > 0)
            entityItem.getItemStack().add(remaining);

        int stackAmount = stackedItem.getStackAmount();
        int maxStackSize = entityItem.getItemStack().getMaxStackSize();
        boolean retryPickup = false;

        EntityItem pickupItem;
        if (stackAmount <= maxStackSize) {
            // If the stack size is not larger than vanilla, we can safely pickup the original item.
            pickupItem = entityItem;
        } else {
            // In case the stack size is larger than vanilla's max stack size, we want to simulate pickup
            // of a max stack size item instead. In case it's a player picking up the item, we want the item to have
            // the real count.
            ItemStack itemStack = entityItem.getItemStack().cloneItemStack();

            if (isPlayerPickup || entityLiving instanceof EntityFox) {
                if (plugin.getSettings().itemsFixStackEnabled) {
                    itemStack.setCount(maxStackSize);
                    retryPickup = true;
                } else {
                    itemStack.setCount(stackAmount);
                }
            } else {
                itemStack.setCount(maxStackSize);
            }

            pickupItem = new EntityItem(entityItem.world, entityItem.locX(), entityItem.locY(), entityItem.locZ(), itemStack);
        }

        int originalItemCount = pickupItem.getItemStack().getCount();
        int originalPickupDelay = entityItem.pickupDelay;
        boolean isDifferentPickupItem = pickupItem != entityItem;
        boolean actualItemDupe = originalItemCount != stackAmount;

        try {
            if (isDifferentPickupItem) entityItem.o(); // setNeverPickUp
            EntitiesListener.IMP.secondPickupEventCall = true;
            EntitiesListener.IMP.secondPickupEvent = null;
            if (isPlayerPickup) {
                pickupItem.pickup((EntityHuman) entityLiving);
            } else {
                INSENTIENT_PICK_UP_ITEM.invoke(entityLiving, pickupItem);
            }
        } finally {
            if (isDifferentPickupItem) entityItem.pickupDelay = originalPickupDelay;
            EntitiesListener.IMP.secondPickupEventCall = false;
            EntitiesListener.IMP.secondPickupEvent = null;
        }

        int pickupCount = originalItemCount - (pickupItem.isAlive() ? pickupItem.getItemStack().getCount() : 0);

        if (pickupCount > 0) {
            stackedItem.decreaseStackAmount(pickupCount, true);
            if (retryPickup) {
                handleItemPickup(bukkitLivingEntity, stackedItem, 0);
            } else {
                entityItem.defaultPickupDelay();
            }
        }

        if (!actualItemDupe && isDifferentPickupItem) {
            entityLiving.a(entityItem); // onItemPickup

            if (pickupCount < originalItemCount) {
                // Need to simulate item pickup
                EntityItem simulatedEntityItemPickup = pickupItem != entityItem ? pickupItem :
                        new EntityItem(entityItem.world, entityItem.locX(), entityItem.locY(), entityItem.locZ(), entityItem.getItemStack());

                ChunkProviderServer chunkProviderServer = ((WorldServer) simulatedEntityItemPickup.world).getChunkProvider();
                chunkProviderServer.broadcastIncludingSelf(entityLiving, simulatedEntityItemPickup.P());
                chunkProviderServer.broadcastIncludingSelf(entityLiving, new PacketPlayOutEntityMetadata(
                        simulatedEntityItemPickup.getId(), simulatedEntityItemPickup.getDataWatcher(), true));
                entityLiving.receive(simulatedEntityItemPickup, Math.min(pickupCount, maxStackSize));
            } else {
                entityLiving.receive(entityItem, Math.min(pickupCount, maxStackSize));
            }

            if (!pickupItem.isAlive())
                entityItem.die();
        }
    }

    @Override
    public void handleSweepingEdge(Player attacker, org.bukkit.inventory.ItemStack usedItem, LivingEntity target, double damage) {
        EntityLiving targetLiving = ((CraftLivingEntity) target).getHandle();
        EntityHuman entityHuman = ((CraftPlayer) attacker).getHandle();

        // Making sure the player used a sword.
        if (usedItem.getType() == Material.AIR || !(CraftItemStack.asNMSCopy(usedItem).getItem() instanceof ItemSword))
            return;

        float sweepDamage = 1.0F + EnchantmentManager.a(entityHuman) * (float) damage;
        List<EntityLiving> nearbyEntities = targetLiving.world.a(EntityLiving.class, targetLiving.getBoundingBox().grow(1.0D, 0.25D, 1.0D));

        for (EntityLiving nearby : nearbyEntities) {
            if (nearby != targetLiving && nearby != entityHuman && !entityHuman.r(nearby) && (!(nearby instanceof EntityArmorStand) || !((EntityArmorStand) nearby).isMarker()) && entityHuman.h(nearby) < 9.0D) {
                nearby.damageEntity(DamageSource.playerAttack(entityHuman).sweep(), sweepDamage);
            }
        }
    }

    @Override
    public void giveExp(Player player, int amount) {
        player.giveExp(amount, true);
    }

    @Override
    public void enterVehicle(org.bukkit.entity.Entity vehicle, org.bukkit.entity.Entity entity) {
        vehicle.addPassenger(entity);
    }

    @Override
    public int getPassengersCount(Vehicle vehicle) {
        return (int) ((CraftVehicle) vehicle).getHandle().passengers.stream()
                .filter(entity -> !(entity instanceof EntityPlayer)).count();
    }

    @Override
    public String getCustomName(org.bukkit.entity.Entity entity) {
        // Much more optimized way than Bukkit's method.
        IChatBaseComponent chatBaseComponent = ((CraftEntity) entity).getHandle().getCustomName();
        return chatBaseComponent == null ? "" : chatBaseComponent.getText();
    }

    @Override
    public void setCustomName(org.bukkit.entity.Entity entity, String name) {
        entity.setCustomName(name);
    }

    @Override
    public boolean isCustomNameVisible(org.bukkit.entity.Entity entity) {
        return entity.isCustomNameVisible();
    }

    @Override
    public void setCustomNameVisible(org.bukkit.entity.Entity entity, boolean visible) {
        entity.setCustomNameVisible(visible);
    }

    @Override
    public boolean isDroppedItem(Item item) {
        return ((CraftItem) item).getHandle() instanceof EntityItem;
    }

    @Override
    public IEntityWrapper wrapEntity(LivingEntity bukkitLivingEntity) {
        return new IEntityWrapper() {
            @Override
            public void setHealth(float health, boolean preventUpdate) {
                NMSEntities.this.setHealthDirectly(bukkitLivingEntity, health, preventUpdate);
            }

            @Override
            public void setRemoved(boolean removed) {
                ((CraftLivingEntity) bukkitLivingEntity).getHandle().dead = removed;
            }

            @Override
            public void setDead(boolean dead) {
                EntityLiving entity = ((CraftLivingEntity) bukkitLivingEntity).getHandle();
                ENTITY_LIVING_DEAD.set(entity, dead);
                entity.deathTicks = 0;
            }
        };
    }

}
