package com.bgsoftware.wildstacker.nms.v1_7_R4;


import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.wildstacker.api.enums.SpawnCause;
import com.bgsoftware.wildstacker.api.enums.StackCheckResult;
import com.bgsoftware.wildstacker.api.objects.StackedItem;
import com.bgsoftware.wildstacker.nms.v1_7_R4.entity.EntityHelper;
import com.bgsoftware.wildstacker.objects.WStackedEntity;
import com.bgsoftware.wildstacker.objects.WStackedItem;
import com.bgsoftware.wildstacker.utils.entity.EntityUtils;
import com.bgsoftware.wildstacker.utils.legacy.EntityTypes;
import net.minecraft.server.v1_7_R4.AchievementList;
import net.minecraft.server.v1_7_R4.Blocks;
import net.minecraft.server.v1_7_R4.Entity;
import net.minecraft.server.v1_7_R4.EntityAnimal;
import net.minecraft.server.v1_7_R4.EntityExperienceOrb;
import net.minecraft.server.v1_7_R4.EntityHuman;
import net.minecraft.server.v1_7_R4.EntityInsentient;
import net.minecraft.server.v1_7_R4.EntityItem;
import net.minecraft.server.v1_7_R4.EntityLiving;
import net.minecraft.server.v1_7_R4.EntityPlayer;
import net.minecraft.server.v1_7_R4.EntityTracker;
import net.minecraft.server.v1_7_R4.EntityVillager;
import net.minecraft.server.v1_7_R4.EntityZombie;
import net.minecraft.server.v1_7_R4.Item;
import net.minecraft.server.v1_7_R4.ItemArmor;
import net.minecraft.server.v1_7_R4.ItemStack;
import net.minecraft.server.v1_7_R4.Items;
import net.minecraft.server.v1_7_R4.NBTBase;
import net.minecraft.server.v1_7_R4.NBTTagByte;
import net.minecraft.server.v1_7_R4.NBTTagCompound;
import net.minecraft.server.v1_7_R4.NBTTagDouble;
import net.minecraft.server.v1_7_R4.NBTTagFloat;
import net.minecraft.server.v1_7_R4.NBTTagInt;
import net.minecraft.server.v1_7_R4.NBTTagLong;
import net.minecraft.server.v1_7_R4.NBTTagShort;
import net.minecraft.server.v1_7_R4.NBTTagString;
import net.minecraft.server.v1_7_R4.PacketPlayOutCollect;
import net.minecraft.server.v1_7_R4.PacketPlayOutEntityMetadata;
import net.minecraft.server.v1_7_R4.PacketPlayOutSpawnEntity;
import net.minecraft.server.v1_7_R4.WorldServer;
import org.bukkit.Achievement;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_7_R4.CraftWorld;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftAnimals;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftChicken;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftExperienceOrb;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftItem;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftVillager;
import org.bukkit.craftbukkit.v1_7_R4.inventory.CraftItemStack;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.ExperienceOrb;
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
import java.util.Random;
import java.util.function.Consumer;

public final class NMSEntities implements com.bgsoftware.wildstacker.nms.NMSEntities {

    private static final ReflectField<Integer> ENTITY_EXP = new ReflectField<>(EntityInsentient.class, int.class, "b");
    private static final ReflectField<Integer> LAST_DAMAGE_BY_PLAYER_TIME = new ReflectField<>(EntityLiving.class, int.class, "lastDamageByPlayerTime");
    private static final ReflectMethod<Boolean> ALWAYS_GIVES_EXP = new ReflectMethod<>(EntityLiving.class, "alwaysGivesExp");
    private static final ReflectMethod<Boolean> IS_DROP_EXPERIENCE = new ReflectMethod<>(EntityLiving.class, "aG");
    private static final ReflectMethod<String> GET_SOUND_DEATH = new ReflectMethod<>(EntityLiving.class, "aU");
    private static final ReflectMethod<Float> GET_SOUND_VOLUME = new ReflectMethod<>(EntityLiving.class, "bf");
    private static final ReflectMethod<Float> GET_SOUND_PITCH = new ReflectMethod<>(EntityLiving.class, "bg");

    @Override
    public <T extends org.bukkit.entity.Entity> T createEntity(Location location, Class<T> type, SpawnCause spawnCause, Consumer<T> beforeSpawnConsumer, Consumer<T> afterSpawnConsumer) {
        CraftWorld world = (CraftWorld) location.getWorld();

        assert world != null;

        Entity nmsEntity = EntityHelper.createEntity(location, type);
        org.bukkit.entity.Entity bukkitEntity = nmsEntity.getBukkitEntity();

        if (beforeSpawnConsumer != null) {
            //noinspection unchecked
            beforeSpawnConsumer.accept((T) bukkitEntity);
        }

        EntityHelper.addEntity(nmsEntity, spawnCause.toSpawnReason());

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

        assert craftWorld != null;

        EntityItem entityItem = new EntityItem(craftWorld.getHandle(), location.getX(), location.getY(), location.getZ(), CraftItemStack.asNMSCopy(itemStack));

        entityItem.pickupDelay = 10;

        StackedItem stackedItem = WStackedItem.ofBypass((org.bukkit.entity.Item) entityItem.getBukkitEntity());

        itemConsumer.accept(stackedItem);

        EntityHelper.addEntity(entityItem, spawnCause.toSpawnReason());

        return stackedItem;
    }

    @Override
    public ExperienceOrb spawnExpOrb(Location location, SpawnCause spawnCause, int value) {
        return createEntity(location, org.bukkit.entity.ExperienceOrb.class, spawnCause, bukkitOrb -> {
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

        if (entityVillager.hasCustomName()) {
            entityZombie.setCustomName(entityVillager.getCustomName());
            entityZombie.setCustomNameVisible(entityVillager.getCustomNameVisible());
        }

        entityVillager.world.addEntity(entityZombie, CreatureSpawnEvent.SpawnReason.INFECTION);
        entityVillager.world.a(null, 1016, (int) entityVillager.locX, (int) entityVillager.locY, (int) entityVillager.locZ, 0);

        return (Zombie) entityZombie.getBukkitEntity();
    }

    @Override
    public void setInLove(Animals entity, Player breeder, boolean inLove) {
        EntityAnimal nmsEntity = ((CraftAnimals) entity).getHandle();
        EntityPlayer entityPlayer = ((CraftPlayer) breeder).getHandle();
        if (inLove)
            nmsEntity.f(entityPlayer);
        else
            nmsEntity.cf();
    }

    @Override
    public boolean isInLove(Animals entity) {
        return ((EntityAnimal) ((CraftEntity) entity).getHandle()).ce();
    }

    @Override
    public boolean isAnimalFood(Animals animal, org.bukkit.inventory.ItemStack itemStack) {
        EntityAnimal nmsEntity = ((CraftAnimals) animal).getHandle();
        return itemStack != null && nmsEntity.c(CraftItemStack.asNMSCopy(itemStack));
    }

    @Override
    public int getEntityExp(LivingEntity livingEntity) {
        EntityLiving a = ((CraftLivingEntity) livingEntity).getHandle();

        if (!(a instanceof EntityInsentient))
            return 0;

        EntityInsentient entityInsentient = (EntityInsentient) a;

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
    public void setHealthDirectly(LivingEntity livingEntity, double health) {
        EntityLiving entityLiving = ((CraftLivingEntity) livingEntity).getHandle();
        entityLiving.setHealth((float) health);
    }

    @Override
    public void setEntityDead(LivingEntity livingEntity, boolean dead) {
        ((CraftLivingEntity) livingEntity).getHandle().dead = dead;
    }

    @Override
    public int getEggLayTime(Chicken chicken) {
        return ((CraftChicken) chicken).getHandle().bu;
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
    public boolean isDroppedItem(org.bukkit.entity.Entity entity) {
        return ((CraftEntity) entity).getHandle() instanceof EntityItem;
    }

    @Override
    public void awardKillScore(org.bukkit.entity.Entity bukkitDamaged,
                               org.bukkit.entity.Entity damagerEntity) {
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
        ItemStack itemStack = CraftItemStack.asNMSCopy(pickItem.getItemStack());

        if (itemStack.getItem() == Item.getItemOf(Blocks.LOG)) {
            entityPlayer.a(AchievementList.g);
        }

        if (itemStack.getItem() == Item.getItemOf(Blocks.LOG2)) {
            entityPlayer.a(AchievementList.g);
        }

        if (itemStack.getItem() == Items.LEATHER) {
            entityPlayer.a(AchievementList.t);
        }

        if (itemStack.getItem() == Items.DIAMOND) {
            entityPlayer.a(AchievementList.w);
        }

        if (itemStack.getItem() == Items.BLAZE_ROD) {
            entityPlayer.a(AchievementList.A);
        }

        if (itemStack.getItem() == Items.DIAMOND && entityItem.j() != null) {
            EntityHuman otherPlayer = entityItem.world.a(entityItem.j());
            if (otherPlayer != null && otherPlayer != entityPlayer) {
                otherPlayer.a(AchievementList.x);
            }
        }
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
            ((EntityInsentient) entityLiving).s();
    }

    @Override
    public boolean handlePiglinPickup(org.bukkit.entity.Entity bukkitPiglin, org.bukkit.entity.Item item) {
        return false;
    }

    @Override
    public boolean handleEquipmentPickup(LivingEntity livingEntity, org.bukkit.entity.Item bukkitItem) {
        EntityLiving a = ((CraftLivingEntity) livingEntity).getHandle();

        if (!(a instanceof EntityInsentient))
            return false;

        EntityInsentient entityInsentient = (EntityInsentient) a;
        EntityItem entityItem = (EntityItem) ((CraftItem) bukkitItem).getHandle();
        ItemStack itemStack = entityItem.getItemStack().cloneItemStack();
        itemStack.count = 1;

        if (!(itemStack.getItem() instanceof ItemArmor))
            return false;

        int equipmentSlotForItem = EntityInsentient.b(itemStack);

        ItemStack equipmentItem = entityInsentient.getEquipment(equipmentSlotForItem);

        double equipmentDropChance = entityInsentient.dropChances[equipmentSlotForItem];

        Random random = new Random();
        if (equipmentItem != null && Math.max(random.nextFloat() - 0.1F, 0.0F) < equipmentDropChance) {
            entityInsentient.a(equipmentItem, 0F);
        }

        entityInsentient.setEquipment(equipmentSlotForItem, itemStack);
        entityInsentient.dropChances[equipmentSlotForItem] = 2.0F;

        entityInsentient.persistent = true;
        entityInsentient.receive(entityItem, itemStack.count);

        return true;
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
        return bukkitEntity instanceof LivingEntity ? ((LivingEntity) bukkitEntity).getCustomName() : "";
    }

    @Override
    public void setCustomName(org.bukkit.entity.Entity entity, String name) {
        if (entity instanceof LivingEntity)
            ((LivingEntity) entity).setCustomName(name);
    }

    @Override
    public boolean isCustomNameVisible(org.bukkit.entity.Entity entity) {
        return entity instanceof LivingEntity && ((LivingEntity) entity).isCustomNameVisible();
    }

    @Override
    public void setCustomNameVisible(org.bukkit.entity.Entity entity, boolean visible) {
        if (entity instanceof LivingEntity)
            ((LivingEntity) entity).setCustomNameVisible(visible);
    }

}
