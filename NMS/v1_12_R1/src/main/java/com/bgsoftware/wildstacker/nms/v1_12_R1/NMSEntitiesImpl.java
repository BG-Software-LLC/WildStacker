package com.bgsoftware.wildstacker.nms.v1_12_R1;


import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.enums.SpawnCause;
import com.bgsoftware.wildstacker.api.enums.StackCheckResult;
import com.bgsoftware.wildstacker.api.objects.StackedItem;
import com.bgsoftware.wildstacker.listeners.EntitiesListener;
import com.bgsoftware.wildstacker.nms.NMSEntities;
import com.bgsoftware.wildstacker.nms.entity.IEntityWrapper;
import com.bgsoftware.wildstacker.objects.WStackedEntity;
import com.bgsoftware.wildstacker.objects.WStackedItem;
import com.bgsoftware.wildstacker.utils.entity.EntityUtils;
import com.bgsoftware.wildstacker.utils.legacy.EntityTypes;
import net.minecraft.server.v1_12_R1.BlockPosition;
import net.minecraft.server.v1_12_R1.CriterionTriggers;
import net.minecraft.server.v1_12_R1.DamageSource;
import net.minecraft.server.v1_12_R1.DataWatcher;
import net.minecraft.server.v1_12_R1.DataWatcherObject;
import net.minecraft.server.v1_12_R1.EnchantmentManager;
import net.minecraft.server.v1_12_R1.Enchantments;
import net.minecraft.server.v1_12_R1.Entity;
import net.minecraft.server.v1_12_R1.EntityAnimal;
import net.minecraft.server.v1_12_R1.EntityArmorStand;
import net.minecraft.server.v1_12_R1.EntityArrow;
import net.minecraft.server.v1_12_R1.EntityExperienceOrb;
import net.minecraft.server.v1_12_R1.EntityFireball;
import net.minecraft.server.v1_12_R1.EntityHuman;
import net.minecraft.server.v1_12_R1.EntityInsentient;
import net.minecraft.server.v1_12_R1.EntityItem;
import net.minecraft.server.v1_12_R1.EntityLiving;
import net.minecraft.server.v1_12_R1.EntityPlayer;
import net.minecraft.server.v1_12_R1.EntityTracker;
import net.minecraft.server.v1_12_R1.EntityVillager;
import net.minecraft.server.v1_12_R1.EntityZombieVillager;
import net.minecraft.server.v1_12_R1.EnumHand;
import net.minecraft.server.v1_12_R1.ItemStack;
import net.minecraft.server.v1_12_R1.ItemSword;
import net.minecraft.server.v1_12_R1.Items;
import net.minecraft.server.v1_12_R1.MobEffect;
import net.minecraft.server.v1_12_R1.MobEffects;
import net.minecraft.server.v1_12_R1.NBTBase;
import net.minecraft.server.v1_12_R1.NBTTagByte;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import net.minecraft.server.v1_12_R1.NBTTagDouble;
import net.minecraft.server.v1_12_R1.NBTTagFloat;
import net.minecraft.server.v1_12_R1.NBTTagInt;
import net.minecraft.server.v1_12_R1.NBTTagLong;
import net.minecraft.server.v1_12_R1.NBTTagShort;
import net.minecraft.server.v1_12_R1.NBTTagString;
import net.minecraft.server.v1_12_R1.PacketPlayOutCollect;
import net.minecraft.server.v1_12_R1.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_12_R1.PacketPlayOutEntityMetadata;
import net.minecraft.server.v1_12_R1.PacketPlayOutSpawnEntity;
import net.minecraft.server.v1_12_R1.SoundEffect;
import net.minecraft.server.v1_12_R1.StatisticList;
import net.minecraft.server.v1_12_R1.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftAnimals;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftChicken;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftExperienceOrb;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftItem;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftVehicle;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftVillager;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.MushroomCow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.material.MaterialData;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class NMSEntitiesImpl implements NMSEntities {

    private static final WildStackerPlugin plugin = WildStackerPlugin.getPlugin();

    private static final ReflectField<Integer> ENTITY_EXP = new ReflectField<>(EntityInsentient.class, int.class, "b_");
    private static final ReflectField<Integer> LAST_DAMAGE_BY_PLAYER_TIME = new ReflectField<>(EntityLiving.class, int.class, "lastDamageByPlayerTime");
    private static final ReflectField<Boolean> ENTITY_LIVING_DEAD = new ReflectField<>(EntityLiving.class, boolean.class, "aU");
    private static final ReflectMethod<Boolean> ALWAYS_GIVES_EXP = new ReflectMethod<>(EntityLiving.class, "alwaysGivesExp");
    private static final ReflectMethod<Boolean> IS_DROP_EXPERIENCE = new ReflectMethod<>(EntityLiving.class, "isDropExperience");
    private static final ReflectMethod<SoundEffect> GET_SOUND_DEATH = new ReflectMethod<>(EntityLiving.class, "cf");
    private static final ReflectMethod<Float> GET_SOUND_VOLUME = new ReflectMethod<>(EntityLiving.class, "cq");
    private static final ReflectMethod<Float> GET_SOUND_PITCH = new ReflectMethod<>(EntityLiving.class, "cr");
    private static final ReflectMethod<Void> INSENTIENT_PICK_UP_ITEM = new ReflectMethod<>(EntityInsentient.class, "a", EntityItem.class);
    private static final ReflectMethod<DataWatcher.Item<?>> DATA_WATCHER_GET_ITEM = new ReflectMethod<>(DataWatcher.class, "c", DataWatcherObject.class);

    @Override
    public <T extends org.bukkit.entity.Entity> T createEntity(Location location, Class<T> type, SpawnCause spawnCause,
                                                               Predicate<org.bukkit.entity.Entity> beforeSpawnConsumer,
                                                               Consumer<org.bukkit.entity.Entity> afterSpawnConsumer) {
        CraftWorld world = (CraftWorld) location.getWorld();

        Entity nmsEntity = world.createEntity(location, type);
        //noinspection unchecked
        T bukkitEntity = (T) nmsEntity.getBukkitEntity();

        if (beforeSpawnConsumer != null && !beforeSpawnConsumer.test(bukkitEntity)) {
            return null;
        }

        world.addEntity(nmsEntity, spawnCause.toSpawnReason());

        if (EntityUtils.isStackable(bukkitEntity))
            WStackedEntity.of(bukkitEntity).setSpawnCause(spawnCause);

        if (afterSpawnConsumer != null) {
            afterSpawnConsumer.accept(bukkitEntity);
        }

        return type.cast(bukkitEntity);
    }

    @Override
    public StackedItem createItem(Location location, org.bukkit.inventory.ItemStack itemStack, SpawnCause spawnCause, Consumer<StackedItem> itemConsumer) {
        CraftWorld craftWorld = (CraftWorld) location.getWorld();

        EntityItem entityItem = new EntityItem(craftWorld.getHandle(), location.getX(), location.getY(), location.getZ(), CraftItemStack.asNMSCopy(itemStack));

        entityItem.pickupDelay = 10;

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
            return true;
        }, null);
    }

    @Override
    public Zombie spawnZombieVillager(Villager villager) {
        EntityVillager entityVillager = ((CraftVillager) villager).getHandle();
        EntityZombieVillager entityZombieVillager = new EntityZombieVillager(entityVillager.world);

        entityZombieVillager.u(entityVillager);
        entityZombieVillager.setProfession(entityVillager.getProfession());
        entityZombieVillager.setBaby(entityVillager.isBaby());
        entityZombieVillager.setNoAI(entityVillager.isNoAI());

        if (entityVillager.hasCustomName()) {
            entityZombieVillager.setCustomName(entityVillager.getCustomName());
            entityZombieVillager.setCustomNameVisible(entityVillager.getCustomNameVisible());
        }

        entityVillager.world.addEntity(entityZombieVillager, CreatureSpawnEvent.SpawnReason.INFECTION);
        entityVillager.world.a(null, 1026, new BlockPosition(entityVillager), 0);

        return (Zombie) entityZombieVillager.getBukkitEntity();
    }

    @Override
    public void setInLove(Animals entity, Player breeder, boolean inLove) {
        EntityAnimal nmsEntity = ((CraftAnimals) entity).getHandle();
        EntityPlayer entityPlayer = ((CraftPlayer) breeder).getHandle();
        if (inLove)
            nmsEntity.f(entityPlayer);
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
        return itemStack != null && nmsEntity.e(CraftItemStack.asNMSCopy(itemStack));
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
        return (lastDamageByPlayerTime > 0 || alwaysGivesExp) && isDropExperience &&
                entityLiving.world.getGameRules().getBoolean("doMobLoot");
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
        return ((CraftChicken) chicken).getHandle().bD;
    }

    @Override
    public void setNerfedEntity(LivingEntity livingEntity, boolean nerfed) {
        EntityLiving entityLiving = ((CraftLivingEntity) livingEntity).getHandle();
        entityLiving.fromMobSpawner = nerfed;
    }

    @Override
    public void setKiller(LivingEntity livingEntity, Player killer) {
        EntityLiving entityLiving = ((CraftLivingEntity) livingEntity).getHandle();
        entityLiving.killer = killer == null ? null : ((CraftPlayer) killer).getHandle();
    }

    @Override
    public String getEndermanCarried(Enderman enderman) {
        MaterialData materialData = enderman.getCarriedMaterial();
        //noinspection deprecation
        return materialData.getItemType() + ":" + materialData.getData();
    }

    @Override
    public byte getMooshroomType(MushroomCow mushroomCow) {
        return 0;
    }

    @Override
    public boolean doesStriderHaveSaddle(org.bukkit.entity.Entity strider) {
        throw new UnsupportedOperationException("Not supported in this version.");
    }

    @Override
    public void removeStriderSaddle(org.bukkit.entity.Entity strider) {
        throw new UnsupportedOperationException("Not supported in this version.");
    }

    @Override
    public void setTurtleEgg(org.bukkit.entity.Entity turtle) {
        throw new UnsupportedOperationException("Not supported in this version.");
    }

    @Override
    public Location getTurtleHome(org.bukkit.entity.Entity turtle) {
        throw new UnsupportedOperationException("Not supported in this version.");
    }

    @Override
    public boolean handleTotemOfUndying(LivingEntity livingEntity) {
        EntityLiving entityLiving = ((CraftLivingEntity) livingEntity).getHandle();

        ItemStack totemOfUndying = ItemStack.a;

        for (EnumHand enumHand : EnumHand.values()) {
            ItemStack handItem = entityLiving.b(enumHand);
            if (handItem.getItem() == Items.cY) {
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
                ((EntityPlayer) entityLiving).b(StatisticList.b(Items.cY));
                CriterionTriggers.A.a((EntityPlayer) entityLiving, totemOfUndying);
            }

            entityLiving.setHealth(1.0F);
            entityLiving.removeAllEffects();
            entityLiving.addEffect(new MobEffect(MobEffects.REGENERATION, 900, 1));
            entityLiving.addEffect(new MobEffect(MobEffects.ABSORBTION, 100, 1));
            entityLiving.addEffect(new MobEffect(MobEffects.FIRE_RESISTANCE, 800, 0));
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
    public StackCheckResult areSimilar(EntityTypes entityType, LivingEntity en1, LivingEntity en2) {
        return StackCheckResult.SUCCESS;
    }

    @Override
    public boolean checkEntityAttributes(LivingEntity bukkitEntity, Map<String, Object> attributes) {
        Entity entity = ((CraftEntity) bukkitEntity).getHandle();
        NBTTagCompound entityCompound = entity.save(new NBTTagCompound());

        for (Map.Entry<String, Object> attribute : attributes.entrySet()) {
            NBTBase nbtBase = entityCompound.get(attribute.getKey());
            Object attributeValue;

            if (nbtBase instanceof NBTTagByte) {
                attributeValue = ((NBTTagByte) nbtBase).g();
            } else if (nbtBase instanceof NBTTagDouble) {
                attributeValue = ((NBTTagDouble) nbtBase).asDouble();
            } else if (nbtBase instanceof NBTTagFloat) {
                attributeValue = ((NBTTagFloat) nbtBase).i();
            } else if (nbtBase instanceof NBTTagInt) {
                attributeValue = ((NBTTagInt) nbtBase).e();
            } else if (nbtBase instanceof NBTTagLong) {
                attributeValue = ((NBTTagLong) nbtBase).d();
            } else if (nbtBase instanceof NBTTagShort) {
                attributeValue = ((NBTTagShort) nbtBase).f();
            } else if (nbtBase instanceof NBTTagString) {
                attributeValue = ((NBTTagString) nbtBase).c_();
            } else {
                return false;       // Value not allowed
            }

            if (!Objects.equals(attributeValue, attribute.getValue()))
                return false;
        }

        return true;
    }

    @Override
    public void awardKillScore(Player playerKiller,
                               org.bukkit.entity.Entity bukkitDamaged,
                               org.bukkit.entity.Entity directDamager) {
        EntityPlayer entityPlayer = ((CraftPlayer) playerKiller).getHandle();
        Entity damaged = ((CraftEntity) bukkitDamaged).getHandle();
        Entity damager = ((CraftEntity) directDamager).getHandle();

        DamageSource damageSource = null;

        if (damager instanceof EntityHuman) {
            damageSource = DamageSource.playerAttack((EntityHuman) damager);
        } else if (damager instanceof EntityArrow) {
            EntityArrow entityArrow = (EntityArrow) damager;
            damageSource = DamageSource.arrow(entityArrow, entityArrow.shooter);
        } else if (damager instanceof EntityFireball) {
            EntityFireball entityFireball = (EntityFireball) damager;
            damageSource = DamageSource.fireball(entityFireball, entityFireball.shooter);
        }

        if (damageSource == null) {
            if (damager instanceof EntityLiving) {
                damageSource = DamageSource.mobAttack((EntityLiving) damager);
            } else {
                return;
            }
        }

        entityPlayer.a(damaged, 0, damageSource);
    }

    @Override
    public void awardPickupScore(Player player, Item pickItem) {
        // Do nothing.
    }

    @Override
    public void awardCrossbowShot(Player player, LivingEntity target, org.bukkit.inventory.ItemStack crossBowItem) {
        // Do nothing.
    }

    @Override
    public void playPickupAnimation(LivingEntity livingEntity, Item item) {
        EntityLiving entityLiving = ((CraftLivingEntity) livingEntity).getHandle();
        EntityItem entityItem = (EntityItem) ((CraftItem) item).getHandle();
        EntityTracker entityTracker = ((WorldServer) entityLiving.world).getTracker();
        entityTracker.a(entityItem, new PacketPlayOutCollect(entityItem.getId(), entityLiving.getId(), item.getItemStack().getAmount()));
        //Makes sure the entity is still there.
        entityTracker.a(entityItem, new PacketPlayOutSpawnEntity(entityItem, 2, 1));
        entityTracker.a(entityItem, new PacketPlayOutEntityMetadata(entityItem.getId(), entityItem.getDataWatcher(), true));
    }

    @Override
    public void playDeathSound(LivingEntity livingEntity) {
        EntityLiving entityLiving = ((CraftLivingEntity) livingEntity).getHandle();

        SoundEffect deathSound = GET_SOUND_DEATH.invoke(entityLiving);
        float soundVolume = GET_SOUND_VOLUME.invoke(entityLiving);
        float soundPitch = GET_SOUND_PITCH.invoke(entityLiving);

        if (deathSound != null)
            entityLiving.a(deathSound, soundVolume, soundPitch);
    }

    @Override
    public void playSpawnEffect(LivingEntity livingEntity) {
        EntityLiving entityLiving = ((CraftLivingEntity) livingEntity).getHandle();
        if (entityLiving instanceof EntityInsentient)
            ((EntityInsentient) entityLiving).doSpawnEffect();
    }

    @Override
    public void handleItemPickup(LivingEntity bukkitLivingEntity, StackedItem stackedItem, int remaining) {
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

            if (isPlayerPickup) {
                if (plugin.getSettings().getItems().isFixStackEnabled()) {
                    itemStack.setCount(maxStackSize);
                    retryPickup = true;
                } else {
                    itemStack.setCount(stackAmount);
                }
            } else {
                itemStack.setCount(maxStackSize);
            }

            pickupItem = new EntityItem(entityItem.world, entityItem.locX, entityItem.locY, entityItem.locZ, itemStack);
        }

        int originalItemCount = pickupItem.getItemStack().getCount();
        int originalPickupDelay = entityItem.pickupDelay;
        boolean isDifferentPickupItem = pickupItem != entityItem;
        boolean actualItemDupe = originalItemCount != stackAmount;

        try {
            if (isDifferentPickupItem) entityItem.s(); // setNeverPickUp
            EntitiesListener.IMP.secondPickupEventCall = true;
            EntitiesListener.IMP.secondPickupEvent = null;
            if (isPlayerPickup) {
                pickupItem.d((EntityHuman) entityLiving); // playerTouch
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
                entityItem.q(); // setDefaultPickUpDelay
            }
        }

        if (!actualItemDupe && isDifferentPickupItem) {
            int simulatedItemPickupCount = Math.min(pickupCount, maxStackSize);

            if (pickupCount < originalItemCount) {
                // Need to simulate item pickup
                ItemStack simulatedItem = pickupItem.getItemStack().cloneItemStack();
                simulatedItem.setCount(simulatedItemPickupCount);

                EntityItem simulatedEntityItemPickup = new EntityItem(entityItem.world,
                        entityItem.locX, entityItem.locY, entityItem.locZ, simulatedItem);

                EntityTracker entityTracker = ((WorldServer) entityLiving.world).getTracker();

                entityTracker.a(entityLiving, new PacketPlayOutSpawnEntity(simulatedEntityItemPickup, 2, 1));
                entityTracker.a(entityLiving, new PacketPlayOutEntityMetadata(
                        simulatedEntityItemPickup.getId(), simulatedEntityItemPickup.getDataWatcher(), true));
                entityTracker.a(entityLiving, new PacketPlayOutCollect(
                        simulatedEntityItemPickup.getId(), entityLiving.getId(), simulatedItemPickupCount));
                entityTracker.a(entityLiving, new PacketPlayOutEntityDestroy(simulatedEntityItemPickup.getId()));
            } else {
                entityLiving.receive(entityItem, simulatedItemPickupCount);
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
        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();

        ItemStack mendingItem = EnchantmentManager.b(Enchantments.C, entityPlayer);

        if (!mendingItem.isEmpty() && mendingItem.getItem().usesDurability()) {
            int repairAmount = Math.min(amount * 2, mendingItem.getData());
            amount -= repairAmount / 2;
            mendingItem.setData(mendingItem.getData() - repairAmount);
        }

        if (amount > 0) {
            PlayerExpChangeEvent playerExpChangeEvent = new PlayerExpChangeEvent(player, amount);
            Bukkit.getPluginManager().callEvent(playerExpChangeEvent);
            if (playerExpChangeEvent.getAmount() > 0)
                player.giveExp(playerExpChangeEvent.getAmount());
        }
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
    public String getCustomName(org.bukkit.entity.Entity bukkitEntity) {
        return bukkitEntity.getCustomName();
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
                NMSEntitiesImpl.this.setHealthDirectly(bukkitLivingEntity, health, preventUpdate);
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
