package com.bgsoftware.wildstacker.nms;

import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.enums.SpawnCause;
import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.api.objects.StackedItem;
import com.bgsoftware.wildstacker.api.upgrades.SpawnerUpgrade;
import com.bgsoftware.wildstacker.objects.WStackedEntity;
import com.bgsoftware.wildstacker.objects.WStackedItem;
import com.bgsoftware.wildstacker.utils.chunks.ChunkPosition;
import com.bgsoftware.wildstacker.utils.spawners.SpawnerCachedData;
import com.bgsoftware.wildstacker.utils.spawners.SyncedCreatureSpawner;
import com.bgsoftware.wildstacker.utils.threads.Executor;
import io.papermc.paper.enchantments.EnchantmentRarity;
import net.kyori.adventure.text.Component;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.core.BlockPosition;
import net.minecraft.nbt.DynamicOpsNBT;
import net.minecraft.nbt.NBTCompressedStreamTools;
import net.minecraft.nbt.NBTReadLimiter;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.chat.ChatComponentText;
import net.minecraft.network.chat.ChatMessage;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.game.PacketPlayOutCollect;
import net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata;
import net.minecraft.network.protocol.game.PacketPlayOutSpawnEntity;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.network.syncher.DataWatcherRegistry;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.server.level.ChunkProviderServer;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.stats.StatisticList;
import net.minecraft.tags.TagsItem;
import net.minecraft.util.UtilColor;
import net.minecraft.world.EnumHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityExperienceOrb;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EntityPositionTypes;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.entity.EnumMobSpawn;
import net.minecraft.world.entity.SaddleStorage;
import net.minecraft.world.entity.ai.gossip.Reputation;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.animal.EntityAnimal;
import net.minecraft.world.entity.animal.EntityChicken;
import net.minecraft.world.entity.animal.EntityTurtle;
import net.minecraft.world.entity.decoration.EntityArmorStand;
import net.minecraft.world.entity.item.EntityItem;
import net.minecraft.world.entity.monster.EntityStrider;
import net.minecraft.world.entity.monster.EntityZombieVillager;
import net.minecraft.world.entity.monster.piglin.EntityPiglin;
import net.minecraft.world.entity.monster.piglin.PiglinAI;
import net.minecraft.world.entity.npc.EntityVillager;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.entity.raid.EntityRaider;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemSword;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentManager;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.MobSpawnerAbstract;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.BlockRotatable;
import net.minecraft.world.level.block.IFluidContainer;
import net.minecraft.world.level.block.entity.TileEntityMobSpawner;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.material.FluidTypes;
import net.minecraft.world.phys.AxisAlignedBB;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.TurtleEgg;
import org.bukkit.craftbukkit.v1_17_R1.CraftParticle;
import org.bukkit.craftbukkit.v1_17_R1.CraftServer;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_17_R1.block.CraftBlockEntityState;
import org.bukkit.craftbukkit.v1_17_R1.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftAnimals;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftChicken;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftItem;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPiglin;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftStrider;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftTurtle;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftVillager;
import org.bukkit.craftbukkit.v1_17_R1.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_17_R1.util.CraftMagicNumbers;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.EntityCategory;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.MushroomCow;
import org.bukkit.entity.Piglin;
import org.bukkit.entity.Player;
import org.bukkit.entity.Strider;
import org.bukkit.entity.Vehicle;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.entity.EntityTransformEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerItemMendEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.metadata.MetadataStoreBase;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;

@SuppressWarnings("ConstantConditions")
public final class NMSAdapter_v1_17_R1 implements NMSAdapter {

    private static final ReflectField<Integer> ENTITY_EXP = new ReflectField<>(EntityInsentient.class, int.class, "bK");
    private static final ReflectField<Integer> LAST_DAMAGE_BY_PLAYER_TIME = new ReflectField<>(EntityLiving.class, int.class, "bd");
    private static final ReflectMethod<Boolean> IS_DROP_EXPERIENCE = new ReflectMethod<>(EntityLiving.class, "isDropExperience");
    private static final ReflectMethod<SoundEffect> GET_SOUND_DEATH = new ReflectMethod<>(EntityLiving.class, "getSoundDeath");
    private static final ReflectMethod<Float> GET_SOUND_VOLUME = new ReflectMethod<>(EntityLiving.class, "getSoundVolume");
    private static final ReflectMethod<Float> GET_SOUND_PITCH = new ReflectMethod<>(EntityLiving.class, "ep");
    private static final ReflectField<Entity.RemovalReason> ENTITY_REMOVE_REASON = new ReflectField<>(Entity.class, Entity.RemovalReason.class, "aB");
    private static final ReflectMethod<Reputation> VILLAGER_REPUTATION = new ReflectMethod<>(EntityVillager.class, "fS");
    private static final ReflectMethod<Boolean> ANIMAL_BREED_ITEM = new ReflectMethod<>(EntityAnimal.class, "n", ItemStack.class);
    private static final ReflectField<Integer> CHICKEN_EGG_LAY_TIME = new ReflectField<>(EntityChicken.class, Integer.class, "bY");
    private static final ReflectField<SaddleStorage> STRIDER_SADDLE_STORAGE = new ReflectField<>(EntityStrider.class, SaddleStorage.class, "cb");
    private static final ReflectMethod<Boolean> RAIDER_CAN_RAID = new ReflectMethod<>(EntityRaider.class, Boolean.class, "fK");
    private static final ReflectMethod<Raid> RAIDER_RAID = new ReflectMethod<>(EntityRaider.class, Raid.class, "fJ");
    private static final ReflectField<Boolean> ENTITY_PERSISTENT = new ReflectField<>(EntityInsentient.class, Boolean.class, "bZ");

    private static final WildStackerPlugin plugin = WildStackerPlugin.getPlugin();

    /*
     *   Entity methods
     */
    private static final DataWatcherObject<Boolean> HAS_TURTLE_EGG = DataWatcher.a(EntityTurtle.class, DataWatcherRegistry.i);
    private static final DataWatcherObject<BlockPosition> TURTLE_HOME_POS = DataWatcher.a(EntityTurtle.class, DataWatcherRegistry.l);
    private static final NamespacedKey
            STACK_AMOUNT = new NamespacedKey(plugin, "stackAmount"),
            SPAWN_CAUSE = new NamespacedKey(plugin, "spawnCause"),
            NAME_TAG = new NamespacedKey(plugin, "nameTag"),
            UPGRADE = new NamespacedKey(plugin, "upgrade");

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

        WStackedEntity.of(bukkitEntity).setSpawnCause(spawnCause);

        if (afterSpawnConsumer != null) {
            //noinspection unchecked
            afterSpawnConsumer.accept((T) bukkitEntity);
        }

        return type.cast(bukkitEntity);
    }

    @Override
    public Zombie spawnZombieVillager(Villager villager) {
        EntityVillager entityVillager = ((CraftVillager) villager).getHandle();
        EntityZombieVillager entityZombieVillager = EntityTypes.bg.a(entityVillager.getWorld());

        assert entityZombieVillager != null;
        entityZombieVillager.s(entityVillager);
        entityZombieVillager.setVillagerData(entityVillager.getVillagerData());

        Reputation villagerReputation;

        if (VILLAGER_REPUTATION.isValid()) {
            villagerReputation = VILLAGER_REPUTATION.invoke(entityVillager);
        } else {
            villagerReputation = entityVillager.fT();
        }

        entityZombieVillager.a(villagerReputation.a(DynamicOpsNBT.a).getValue());
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

        entityVillager.getWorld().addEntity(entityZombieVillager, CreatureSpawnEvent.SpawnReason.INFECTION);
        entityVillager.getWorld().a(null, 1026,
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
        if (itemStack == null)
            return false;

        EntityAnimal nmsEntity = ((CraftAnimals) animal).getHandle();

        if (ANIMAL_BREED_ITEM.isValid()) {
            return ANIMAL_BREED_ITEM.invoke(nmsEntity, CraftItemStack.asNMSCopy(itemStack));
        } else {
            return nmsEntity.isBreedItem(CraftItemStack.asNMSCopy(itemStack));
        }
    }

    @Override
    public int getEntityExp(LivingEntity livingEntity) {
        EntityInsentient entityLiving = (EntityInsentient) ((CraftLivingEntity) livingEntity).getHandle();

        int defaultEntityExp = ENTITY_EXP.get(entityLiving);
        int exp = entityLiving.getExpReward();

        ENTITY_EXP.set(entityLiving, defaultEntityExp);

        return exp;
    }

    @Override
    public boolean canDropExp(LivingEntity livingEntity) {
        EntityLiving entityLiving = ((CraftLivingEntity) livingEntity).getHandle();
        int lastDamageByPlayerTime = LAST_DAMAGE_BY_PLAYER_TIME.get(entityLiving);
        boolean isDropExperience = IS_DROP_EXPERIENCE.invoke(entityLiving);
        return lastDamageByPlayerTime > 0 && isDropExperience &&
                entityLiving.getWorld().getGameRules().getBoolean(GameRules.f);
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
        EntityLiving entityLiving = ((CraftLivingEntity) livingEntity).getHandle();
        ENTITY_REMOVE_REASON.set(entityLiving, dead ? Entity.RemovalReason.b : null);
    }

    @Override
    public int getEggLayTime(Chicken chicken) {
        EntityChicken entityChicken = ((CraftChicken) chicken).getHandle();

        try {
            return entityChicken.bZ;
        } catch (Throwable ex) {
            return CHICKEN_EGG_LAY_TIME.get(entityChicken);
        }
    }

    @Override
    public void setNerfedEntity(LivingEntity livingEntity, boolean nerfed) {
        EntityInsentient entityLiving = (EntityInsentient) ((CraftLivingEntity) livingEntity).getHandle();
        entityLiving.aware = !nerfed;

        try {
            entityLiving.spawnedViaMobSpawner = nerfed;
        } catch (Throwable ignored) {
        }
    }

    @Override
    public void setKiller(LivingEntity livingEntity, Player killer) {
        EntityLiving entityLiving = ((CraftLivingEntity) livingEntity).getHandle();
        entityLiving.bc = killer == null ? null : ((CraftPlayer) killer).getHandle();
    }

    @Override
    public boolean canSpawnOn(org.bukkit.entity.Entity bukkitEntity, Location location) {
        assert location.getWorld() != null;
        World world = ((CraftWorld) location.getWorld()).getHandle();
        EntityTypes<?> entityTypes = ((CraftEntity) bukkitEntity).getHandle().getEntityType();
        return EntityPositionTypes.a(entityTypes, world.getMinecraftWorld(), EnumMobSpawn.c,
                new BlockPosition(location.getX(), location.getY(), location.getZ()), world.getRandom());
    }

    @Override
    public Collection<org.bukkit.entity.Entity> getEntitiesAtChunk(ChunkPosition chunkPosition) {
        return new ArrayList<>();
    }

    @Override
    public Collection<org.bukkit.entity.Entity> getNearbyEntities(Location location, int range, Predicate<org.bukkit.entity.Entity> filter) {
        WorldServer world = ((CraftWorld) location.getWorld()).getHandle();
        List<org.bukkit.entity.Entity> entities = new ArrayList<>();

        AxisAlignedBB axisAlignedBB = new AxisAlignedBB(
                location.getBlockX() - range,
                location.getBlockY() - range,
                location.getBlockZ() - range,
                location.getBlockX() + range,
                location.getBlockY() + range,
                location.getBlockZ() + range
        );

        world.getEntities().a(axisAlignedBB, entity -> {
            if (filter == null || filter.test(entity.getBukkitEntity()))
                entities.add(entity.getBukkitEntity());
        });

        return entities;
    }

    @Override
    public float getItemInMainHandDropChance(EntityEquipment entityEquipment) {
        return entityEquipment.getItemInMainHandDropChance();
    }

    @Override
    public float getItemInOffHandDropChance(EntityEquipment entityEquipment) {
        return entityEquipment.getItemInOffHandDropChance();
    }

    @Override
    public void setItemInMainHand(EntityEquipment entityEquipment, org.bukkit.inventory.ItemStack itemStack) {
        entityEquipment.setItemInMainHand(itemStack);
    }

    @Override
    public void setItemInOffHand(EntityEquipment entityEquipment, org.bukkit.inventory.ItemStack itemStack) {
        entityEquipment.setItemInOffHand(itemStack);
    }

    @Override
    public org.bukkit.inventory.ItemStack getItemInOffHand(EntityEquipment entityEquipment) {
        return entityEquipment.getItemInOffHand();
    }

    @Override
    public boolean shouldArmorBeDamaged(org.bukkit.inventory.ItemStack itemStack) {
        ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
        return nmsItem != null && nmsItem.f();
    }

    @Override
    public boolean doesStriderHaveSaddle(Strider strider) {
        return ((CraftStrider) strider).getHandle().hasSaddle();
    }

    @Override
    public void removeStriderSaddle(Strider strider) {
        try {
            strider.setSaddle(false);
        } catch (Throwable ex) {
            SaddleStorage saddleStorage;

            if (STRIDER_SADDLE_STORAGE.isValid()) {
                saddleStorage = STRIDER_SADDLE_STORAGE.get(((CraftStrider) strider).getHandle());
            } else {
                saddleStorage = ((CraftStrider) strider).getHandle().cc;
            }

            saddleStorage.setSaddle(false);
        }
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
    public void setTurtleEgg(org.bukkit.entity.Entity turtle) {
        ((CraftTurtle) turtle).getHandle().getDataWatcher().set(HAS_TURTLE_EGG, true);
    }

    @Override
    public Location getTurtleHome(org.bukkit.entity.Entity turtle) {
        BlockPosition homePos = ((CraftTurtle) turtle).getHandle().getDataWatcher().get(TURTLE_HOME_POS);
        return new Location(turtle.getWorld(), homePos.getX(), homePos.getY(), homePos.getZ());
    }

    @Override
    public void setTurtleEggsAmount(Block turtleEggBlock, int amount) {
        TurtleEgg turtleEgg = (TurtleEgg) turtleEggBlock.getBlockData();
        turtleEgg.setEggs(amount);
        turtleEggBlock.setBlockData(turtleEgg, true);
    }

    @Override
    public void handleSweepingEdge(Player attacker, org.bukkit.inventory.ItemStack usedItem, LivingEntity target, double damage) {
        EntityLiving targetLiving = ((CraftLivingEntity) target).getHandle();
        EntityHuman entityHuman = ((CraftPlayer) attacker).getHandle();

        // Making sure the player used a sword.
        if (usedItem.getType() == Material.AIR || !(CraftItemStack.asNMSCopy(usedItem).getItem() instanceof ItemSword))
            return;

        float sweepDamage = 1.0F + EnchantmentManager.a(entityHuman) * (float) damage;
        List<EntityLiving> nearbyEntities = targetLiving.getWorld()
                .a(EntityLiving.class, targetLiving.getBoundingBox().grow(1.0D, 0.25D, 1.0D));

        for (EntityLiving nearby : nearbyEntities) {
            if (nearby != targetLiving && nearby != entityHuman && !entityHuman.r(nearby) &&
                    (!(nearby instanceof EntityArmorStand) || !((EntityArmorStand) nearby).isMarker()) &&
                    entityHuman.f(nearby) < 9.0D) {
                nearby.damageEntity(DamageSource.playerAttack(entityHuman).sweep(), sweepDamage);
            }
        }
    }

    @Override
    public String getCustomName(org.bukkit.entity.Entity entity) {
        // Much more optimized way than Bukkit's method.
        IChatBaseComponent chatBaseComponent = ((CraftEntity) entity).getHandle().getCustomName();
        return chatBaseComponent == null ? "" : chatBaseComponent.getText();
    }

    @Override
    public void setCustomName(org.bukkit.entity.Entity entity, String name) {
        // Much more optimized way than Bukkit's method.
        ((CraftEntity) entity).getHandle().setCustomName(name == null || name.isEmpty() ? null : new ChatComponentText(name));
    }

    @Override
    public Object getPersistentDataContainer(org.bukkit.entity.Entity entity) {
        return entity.getPersistentDataContainer();
    }

    /*
     *   Spawner methods
     */

    @Override
    public boolean handleTotemOfUndying(LivingEntity livingEntity) {
        EntityLiving entityLiving = ((CraftLivingEntity) livingEntity).getHandle();

        ItemStack totemOfUndying = ItemStack.b;

        for (EnumHand enumHand : EnumHand.values()) {
            ItemStack handItem = entityLiving.b(enumHand);
            if (handItem.getItem() == Items.sw) {
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
                ((EntityPlayer) entityLiving).b(StatisticList.c.b(Items.sw));
                CriterionTriggers.B.a((EntityPlayer) entityLiving, totemOfUndying);
            }

            entityLiving.setHealth(1.0F);
            entityLiving.removeAllEffects(EntityPotionEffectEvent.Cause.TOTEM);
            entityLiving.addEffect(new MobEffect(MobEffects.j, 900, 1), EntityPotionEffectEvent.Cause.TOTEM);
            entityLiving.addEffect(new MobEffect(MobEffects.v, 100, 1), EntityPotionEffectEvent.Cause.TOTEM);
            entityLiving.addEffect(new MobEffect(MobEffects.l, 800, 0), EntityPotionEffectEvent.Cause.TOTEM);
            entityLiving.getWorld().broadcastEntityEffect(entityLiving, (byte) 35);
        }

        return true;
    }

    @Override
    public SyncedCreatureSpawner createSyncedSpawner(CreatureSpawner creatureSpawner) {
        return new SyncedCreatureSpawnerImpl(creatureSpawner.getBlock());
    }

    /*
     *   Item methods
     */

    @Override
    public boolean isRotatable(Block block) {
        World world = ((CraftWorld) block.getWorld()).getHandle();
        return world.getType(new BlockPosition(block.getX(), block.getY(), block.getZ())).getBlock() instanceof BlockRotatable;
    }

    @Override
    public StackedItem createItem(Location location, org.bukkit.inventory.ItemStack itemStack, SpawnCause spawnCause, Consumer<StackedItem> itemConsumer) {
        CraftWorld craftWorld = (CraftWorld) location.getWorld();

        EntityItem entityItem = new EntityItem(craftWorld.getHandle(), location.getX(), location.getY(), location.getZ(), CraftItemStack.asNMSCopy(itemStack));

        entityItem.ap = 10;

        try{
            entityItem.canMobPickup = false;
            Executor.sync(() -> entityItem.canMobPickup = true, 20L);
        }catch (Throwable ignored){}

        StackedItem stackedItem = WStackedItem.ofBypass((Item) entityItem.getBukkitEntity());

        itemConsumer.accept(stackedItem);

        craftWorld.addEntity(entityItem, spawnCause.toSpawnReason());

        return stackedItem;
    }

    @Override
    @SuppressWarnings("all")
    public Enchantment getGlowEnchant() {
        return new Enchantment(NamespacedKey.minecraft("glowing_enchant")) {
            @Override
            public String getName() {
                return "WildStackerGlow";
            }

            @Override
            public int getMaxLevel() {
                return 1;
            }

            @Override
            public int getStartLevel() {
                return 0;
            }

            @Override
            public EnchantmentTarget getItemTarget() {
                return null;
            }

            @Override
            public boolean isTreasure() {
                return false;
            }

            @Override
            public boolean isCursed() {
                return false;
            }

            @Override
            public boolean conflictsWith(Enchantment enchantment) {
                return false;
            }

            @Override
            public boolean canEnchantItem(org.bukkit.inventory.ItemStack itemStack) {
                return true;
            }

            public Component displayName(int i) {
                return null;
            }

            public boolean isTradeable() {
                return false;
            }

            public boolean isDiscoverable() {
                return false;
            }

            public EnchantmentRarity getRarity() {
                return null;
            }

            public float getDamageIncrease(int i, EntityCategory entityCategory) {
                return 0;
            }

            public Set<EquipmentSlot> getActiveSlots() {
                return null;
            }
        };
    }

    /*
     *   World methods
     */

    @Override
    public org.bukkit.inventory.ItemStack getPlayerSkull(org.bukkit.inventory.ItemStack bukkitItem, String texture) {
        ItemStack itemStack = CraftItemStack.asNMSCopy(bukkitItem);
        NBTTagCompound nbtTagCompound = itemStack.getOrCreateTag();

        NBTTagCompound skullOwner = nbtTagCompound.hasKey("SkullOwner") ? nbtTagCompound.getCompound("SkullOwner") : new NBTTagCompound();

        skullOwner.setString("Id", new UUID(texture.hashCode(), texture.hashCode()).toString());

        NBTTagCompound properties = new NBTTagCompound();

        NBTTagList textures = new NBTTagList();
        NBTTagCompound signature = new NBTTagCompound();
        signature.setString("Value", texture);
        textures.add(signature);

        properties.set("textures", textures);

        skullOwner.set("Properties", properties);

        nbtTagCompound.set("SkullOwner", skullOwner);

        itemStack.setTag(nbtTagCompound);

        return CraftItemStack.asBukkitCopy(itemStack);
    }

    @Override
    public void grandAchievement(Player player, EntityType victim, String name) {
        grandAchievement(player, victim.getKey().toString(), name);
    }

    @Override
    public void grandAchievement(Player player, String criteria, String name) {
        Advancement advancement = Bukkit.getAdvancement(NamespacedKey.minecraft(name));

        if (advancement == null)
            throw new NullPointerException("Invalid advancement " + name);

        AdvancementProgress advancementProgress = player.getAdvancementProgress(advancement);

        if (!advancementProgress.isDone()) {
            advancementProgress.awardCriteria(criteria);
        }
    }

    @Override
    public void playPickupAnimation(LivingEntity livingEntity, Item item) {
        EntityLiving entityLiving = ((CraftLivingEntity) livingEntity).getHandle();
        EntityItem entityItem = (EntityItem) ((CraftItem) item).getHandle();
        ChunkProviderServer chunkProvider = ((WorldServer) entityLiving.getWorld()).getChunkProvider();
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
    public void playParticle(String particle, Location location, int count, int offsetX, int offsetY, int offsetZ, double extra) {
        assert location.getWorld() != null;
        WorldServer world = ((CraftWorld) location.getWorld()).getHandle();
        world.sendParticles(null, CraftParticle.toNMS(Particle.valueOf(particle)), location.getBlockX(), location.getBlockY(), location.getBlockZ(),
                count, offsetX, offsetY, offsetZ, extra, false);
    }

    @Override
    public void playSpawnEffect(LivingEntity livingEntity) {
        EntityInsentient entityInsentient = (EntityInsentient) ((CraftLivingEntity) livingEntity).getHandle();
        entityInsentient.doSpawnEffect();
    }

    @Override
    public Object getBlockData(Material type, short data) {
        return CraftBlockData.fromData(CraftMagicNumbers.getBlock(type, (byte) data));
    }

    @Override
    public void attemptJoinRaid(Player player, org.bukkit.entity.Entity raider) {
        EntityRaider entityRaider = (EntityRaider) ((CraftEntity) raider).getHandle();
        boolean canJoinRaid = RAIDER_CAN_RAID.isValid() ? RAIDER_CAN_RAID.invoke(entityRaider) : entityRaider.fL();
        if (canJoinRaid) {
            Raid villagerRaid = RAIDER_RAID.isValid() ? RAIDER_RAID.invoke(entityRaider) : entityRaider.fK();
            villagerRaid.a((Entity) ((CraftPlayer) player).getHandle());
        }
    }

    @Override
    public boolean attemptToWaterLog(Block block) {
        World world = ((CraftWorld) block.getWorld()).getHandle();
        BlockPosition blockPosition = ((CraftBlock) block).getPosition();
        IBlockData blockData = ((CraftBlock) block).getNMS();

        if (blockData.getBlock() instanceof IFluidContainer) {
            ((IFluidContainer) blockData.getBlock()).place(world, blockPosition, blockData, FluidTypes.c.a(false));
            return true;
        }

        return false;
    }

    @Override
    public boolean handlePiglinPickup(org.bukkit.entity.Entity bukkitPiglin, Item bukkitItem) {
        if (!(bukkitPiglin instanceof Piglin))
            return false;

        EntityPiglin entityPiglin = ((CraftPiglin) bukkitPiglin).getHandle();
        EntityItem entityItem = (EntityItem) ((CraftItem) bukkitItem).getHandle();

        entityPiglin.a(entityItem);

        entityPiglin.getBehaviorController().removeMemory(MemoryModuleType.m);
        entityPiglin.getNavigation().o();

        entityPiglin.receive(entityItem, 1);

        ItemStack itemStack = entityItem.getItemStack().cloneItemStack();
        itemStack.setCount(1);
        net.minecraft.world.item.Item item = itemStack.getItem();
        if (TagsItem.O.isTagged(item)) {
            entityPiglin.getBehaviorController().removeMemory(MemoryModuleType.Y);
            if (!entityPiglin.getItemInOffHand().isEmpty()) {
                entityPiglin.b(entityPiglin.b(EnumHand.b));
            }

            entityPiglin.setSlot(EnumItemSlot.b, itemStack);
            entityPiglin.d(EnumItemSlot.b);

            if (item != PiglinAI.c) {
                if (ENTITY_PERSISTENT.isValid()) {
                    ENTITY_PERSISTENT.set(entityPiglin, true);
                } else {
                    ((EntityInsentient) entityPiglin).ca = true;
                }
            }

            entityPiglin.getBehaviorController().a(MemoryModuleType.X, true, 120L);
        } else if ((item == Items.nJ || item == Items.nK) && !
                entityPiglin.getBehaviorController().hasMemory(MemoryModuleType.ap)) {
            entityPiglin.getBehaviorController().a(MemoryModuleType.ap, true, 200L);
        }

        return true;
    }

    @Override
    public void giveExp(Player player, int amount) {
        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();

        Map.Entry<EnumItemSlot, ItemStack> entry = EnchantmentManager.b(Enchantments.K, entityPlayer);
        ItemStack mendingItem = entry != null ? entry.getValue() : ItemStack.b;

        if (!mendingItem.isEmpty() && mendingItem.getItem().usesDurability()) {
            EntityExperienceOrb orb = EntityTypes.A.a(entityPlayer.getWorld());
            orb.aq = amount;
            orb.spawnReason = ExperienceOrb.SpawnReason.CUSTOM;
            orb.setPositionRaw(entityPlayer.locX(), entityPlayer.locY(), entityPlayer.locZ());
            int repairAmount = Math.min(amount * 2, mendingItem.getDamage());
            PlayerItemMendEvent event = CraftEventFactory.callPlayerItemMendEvent(entityPlayer, orb, mendingItem, repairAmount);
            repairAmount = event.getRepairAmount();
            orb.die();
            if (!event.isCancelled()) {
                amount -= (repairAmount / 2);
                mendingItem.setDamage(mendingItem.getDamage() - repairAmount);
            }
        }

        if (amount > 0) {
            PlayerExpChangeEvent playerExpChangeEvent = new PlayerExpChangeEvent(player, amount);
            Bukkit.getPluginManager().callEvent(playerExpChangeEvent);
            if (playerExpChangeEvent.getAmount() > 0)
                player.giveExp(playerExpChangeEvent.getAmount());
        }
    }

    @Override
    public void enterVehicle(Vehicle vehicle, org.bukkit.entity.Entity entity) {
        vehicle.addPassenger(entity);
    }

    @Override
    public int getPassengersCount(Vehicle vehicle) {
        return (int) vehicle.getPassengers().stream().filter(entity -> !(entity instanceof Player)).count();
    }

    /*
     *   Tag methods
     */

    @Override
    public void updateEntity(LivingEntity sourceBukkit, LivingEntity targetBukkit) {
        EntityLiving source = ((CraftLivingEntity) sourceBukkit).getHandle();
        EntityLiving target = ((CraftLivingEntity) targetBukkit).getHandle();

        NBTTagCompound nbtTagCompound = new NBTTagCompound();
        source.saveData(nbtTagCompound);

        nbtTagCompound.setFloat("Health", source.getMaxHealth());
        nbtTagCompound.remove("SaddleItem");
        nbtTagCompound.remove("Saddle");
        nbtTagCompound.remove("ArmorItem");
        nbtTagCompound.remove("ArmorItems");
        nbtTagCompound.remove("HandItems");
        nbtTagCompound.remove("Leash");
        if (targetBukkit instanceof Zombie) {
            //noinspection deprecation
            ((Zombie) targetBukkit).setBaby(nbtTagCompound.hasKey("IsBaby") && nbtTagCompound.getBoolean("IsBaby"));
        }

        target.loadData(nbtTagCompound);
    }

    @Override
    public String serialize(org.bukkit.inventory.ItemStack itemStack) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        DataOutput dataOutput = new DataOutputStream(outputStream);

        NBTTagCompound tagCompound = new NBTTagCompound();

        ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);

        nmsItem.save(tagCompound);

        try {
            NBTCompressedStreamTools.a(tagCompound, dataOutput);
        } catch (Exception ex) {
            return null;
        }

        return new BigInteger(1, outputStream.toByteArray()).toString(32);
    }

    @Override
    public org.bukkit.inventory.ItemStack deserialize(String serialized) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(new BigInteger(serialized, 32).toByteArray());

        try {
            NBTTagCompound nbtTagCompoundRoot = NBTCompressedStreamTools.a(new DataInputStream(inputStream), NBTReadLimiter.a);

            ItemStack nmsItem = ItemStack.a(nbtTagCompoundRoot);

            return CraftItemStack.asBukkitCopy(nmsItem);
        } catch (Exception ex) {
            return null;
        }
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public org.bukkit.inventory.ItemStack setTag(org.bukkit.inventory.ItemStack itemStack, String key, Object value) {
        ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound tagCompound = nmsItem.hasTag() ? nmsItem.getTag() : new NBTTagCompound();

        if (value instanceof Boolean)
            tagCompound.setBoolean(key, (boolean) value);
        else if (value instanceof Integer)
            tagCompound.setInt(key, (int) value);
        else if (value instanceof String)
            tagCompound.setString(key, (String) value);
        else if (value instanceof Double)
            tagCompound.setDouble(key, (double) value);
        else if (value instanceof Short)
            tagCompound.setShort(key, (short) value);
        else if (value instanceof Byte)
            tagCompound.setByte(key, (byte) value);
        else if (value instanceof Float)
            tagCompound.setFloat(key, (float) value);
        else if (value instanceof Long)
            tagCompound.setLong(key, (long) value);

        nmsItem.setTag(tagCompound);

        return CraftItemStack.asBukkitCopy(nmsItem);
    }

    @Override
    public <T> T getTag(org.bukkit.inventory.ItemStack itemStack, String key, Class<T> valueType, Object def) {
        ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);

        if (nmsItem == null)
            return valueType.cast(def);

        NBTTagCompound tagCompound = nmsItem.hasTag() ? nmsItem.getTag() : new NBTTagCompound();

        if (tagCompound != null) {
            if (!tagCompound.hasKey(key))
                return valueType.cast(def);
            else if (valueType.equals(Boolean.class))
                return valueType.cast(tagCompound.getBoolean(key));
            else if (valueType.equals(Integer.class))
                return valueType.cast(tagCompound.getInt(key));
            else if (valueType.equals(String.class))
                return valueType.cast(tagCompound.getString(key));
            else if (valueType.equals(Double.class))
                return valueType.cast(tagCompound.getDouble(key));
            else if (valueType.equals(Short.class))
                return valueType.cast(tagCompound.getShort(key));
            else if (valueType.equals(Byte.class))
                return valueType.cast(tagCompound.getByte(key));
            else if (valueType.equals(Float.class))
                return valueType.cast(tagCompound.getFloat(key));
            else if (valueType.equals(Long.class))
                return valueType.cast(tagCompound.getLong(key));
        }

        throw new IllegalArgumentException("Cannot find nbt class type: " + valueType);
    }

    /*
     *   Other methods
     */

    @Override
    public Object getChatMessage(String message) {
        return new ChatMessage(message);
    }

    @Override
    public MetadataStoreBase<org.bukkit.entity.Entity> getEntityMetadataStore() {
        return ((CraftServer) Bukkit.getServer()).getEntityMetadata();
    }

    /*
     *   Data methods
     */

    @Override
    public void saveEntity(StackedEntity stackedEntity) {
        LivingEntity livingEntity = stackedEntity.getLivingEntity();
        PersistentDataContainer dataContainer = livingEntity.getPersistentDataContainer();

        dataContainer.set(STACK_AMOUNT, PersistentDataType.INTEGER, stackedEntity.getStackAmount());
        dataContainer.set(SPAWN_CAUSE, PersistentDataType.STRING, stackedEntity.getSpawnCause().name());

        if (stackedEntity.hasNameTag())
            dataContainer.set(NAME_TAG, PersistentDataType.BYTE, (byte) 1);

        int upgradeId = ((WStackedEntity) stackedEntity).getUpgradeId();
        if (upgradeId != 0)
            dataContainer.set(UPGRADE, PersistentDataType.INTEGER, upgradeId);
    }

    @Override
    public void loadEntity(StackedEntity stackedEntity) {
        LivingEntity livingEntity = stackedEntity.getLivingEntity();
        PersistentDataContainer dataContainer = livingEntity.getPersistentDataContainer();

        if (dataContainer.has(STACK_AMOUNT, PersistentDataType.INTEGER)) {
            try {
                Integer stackAmount = dataContainer.get(STACK_AMOUNT, PersistentDataType.INTEGER);
                stackedEntity.setStackAmount(stackAmount, false);

                String spawnCause = dataContainer.get(SPAWN_CAUSE, PersistentDataType.STRING);
                if (spawnCause != null)
                    stackedEntity.setSpawnCause(SpawnCause.valueOf(spawnCause));

                if (dataContainer.has(NAME_TAG, PersistentDataType.BYTE))
                    ((WStackedEntity) stackedEntity).setNameTag();

                Integer upgradeId = dataContainer.get(UPGRADE, PersistentDataType.INTEGER);
                if (upgradeId != null && upgradeId > 0)
                    ((WStackedEntity) stackedEntity).setUpgradeId(upgradeId);
            } catch (Exception ignored) {
            }
        } else {
            // Old saving method
            EntityLiving entityLiving = ((CraftLivingEntity) stackedEntity.getLivingEntity()).getHandle();
            for (String scoreboardTag : entityLiving.getScoreboardTags()) {
                if (scoreboardTag.startsWith("ws:")) {
                    String[] tagSections = scoreboardTag.split("=");
                    if (tagSections.length == 2) {
                        try {
                            String key = tagSections[0], value = tagSections[1];
                            if (key.equals("ws:stack-amount")) {
                                stackedEntity.setStackAmount(Integer.parseInt(value), false);
                            } else if (key.equals("ws:stack-cause")) {
                                stackedEntity.setSpawnCause(SpawnCause.valueOf(value));
                            } else if (key.equals("ws:name-tag")) {
                                ((WStackedEntity) stackedEntity).setNameTag();
                            } else if (key.equals("ws:upgrade")) {
                                ((WStackedEntity) stackedEntity).setUpgradeId(Integer.parseInt(value));
                            }
                        } catch (Exception ignored) {
                        }
                    }
                }
            }
        }
    }

    @Override
    public void saveItem(StackedItem stackedItem) {
        Item item = stackedItem.getItem();
        PersistentDataContainer dataContainer = item.getPersistentDataContainer();
        dataContainer.set(STACK_AMOUNT, PersistentDataType.INTEGER, stackedItem.getStackAmount());
    }

    @Override
    public void loadItem(StackedItem stackedItem) {
        Item item = stackedItem.getItem();
        PersistentDataContainer dataContainer = item.getPersistentDataContainer();

        if (dataContainer.has(STACK_AMOUNT, PersistentDataType.INTEGER)) {
            Integer stackAmount = dataContainer.get(STACK_AMOUNT, PersistentDataType.INTEGER);
            stackedItem.setStackAmount(stackAmount, false);
        } else {
            // Old saving method
            EntityItem entityItem = (EntityItem) ((CraftItem) item).getHandle();
            for (String scoreboardTag : entityItem.getScoreboardTags()) {
                if (scoreboardTag.startsWith("ws:")) {
                    String[] tagSections = scoreboardTag.split("=");
                    if (tagSections.length == 2) {
                        try {
                            String key = tagSections[0], value = tagSections[1];
                            if (key.equals("ws:stack-amount")) {
                                stackedItem.setStackAmount(Integer.parseInt(value), false);
                            }
                        } catch (Exception ignored) {
                        }
                    }
                }
            }
        }
    }

    @SuppressWarnings({"deprecation", "NullableProblems"})
    private static class SyncedCreatureSpawnerImpl extends CraftBlockEntityState<TileEntityMobSpawner> implements SyncedCreatureSpawner {

        private final World world;
        private final BlockPosition blockPosition;
        private final Location blockLocation;

        SyncedCreatureSpawnerImpl(Block block) {
            super(block, TileEntityMobSpawner.class);
            world = ((CraftWorld) block.getWorld()).getHandle();
            blockPosition = new BlockPosition(block.getX(), block.getY(), block.getZ());
            blockLocation = block.getLocation();
        }

        @Override
        public EntityType getSpawnedType() {
            try {
                MinecraftKey key = getMobName();
                EntityType entityType = key == null ? EntityType.PIG : EntityType.fromName(key.getKey());
                return entityType == null ? EntityType.PIG : entityType;
            } catch (Exception ex) {
                return EntityType.PIG;
            }
        }

        @Override
        public void setSpawnedType(EntityType entityType) {
            if (entityType != null && entityType.getName() != null) {
                getSpawner().getSpawner().setMobName(EntityTypes.a(entityType.getName()).orElse(EntityTypes.an));
            } else {
                throw new IllegalArgumentException("Can't spawn EntityType " + entityType + " from mobspawners!");
            }
        }

        @Override
        public void setCreatureTypeByName(String s) {
            EntityType entityType = EntityType.fromName(s);
            if (entityType != null && entityType != EntityType.UNKNOWN)
                setSpawnedType(entityType);
        }

        @Override
        public String getCreatureTypeName() {
            MinecraftKey key = getMobName();
            return key == null ? "PIG" : key.getKey();
        }

        @Override
        public int getDelay() {
            return getSpawner().getSpawner().d;
        }

        @Override
        public void setDelay(int i) {
            getSpawner().getSpawner().d = i;
        }

        @Override
        public int getMinSpawnDelay() {
            return getSpawner().getSpawner().i;
        }

        @Override
        public void setMinSpawnDelay(int i) {
            getSpawner().getSpawner().i = i;
        }

        @Override
        public int getMaxSpawnDelay() {
            return getSpawner().getSpawner().j;
        }

        @Override
        public void setMaxSpawnDelay(int i) {
            getSpawner().getSpawner().j = i;
        }

        @Override
        public int getSpawnCount() {
            return getSpawner().getSpawner().k;
        }

        @Override
        public void setSpawnCount(int i) {
            getSpawner().getSpawner().k = i;
        }

        @Override
        public int getMaxNearbyEntities() {
            return getSpawner().getSpawner().m;
        }

        @Override
        public void setMaxNearbyEntities(int i) {
            getSpawner().getSpawner().m = i;
        }

        @Override
        public int getRequiredPlayerRange() {
            return getSpawner().getSpawner().n;
        }

        @Override
        public void setRequiredPlayerRange(int i) {
            getSpawner().getSpawner().n = i;
        }

        @Override
        public int getSpawnRange() {
            return getSpawner().getSpawner().o;
        }

        @Override
        public void setSpawnRange(int i) {
            getSpawner().getSpawner().o = i;
        }

        public boolean isActivated() {
            return false;
        }

        public void resetTimer() {

        }

        public void setSpawnedItem(org.bukkit.inventory.ItemStack itemStack) {

        }

        @Override
        public void updateSpawner(SpawnerUpgrade spawnerUpgrade) {
        }

        @Override
        public SpawnerCachedData readData() {
            MobSpawnerAbstract mobSpawnerAbstract = getSpawner().getSpawner();
            return new SpawnerCachedData(
                    mobSpawnerAbstract.i,
                    mobSpawnerAbstract.j,
                    mobSpawnerAbstract.k,
                    mobSpawnerAbstract.m,
                    mobSpawnerAbstract.n,
                    mobSpawnerAbstract.o,
                    mobSpawnerAbstract.d / 20,
                    ""
            );
        }

        @Override
        public boolean update(boolean force, boolean applyPhysics) {
            return blockLocation.getBlock().getState().update(force, applyPhysics);
        }

        TileEntityMobSpawner getSpawner() {
            return (TileEntityMobSpawner) world.getTileEntity(blockPosition);
        }

        private MinecraftKey getMobName() {
            String id = getSpawner().getSpawner().f.getEntity().getString("id");
            return UtilColor.b(id) ? null : new MinecraftKey(id);
        }

    }

}
