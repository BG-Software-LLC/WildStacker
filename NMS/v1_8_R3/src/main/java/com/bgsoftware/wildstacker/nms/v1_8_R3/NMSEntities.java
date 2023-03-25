package com.bgsoftware.wildstacker.nms.v1_8_R3;


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
import com.bgsoftware.wildstacker.utils.legacy.EntityTypes;
import net.minecraft.server.v1_8_R3.AchievementList;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.Blocks;
import net.minecraft.server.v1_8_R3.DataWatcher;
import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.EntityAnimal;
import net.minecraft.server.v1_8_R3.EntityExperienceOrb;
import net.minecraft.server.v1_8_R3.EntityHuman;
import net.minecraft.server.v1_8_R3.EntityInsentient;
import net.minecraft.server.v1_8_R3.EntityItem;
import net.minecraft.server.v1_8_R3.EntityLiving;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.EntityTracker;
import net.minecraft.server.v1_8_R3.EntityVillager;
import net.minecraft.server.v1_8_R3.EntityZombie;
import net.minecraft.server.v1_8_R3.ItemStack;
import net.minecraft.server.v1_8_R3.Items;
import net.minecraft.server.v1_8_R3.NBTBase;
import net.minecraft.server.v1_8_R3.NBTTagByte;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.NBTTagDouble;
import net.minecraft.server.v1_8_R3.NBTTagFloat;
import net.minecraft.server.v1_8_R3.NBTTagInt;
import net.minecraft.server.v1_8_R3.NBTTagLong;
import net.minecraft.server.v1_8_R3.NBTTagShort;
import net.minecraft.server.v1_8_R3.NBTTagString;
import net.minecraft.server.v1_8_R3.PacketPlayOutCollect;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityMetadata;
import net.minecraft.server.v1_8_R3.PacketPlayOutSpawnEntity;
import net.minecraft.server.v1_8_R3.WorldServer;
import org.bukkit.Achievement;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftAnimals;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftChicken;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftExperienceOrb;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftItem;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftVillager;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.MushroomCow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Vehicle;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.material.MaterialData;
import org.bukkit.projectiles.ProjectileSource;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public final class NMSEntities implements com.bgsoftware.wildstacker.nms.NMSEntities {

    private static final WildStackerPlugin plugin = WildStackerPlugin.getPlugin();

    private static final ReflectField<Integer> ENTITY_EXP = new ReflectField<>(EntityInsentient.class, int.class, "b_");
    private static final ReflectField<Integer> LAST_DAMAGE_BY_PLAYER_TIME = new ReflectField<>(EntityLiving.class, int.class, "lastDamageByPlayerTime");
    private static final ReflectMethod<Boolean> ALWAYS_GIVES_EXP = new ReflectMethod<>(EntityLiving.class, "alwaysGivesExp");
    private static final ReflectMethod<Boolean> IS_DROP_EXPERIENCE = new ReflectMethod<>(EntityLiving.class, "ba");
    private static final ReflectMethod<String> GET_SOUND_DEATH = new ReflectMethod<>(EntityLiving.class, "bp");
    private static final ReflectMethod<Float> GET_SOUND_VOLUME = new ReflectMethod<>(EntityLiving.class, "bB");
    private static final ReflectMethod<Float> GET_SOUND_PITCH = new ReflectMethod<>(EntityLiving.class, "bC");
    private static final ReflectMethod<Void> INSENTIENT_PICK_UP_ITEM = new ReflectMethod<>(EntityInsentient.class, "a", EntityItem.class);
    private static final ReflectMethod<DataWatcher.WatchableObject> DATA_WATCHER_GET_ITEM = new ReflectMethod<>(DataWatcher.class, "j", int.class);

    @Override
    public <T extends org.bukkit.entity.Entity> T createEntity(Location location, Class<T> type, SpawnCause spawnCause, Consumer<T> beforeSpawnConsumer, Consumer<T> afterSpawnConsumer) {
        CraftWorld world = (CraftWorld) location.getWorld();

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

        EntityItem entityItem = new EntityItem(craftWorld.getHandle(), location.getX(), location.getY(), location.getZ(), CraftItemStack.asNMSCopy(itemStack));

        entityItem.pickupDelay = 10;

        StackedItem stackedItem = WStackedItem.ofBypass((org.bukkit.entity.Item) entityItem.getBukkitEntity());

        itemConsumer.accept(stackedItem);

        craftWorld.addEntity(entityItem, spawnCause.toSpawnReason());

        return stackedItem;
    }

    @Override
    public ExperienceOrb spawnExpOrb(Location location, SpawnCause spawnCause, int value) {
        return createEntity(location, ExperienceOrb.class, spawnCause, bukkitOrb -> {
            EntityExperienceOrb orb = ((CraftExperienceOrb) bukkitOrb).getHandle();
            orb.value = value;
        }, null);
    }

    @Override
    public Zombie spawnZombieVillager(Villager villager) {
        EntityVillager entityVillager = ((CraftVillager) villager).getHandle();
        EntityZombie entityZombie = new EntityZombie(entityVillager.world);

        entityZombie.m(entityVillager);
        entityZombie.setVillager(true);
        entityZombie.setBaby(entityVillager.isBaby());
        entityZombie.k(entityVillager.ce());

        if (entityVillager.hasCustomName()) {
            entityZombie.setCustomName(entityVillager.getCustomName());
            entityZombie.setCustomNameVisible(entityVillager.getCustomNameVisible());
        }

        entityVillager.world.addEntity(entityZombie, CreatureSpawnEvent.SpawnReason.INFECTION);
        entityVillager.world.a(null, 1016, new BlockPosition(entityVillager), 0);

        return (Zombie) entityZombie.getBukkitEntity();
    }

    @Override
    public void setInLove(Animals entity, Player breeder, boolean inLove) {
        EntityAnimal nmsEntity = ((CraftAnimals) entity).getHandle();
        EntityPlayer entityPlayer = ((CraftPlayer) breeder).getHandle();
        if (inLove)
            nmsEntity.c(entityPlayer);
        else
            nmsEntity.cs();
    }

    @Override
    public boolean isInLove(Animals entity) {
        return ((EntityAnimal) ((CraftEntity) entity).getHandle()).isInLove();
    }

    @Override
    public boolean isAnimalFood(Animals animal, org.bukkit.inventory.ItemStack itemStack) {
        EntityAnimal nmsEntity = ((CraftAnimals) animal).getHandle();
        return itemStack != null && nmsEntity.d(CraftItemStack.asNMSCopy(itemStack));
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
            DataWatcher.WatchableObject item = DATA_WATCHER_GET_ITEM.invoke(dataWatcher, 6);
            if (item != null)
                item.a(false);
        } else {
            // We make sure health is marked dirty
            dataWatcher.update(6);
        }
    }

    @Override
    public int getEggLayTime(Chicken chicken) {
        return ((CraftChicken) chicken).getHandle().bs;
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
        return false;
    }

    @Override
    public void sendEntityDieEvent(LivingEntity livingEntity) {
        // Do nothing.
    }

    @Override
    public boolean callEntityBreedEvent(LivingEntity child, LivingEntity mother, LivingEntity father, @Nullable LivingEntity breeder,
                                        @Nullable org.bukkit.inventory.ItemStack bredWith, int experience) {
        // Does not exist
        return true;
    }

    @Override
    public StackCheckResult areSimilar(EntityTypes entityType, LivingEntity en1, LivingEntity en2) {
        return StackCheckResult.SUCCESS;
    }

    @Override
    public boolean checkEntityAttributes(LivingEntity bukkitEntity, Map<String, Object> attributes) {
        Entity entity = ((CraftEntity) bukkitEntity).getHandle();
        NBTTagCompound entityCompound = new NBTTagCompound();
        entity.e(entityCompound);

        for (Map.Entry<String, Object> attribute : attributes.entrySet()) {
            NBTBase nbtBase = entityCompound.get(attribute.getKey());
            Object attributeValue;

            if (nbtBase instanceof NBTTagByte) {
                attributeValue = ((NBTTagByte) nbtBase).f();
            } else if (nbtBase instanceof NBTTagDouble) {
                attributeValue = ((NBTTagDouble) nbtBase).g();
            } else if (nbtBase instanceof NBTTagFloat) {
                attributeValue = ((NBTTagFloat) nbtBase).h();
            } else if (nbtBase instanceof NBTTagInt) {
                attributeValue = ((NBTTagInt) nbtBase).d();
            } else if (nbtBase instanceof NBTTagLong) {
                attributeValue = ((NBTTagLong) nbtBase).c();
            } else if (nbtBase instanceof NBTTagShort) {
                attributeValue = ((NBTTagShort) nbtBase).e();
            } else if (nbtBase instanceof NBTTagString) {
                attributeValue = ((NBTTagString) nbtBase).a_();
            } else {
                return false;       // Value not allowed
            }

            if (!Objects.equals(attributeValue, attribute.getValue()))
                return false;
        }

        return true;
    }

    @Override
    public void awardKillScore(org.bukkit.entity.Entity bukkitDamaged, org.bukkit.entity.Entity damagerEntity) {
        if (damagerEntity instanceof Player && bukkitDamaged instanceof Monster) {
            ((Player) damagerEntity).awardAchievement(Achievement.KILL_ENEMY);
        } else if (damagerEntity instanceof Arrow && bukkitDamaged instanceof Skeleton) {
            ProjectileSource shooter = ((Arrow) damagerEntity).getShooter();
            if (shooter instanceof Player && ((Player) shooter).getWorld().equals(damagerEntity.getWorld()) &&
                    ((Player) shooter).getLocation().distanceSquared(damagerEntity.getLocation()) >= 2500)
                ((Player) shooter).awardAchievement(Achievement.SNIPE_SKELETON);
        }
    }

    @Override
    public void awardPickupScore(Player player, org.bukkit.entity.Item pickItem) {
        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
        EntityItem entityItem = (EntityItem) ((CraftItem) pickItem).getHandle();
        net.minecraft.server.v1_8_R3.ItemStack itemStack = CraftItemStack.asNMSCopy(pickItem.getItemStack());

        if (itemStack.getItem() == net.minecraft.server.v1_8_R3.Item.getItemOf(Blocks.LOG)) {
            entityPlayer.b(AchievementList.g);
        }

        if (itemStack.getItem() == net.minecraft.server.v1_8_R3.Item.getItemOf(Blocks.LOG2)) {
            entityPlayer.b(AchievementList.g);
        }

        if (itemStack.getItem() == Items.LEATHER) {
            entityPlayer.b(AchievementList.t);
        }

        if (itemStack.getItem() == Items.DIAMOND) {
            entityPlayer.b(AchievementList.w);
        }

        if (itemStack.getItem() == Items.BLAZE_ROD) {
            entityPlayer.b(AchievementList.A);
        }

        if (itemStack.getItem() == Items.DIAMOND && entityItem.n() != null) {
            EntityHuman otherPlayer = entityPlayer.world.a(entityItem.n());
            if (otherPlayer != null && otherPlayer != entityPlayer) {
                otherPlayer.b(AchievementList.x);
            }
        }
    }

    @Override
    public void awardCrossbowShot(Player player, LivingEntity target) {
        // Do nothing
    }

    @Override
    public void playPickupAnimation(LivingEntity livingEntity, org.bukkit.entity.Item item) {
        EntityLiving entityLiving = ((CraftLivingEntity) livingEntity).getHandle();
        EntityItem entityItem = (EntityItem) ((CraftItem) item).getHandle();
        EntityTracker entityTracker = ((WorldServer) entityLiving.world).getTracker();
        entityTracker.a(entityItem, new PacketPlayOutCollect(entityItem.getId(), entityLiving.getId()));
        //Makes sure the entity is still there.
        entityTracker.a(entityItem, new PacketPlayOutSpawnEntity(entityItem, 2, 1));
        entityTracker.a(entityItem, new PacketPlayOutEntityMetadata(entityItem.getId(), entityItem.getDataWatcher(), true));
    }

    @Override
    public void playDeathSound(LivingEntity livingEntity) {
        EntityLiving entityLiving = ((CraftLivingEntity) livingEntity).getHandle();

        String deathSound = GET_SOUND_DEATH.invoke(entityLiving);
        float soundVolume = GET_SOUND_VOLUME.invoke(entityLiving);
        float soundPitch = GET_SOUND_PITCH.invoke(entityLiving);

        if (deathSound != null)
            entityLiving.makeSound(deathSound, soundVolume, soundPitch);
    }

    @Override
    public void playSpawnEffect(LivingEntity livingEntity) {
        EntityLiving entityLiving = ((CraftLivingEntity) livingEntity).getHandle();
        if (entityLiving instanceof EntityInsentient)
            ((EntityInsentient) entityLiving).y();
    }

    @Override
    public void handleItemPickup(org.bukkit.entity.LivingEntity bukkitLivingEntity, StackedItem stackedItem, int remaining) {
        EntityLiving entityLiving = ((CraftLivingEntity) bukkitLivingEntity).getHandle();
        boolean isPlayerPickup = entityLiving instanceof EntityHuman;

        if (!isPlayerPickup && !(entityLiving instanceof EntityInsentient))
            return;

        EntityItem entityItem = (EntityItem) ((CraftItem) stackedItem.getItem()).getHandle();
        if (remaining > 0)
            entityItem.getItemStack().count += remaining;

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
                if (plugin.getSettings().itemsFixStackEnabled) {
                    itemStack.count = maxStackSize;
                    retryPickup = true;
                } else {
                    itemStack.count = stackAmount;
                }
            } else {
                itemStack.count = maxStackSize;
            }

            pickupItem = new EntityItem(entityItem.world, entityItem.locX, entityItem.locY, entityItem.locZ, itemStack);
        }

        int originalItemCount = pickupItem.getItemStack().count;
        int originalPickupDelay = entityItem.pickupDelay;
        boolean isDifferentPickupItem = pickupItem != entityItem;
        boolean actualItemDupe = originalItemCount != stackAmount;

        try {
            if (isDifferentPickupItem) entityItem.r(); // setNeverPickUp
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

        int pickupCount = originalItemCount - (pickupItem.isAlive() ? pickupItem.getItemStack().count : 0);
        if (pickupCount > 0) {
            stackedItem.decreaseStackAmount(pickupCount, true);
            if (retryPickup) {
                handleItemPickup(bukkitLivingEntity, stackedItem, 0);
            } else {
                entityItem.p(); // setDefaultPickUpDelay
            }
        }

        if (!actualItemDupe && isDifferentPickupItem) {
            entityLiving.receive(entityItem, Math.min(pickupCount, maxStackSize));
            if (!pickupItem.isAlive())
                entityItem.die();
        }
    }

    @Override
    public void handleSweepingEdge(Player attacker, org.bukkit.inventory.ItemStack usedItem, LivingEntity target, double damage) {
        // Do nothing.
    }

    @Override
    public void giveExp(Player player, int amount) {
        if (amount <= 0)
            return;

        PlayerExpChangeEvent playerExpChangeEvent = new PlayerExpChangeEvent(player, amount);
        Bukkit.getPluginManager().callEvent(playerExpChangeEvent);

        if (playerExpChangeEvent.getAmount() > 0)
            player.giveExp(playerExpChangeEvent.getAmount());
    }

    @Override
    public void enterVehicle(Vehicle vehicle, org.bukkit.entity.Entity entity) {
        vehicle.setPassenger(entity);
    }

    @Override
    public int getPassengersCount(Vehicle vehicle) {
        return vehicle.getPassenger() == null ? 0 : 1;
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
                NMSEntities.this.setHealthDirectly(bukkitLivingEntity, health, preventUpdate);
            }

            @Override
            public void setRemoved(boolean removed) {
                ((CraftLivingEntity) bukkitLivingEntity).getHandle().dead = removed;
            }

            @Override
            public void setDead(boolean dead) {
                EntityLiving entity = ((CraftLivingEntity) bukkitLivingEntity).getHandle();
                entity.deathTicks = 0;
            }
        };
    }

}
