package com.bgsoftware.wildstacker.nms;

import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.wildstacker.api.enums.SpawnCause;
import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.api.objects.StackedItem;
import com.bgsoftware.wildstacker.api.upgrades.SpawnerUpgrade;
import com.bgsoftware.wildstacker.listeners.EntitiesListener;
import com.bgsoftware.wildstacker.objects.WStackedEntity;
import com.bgsoftware.wildstacker.objects.WStackedItem;
import com.bgsoftware.wildstacker.utils.chunks.ChunkPosition;
import com.bgsoftware.wildstacker.utils.spawners.SpawnerCachedData;
import com.bgsoftware.wildstacker.utils.spawners.SyncedCreatureSpawner;
import net.minecraft.server.v1_8_R2.BlockPosition;
import net.minecraft.server.v1_8_R2.BlockRotatable;
import net.minecraft.server.v1_8_R2.Chunk;
import net.minecraft.server.v1_8_R2.Entity;
import net.minecraft.server.v1_8_R2.EntityAnimal;
import net.minecraft.server.v1_8_R2.EntityHuman;
import net.minecraft.server.v1_8_R2.EntityInsentient;
import net.minecraft.server.v1_8_R2.EntityItem;
import net.minecraft.server.v1_8_R2.EntityLiving;
import net.minecraft.server.v1_8_R2.EntityPlayer;
import net.minecraft.server.v1_8_R2.EntityTracker;
import net.minecraft.server.v1_8_R2.EntityTypes;
import net.minecraft.server.v1_8_R2.EntityVillager;
import net.minecraft.server.v1_8_R2.EntityZombie;
import net.minecraft.server.v1_8_R2.EnumParticle;
import net.minecraft.server.v1_8_R2.IScoreboardCriteria;
import net.minecraft.server.v1_8_R2.IWorldAccess;
import net.minecraft.server.v1_8_R2.ItemStack;
import net.minecraft.server.v1_8_R2.MinecraftKey;
import net.minecraft.server.v1_8_R2.MobEffect;
import net.minecraft.server.v1_8_R2.MobEffectList;
import net.minecraft.server.v1_8_R2.MobSpawnerAbstract;
import net.minecraft.server.v1_8_R2.NBTCompressedStreamTools;
import net.minecraft.server.v1_8_R2.NBTTagCompound;
import net.minecraft.server.v1_8_R2.NBTTagList;
import net.minecraft.server.v1_8_R2.PacketPlayOutCollect;
import net.minecraft.server.v1_8_R2.PacketPlayOutEntityMetadata;
import net.minecraft.server.v1_8_R2.PacketPlayOutSpawnEntity;
import net.minecraft.server.v1_8_R2.Scoreboard;
import net.minecraft.server.v1_8_R2.ScoreboardObjective;
import net.minecraft.server.v1_8_R2.TileEntityMobSpawner;
import net.minecraft.server.v1_8_R2.World;
import net.minecraft.server.v1_8_R2.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.craftbukkit.v1_8_R2.CraftServer;
import org.bukkit.craftbukkit.v1_8_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R2.block.CraftBlockState;
import org.bukkit.craftbukkit.v1_8_R2.entity.CraftAnimals;
import org.bukkit.craftbukkit.v1_8_R2.entity.CraftChicken;
import org.bukkit.craftbukkit.v1_8_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R2.entity.CraftItem;
import org.bukkit.craftbukkit.v1_8_R2.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_8_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R2.entity.CraftVillager;
import org.bukkit.craftbukkit.v1_8_R2.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.metadata.MetadataStoreBase;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@SuppressWarnings({"unused"})
public final class NMSAdapter_v1_8_R2 implements NMSAdapter {

    private static final ReflectField<Integer> ENTITY_EXP = new ReflectField<>(EntityInsentient.class, int.class, "b_");
    private static final ReflectField<Integer> LAST_DAMAGE_BY_PLAYER_TIME = new ReflectField<>(EntityLiving.class, int.class, "lastDamageByPlayerTime");
    private static final ReflectField<Boolean> FROM_MOB_SPAWNER = new ReflectField<>(Entity.class, boolean.class, "fromMobSpawner");
    private static final ReflectField<Collection[]> ENTITY_SLICES = new ReflectField<>(Chunk.class, Collection[].class, "entitySlices");
    private static final ReflectMethod<Boolean> ALWAYS_GIVES_EXP = new ReflectMethod<>(EntityLiving.class, "alwaysGivesExp");
    private static final ReflectMethod<Boolean> IS_DROP_EXPERIENCE = new ReflectMethod<>(EntityLiving.class, "ba");
    private static final ReflectMethod<String> GET_SOUND_DEATH = new ReflectMethod<>(EntityLiving.class, "bp");
    private static final ReflectMethod<Float> GET_SOUND_VOLUME = new ReflectMethod<>(EntityLiving.class, "bB");
    private static final ReflectMethod<Float> GET_SOUND_PITCH = new ReflectMethod<>(EntityLiving.class, "bC");

    public static MobEffectCustomData STACK_AMOUNT = MobEffectCustomData.newEffect(31, new MinecraftKey("ws:stackAmount"))
            .c("ws.stackAmount").withVanillaEffect(MobEffectList.FASTER_DIG);
    public static MobEffectCustomData SPAWN_CAUSE = MobEffectCustomData.newEffect(30, new MinecraftKey("ws:spawnCause"))
            .c("ws.spawnCause").withVanillaEffect(MobEffectList.SLOWER_DIG);
    public static MobEffectCustomData HAS_NAMETAG = MobEffectCustomData.newEffect(29, new MinecraftKey("ws:hasNametag"))
            .c("ws.hasNametag").withVanillaEffect(MobEffectList.SATURATION);
    public static MobEffectCustomData UPGRADE = MobEffectCustomData.newEffect(28, new MinecraftKey("ws:upgrade"))
            .c("ws.upgrade").withVanillaEffect(MobEffectList.BLINDNESS);

    /*
     *   Entity methods
     */

    private static void saveData(Scoreboard scoreboard, UUID entity, String key, int value) {
        ScoreboardObjective objective = scoreboard.getObjective(key);
        if (objective == null)
            objective = scoreboard.registerObjective(key, IScoreboardCriteria.b);

        scoreboard.getPlayerScoreForObjective(entity + "", objective).setScore(value);
    }

    private static int getData(Scoreboard scoreboard, UUID entity, String key) {
        ScoreboardObjective objective = scoreboard.getObjective(key);

        if (objective == null || !scoreboard.getPlayers().contains(entity + ""))
            return -1;

        return scoreboard.getPlayerScoreForObjective(entity + "", objective).getScore();
    }

    private static void setEffect(EntityLiving entityLiving, CustomMobEffect mobEffect) {
        entityLiving.effects.put(mobEffect.getCustomId(), mobEffect);
    }

    @Override
    public <T extends org.bukkit.entity.Entity> T createEntity(Location location, Class<T> type, SpawnCause spawnCause, Consumer<T> beforeSpawnConsumer, Consumer<T> afterSpawnConsumer) {
        CraftWorld world = (CraftWorld) location.getWorld();

        assert world != null;

        Entity nmsEntity = EntityHelper_v1_8_R2.createEntity(location, type);
        org.bukkit.entity.Entity bukkitEntity = nmsEntity.getBukkitEntity();

        if (beforeSpawnConsumer != null) {
            //noinspection unchecked
            beforeSpawnConsumer.accept((T) bukkitEntity);
        }

        EntityHelper_v1_8_R2.addEntity(nmsEntity, spawnCause.toSpawnReason());

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
        boolean alwaysGivesExp = ALWAYS_GIVES_EXP.invoke(entityLiving);
        boolean isDropExperience = IS_DROP_EXPERIENCE.invoke(entityLiving);
        return !entityLiving.world.isClientSide && (lastDamageByPlayerTime > 0 || alwaysGivesExp) && isDropExperience && entityLiving.world.getGameRules().getBoolean("doMobLoot");
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

    /*
     *   Spawner methods
     */

    @Override
    public boolean canSpawnOn(org.bukkit.entity.Entity bukkitEntity, Location location) {
        World world = ((CraftWorld) location.getWorld()).getHandle();
        Entity entity = EntityTypes.a(bukkitEntity.getEntityId(), world);
        entity.setPosition(location.getX(), location.getY(), location.getZ());
        return !(entity instanceof EntityInsentient) || ((EntityInsentient) entity).canSpawn();
    }

    @Override
    public Collection<org.bukkit.entity.Entity> getEntitiesAtChunk(ChunkPosition chunkPosition) {
        World world = ((CraftWorld) Bukkit.getWorld(chunkPosition.getWorld())).getHandle();

        Chunk chunk = world.getChunkIfLoaded(chunkPosition.getX(), chunkPosition.getZ());

        if (chunk == null)
            return new ArrayList<>();

        return Arrays.stream(chunk.entitySlices)
                .flatMap(Collection::stream)
                .map(Entity::getBukkitEntity)
                .collect(Collectors.toList());
    }

    /*
     *   Item methods
     */

    @Override
    public boolean shouldArmorBeDamaged(org.bukkit.inventory.ItemStack itemStack) {
        ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
        return nmsItem != null && nmsItem.e();
    }

    @Override
    public SyncedCreatureSpawner createSyncedSpawner(CreatureSpawner creatureSpawner) {
        return new SyncedCreatureSpawnerImpl(creatureSpawner.getBlock());
    }

    @Override
    public boolean isRotatable(Block block) {
        World world = ((CraftWorld) block.getWorld()).getHandle();
        return world.getType(new BlockPosition(block.getX(), block.getY(), block.getZ())).getBlock() instanceof BlockRotatable;
    }

    @Override
    public StackedItem createItem(Location location, org.bukkit.inventory.ItemStack itemStack, SpawnCause spawnCause, Consumer<StackedItem> itemConsumer) {
        CraftWorld craftWorld = (CraftWorld) location.getWorld();

        assert craftWorld != null;

        EntityItem entityItem = new EntityItem(craftWorld.getHandle(), location.getX(), location.getY(), location.getZ(), CraftItemStack.asNMSCopy(itemStack));

        entityItem.pickupDelay = 10;

        StackedItem stackedItem = WStackedItem.ofBypass((Item) entityItem.getBukkitEntity());

        itemConsumer.accept(stackedItem);

        EntityHelper_v1_8_R2.addEntity(entityItem, spawnCause.toSpawnReason());

        return stackedItem;
    }

    /*
     *   World methods
     */

    @Override
    public Enchantment getGlowEnchant() {
        return new Enchantment(101) {
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
            public boolean conflictsWith(Enchantment enchantment) {
                return false;
            }

            @Override
            public boolean canEnchantItem(org.bukkit.inventory.ItemStack itemStack) {
                return true;
            }
        };
    }

    @Override
    public org.bukkit.inventory.ItemStack getPlayerSkull(org.bukkit.inventory.ItemStack bukkitItem, String texture) {
        ItemStack itemStack = CraftItemStack.asNMSCopy(bukkitItem);
        NBTTagCompound nbtTagCompound = itemStack.getTag() != null ? itemStack.getTag() : new NBTTagCompound();

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
    public boolean isDroppedItem(org.bukkit.entity.Entity entity) {
        return ((CraftEntity) entity).getHandle() instanceof EntityItem;
    }

    @Override
    public void playPickupAnimation(LivingEntity livingEntity, Item item) {
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
    public void playParticle(String particle, Location location, int count, int offsetX, int offsetY, int offsetZ, double extra) {
        WorldServer world = ((CraftWorld) location.getWorld()).getHandle();
        world.sendParticles(null, EnumParticle.valueOf(particle), true, location.getBlockX(), location.getBlockY(), location.getBlockZ(),
                count, offsetX, offsetY, offsetZ, extra);
    }

    @Override
    public void playSpawnEffect(LivingEntity livingEntity) {
        EntityInsentient entityInsentient = (EntityInsentient) ((CraftLivingEntity) livingEntity).getHandle();
        entityInsentient.y();
    }

    @Override
    public void startEntityListen(org.bukkit.World world) {
        ((CraftWorld) world).getHandle().addIWorldAccess(new IWorldAccess() {
            @Override
            public void a(BlockPosition blockPosition) {

            }

            @Override
            public void b(BlockPosition blockPosition) {

            }

            @Override
            public void a(int i, int i1, int i2, int i3, int i4, int i5) {

            }

            @Override
            public void a(String s, double v, double v1, double v2, float v3, float v4) {

            }

            @Override
            public void a(EntityHuman entityHuman, String s, double v, double v1, double v2, float v3, float v4) {

            }

            @Override
            public void a(int i, boolean b, double v, double v1, double v2, double v3, double v4, double v5, int... ints) {

            }

            @Override
            public void a(Entity entity) {

            }

            @Override
            public void b(Entity entity) {
                EntitiesListener.IMP.handleEntityRemove(entity.getBukkitEntity());
            }

            @Override
            public void a(String s, BlockPosition blockPosition) {

            }

            @Override
            public void a(int i, BlockPosition blockPosition, int i1) {

            }

            @Override
            public void a(EntityHuman entityHuman, int i, BlockPosition blockPosition, int i1) {

            }

            @Override
            public void b(int i, BlockPosition blockPosition, int i1) {

            }
        });
    }

    /*
     *   Tag methods
     */

    @Override
    public void updateEntity(org.bukkit.entity.LivingEntity sourceBukkit, org.bukkit.entity.LivingEntity targetBukkit) {
        EntityLiving source = ((CraftLivingEntity) sourceBukkit).getHandle();
        EntityLiving target = ((CraftLivingEntity) targetBukkit).getHandle();

        NBTTagCompound nbtTagCompound = new NBTTagCompound();
        source.b(nbtTagCompound);

        nbtTagCompound.setFloat("HealF", source.getMaxHealth());
        nbtTagCompound.setShort("Health", (short) Math.ceil(source.getMaxHealth()));
        nbtTagCompound.remove("SaddleItem");
        nbtTagCompound.remove("Saddle");
        nbtTagCompound.remove("ArmorItem");
        nbtTagCompound.remove("Equipment");
        nbtTagCompound.remove("DropChances");
        nbtTagCompound.remove("Leash");
        nbtTagCompound.remove("Leashed");
        if (targetBukkit instanceof Zombie)
            ((Zombie) targetBukkit).setBaby(nbtTagCompound.hasKey("IsBaby") && nbtTagCompound.getBoolean("IsBaby"));

        target.a(nbtTagCompound);
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

    /*
     *   Other methods
     */

    @Override
    public MetadataStoreBase<org.bukkit.entity.Entity> getEntityMetadataStore() {
        return ((CraftServer) Bukkit.getServer()).getEntityMetadata();
    }

    @Override
    public void runAtEndOfTick(Runnable code) {
        ((CraftServer) Bukkit.getServer()).getServer().a(code::run);
    }

    /*
     *   Data methods
     */

    @Override
    public org.bukkit.inventory.ItemStack deserialize(String serialized) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(new BigInteger(serialized, 32).toByteArray());

        try {
            NBTTagCompound nbtTagCompoundRoot = NBTCompressedStreamTools.a(new DataInputStream(inputStream));

            ItemStack nmsItem = ItemStack.createStack(nbtTagCompoundRoot);

            return CraftItemStack.asBukkitCopy(nmsItem);
        } catch (Exception ex) {
            return null;
        }

    }

    @Override
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

        throw new IllegalArgumentException("Cannot find nbt class type: " + valueType);
    }

    @Override
    public void saveEntity(StackedEntity stackedEntity) {
        EntityLiving entityLiving = ((CraftLivingEntity) stackedEntity.getLivingEntity()).getHandle();
        setEffect(entityLiving, new CustomMobEffect(STACK_AMOUNT, stackedEntity.getStackAmount()));
        setEffect(entityLiving, new CustomMobEffect(SPAWN_CAUSE, stackedEntity.getSpawnCause().getId()));
        if (stackedEntity.hasNameTag())
            setEffect(entityLiving, new CustomMobEffect(HAS_NAMETAG, 1));
        int upgradeId = ((WStackedEntity) stackedEntity).getUpgradeId();
        if (upgradeId != 0)
            setEffect(entityLiving, new CustomMobEffect(UPGRADE, upgradeId));
    }

    @Override
    public void loadEntity(StackedEntity stackedEntity) {
        EntityLiving entityLiving = ((CraftLivingEntity) stackedEntity.getLivingEntity()).getHandle();

        // Loading old data from scoreboards
        {
            Scoreboard worldScoreboard = entityLiving.world.getScoreboard();

            int stackAmount = getData(worldScoreboard, entityLiving.getUniqueID(), "ws:stack-amount");
            int spawnCause = getData(worldScoreboard, entityLiving.getUniqueID(), "ws:stack-cause");
            int nameTag = getData(worldScoreboard, entityLiving.getUniqueID(), "ws:name-tag");

            if (stackAmount > 0)
                stackedEntity.setStackAmount(stackAmount, false);

            if (spawnCause > 0)
                stackedEntity.setSpawnCause(SpawnCause.valueOf(spawnCause));

            if (nameTag == 1)
                ((WStackedEntity) stackedEntity).setNameTag();

            worldScoreboard.resetPlayerScores(entityLiving.getUniqueID() + "", null);
        }

        {
            // This section is used to convert the effects into custom ones.
            // This is done because we cannot save custom effects, otherwise 1.8 clients crash.
            {
                MobEffect stackAmountLoad = entityLiving.effects.get(STACK_AMOUNT.vanillaEffect.id),
                        spawnCauseLoad = entityLiving.effects.get(SPAWN_CAUSE.vanillaEffect.id),
                        hasNametagLoad = entityLiving.effects.get(HAS_NAMETAG.vanillaEffect.id),
                        hasUpgradeLoad = entityLiving.effects.get(UPGRADE.vanillaEffect.id);

                if (stackAmountLoad != null && stackAmountLoad.getDuration() > 2140000000) {
                    setEffect(entityLiving, new CustomMobEffect(STACK_AMOUNT, stackAmountLoad.getAmplifier()));
                    entityLiving.effects.remove(stackAmountLoad.getEffectId());
                }

                if (spawnCauseLoad != null && spawnCauseLoad.getDuration() > 2140000000) {
                    setEffect(entityLiving, new CustomMobEffect(SPAWN_CAUSE, spawnCauseLoad.getAmplifier()));
                    entityLiving.effects.remove(spawnCauseLoad.getEffectId());
                }

                if (hasNametagLoad != null && hasNametagLoad.getDuration() > 2140000000) {
                    setEffect(entityLiving, new CustomMobEffect(HAS_NAMETAG, hasNametagLoad.getAmplifier()));
                    entityLiving.effects.remove(hasNametagLoad.getEffectId());
                }

                if (hasUpgradeLoad != null && hasUpgradeLoad.getDuration() > 2140000000) {
                    setEffect(entityLiving, new CustomMobEffect(UPGRADE, hasUpgradeLoad.getAmplifier()));
                    entityLiving.effects.remove(hasUpgradeLoad.getEffectId());
                }
            }

            // Loading data from custom effects
            {
                MobEffect stackAmount = entityLiving.getEffect(STACK_AMOUNT),
                        spawnCause = entityLiving.getEffect(SPAWN_CAUSE),
                        hasNametag = entityLiving.getEffect(HAS_NAMETAG),
                        upgrade = entityLiving.getEffect(UPGRADE);

                if (stackAmount != null)
                    stackedEntity.setStackAmount(stackAmount.getAmplifier(), false);

                if (spawnCause != null)
                    stackedEntity.setSpawnCause(SpawnCause.valueOf(spawnCause.getAmplifier()));

                if (hasNametag != null && hasNametag.getAmplifier() == 1)
                    ((WStackedEntity) stackedEntity).setNameTag();

                if (upgrade != null && upgrade.getAmplifier() != 0)
                    ((WStackedEntity) stackedEntity).setUpgradeId(upgrade.getAmplifier());
            }
        }
    }

    @Override
    public void saveItem(StackedItem stackedItem) {
        if (stackedItem.getStackAmount() > stackedItem.getItemStack().getType().getMaxStackSize()) {
            EntityItem entityItem = (EntityItem) ((CraftItem) stackedItem.getItem()).getHandle();
            Scoreboard worldScoreboard = entityItem.world.getScoreboard();
            saveData(worldScoreboard, entityItem.getUniqueID(), "ws:stack-amount", stackedItem.getStackAmount());
        }
    }

    @Override
    public void loadItem(StackedItem stackedItem) {
        EntityItem entityItem = (EntityItem) ((CraftItem) stackedItem.getItem()).getHandle();
        Scoreboard worldScoreboard = entityItem.world.getScoreboard();

        int stackAmount = getData(worldScoreboard, entityItem.getUniqueID(), "ws:stack-amount");
        if (stackAmount > 0)
            stackedItem.setStackAmount(stackAmount, false);
    }

    private static final class MobEffectCustomData extends MobEffectList {

        private MobEffectList vanillaEffect;

        MobEffectCustomData(int id, MinecraftKey minecraftKey) {
            super(id, minecraftKey, false, 16262179);
        }

        static MobEffectCustomData newEffect(int id, MinecraftKey minecraftKey) {
            try {
                new MobEffectCustomData(id, minecraftKey);
            } catch (Exception ignored) {
            }
            return (MobEffectCustomData) MobEffectList.byId[id];
        }

        public MobEffectCustomData withVanillaEffect(MobEffectList vanillaEffect) {
            this.vanillaEffect = vanillaEffect;
            return this;
        }

        public MobEffectList getVanillaEffect() {
            return vanillaEffect;
        }

        @Override
        public MobEffectCustomData c(String s) {
            return (MobEffectCustomData) super.c(s);
        }

    }

    private static final class CustomMobEffect extends MobEffect {

        private final int customId;

        CustomMobEffect(MobEffectCustomData mobEffectCustomData, int value) {
            super(mobEffectCustomData.vanillaEffect.id, Integer.MAX_VALUE, value, false, false);
            customId = mobEffectCustomData.id;
        }

        public int getCustomId() {
            return customId;
        }
    }

    @SuppressWarnings("deprecation")
    private static class SyncedCreatureSpawnerImpl extends CraftBlockState implements SyncedCreatureSpawner {

        private final World world;
        private final BlockPosition blockPosition;

        SyncedCreatureSpawnerImpl(Block block) {
            super(block);
            world = ((CraftWorld) block.getWorld()).getHandle();
            blockPosition = new BlockPosition(block.getX(), block.getY(), block.getZ());
        }

        @Override
        public CreatureType getCreatureType() {
            return CreatureType.fromEntityType(getSpawnedType());
        }

        @Override
        public void setCreatureType(CreatureType creatureType) {
            setSpawnedType(creatureType.toEntityType());
        }

        @Override
        public String getCreatureTypeId() {
            return getCreatureTypeName();
        }

        @Override
        public void setCreatureTypeId(String s) {
            setCreatureTypeByName(s);
        }

        @Override
        public int getDelay() {
            return getSpawner().getSpawner().spawnDelay;
        }

        @Override
        public void setDelay(int i) {
            getSpawner().getSpawner().spawnDelay = i;
        }

        @Override
        public void setCreatureTypeByName(String s) {
            EntityType entityType = EntityType.fromName(s);
            if (entityType != null && entityType != EntityType.UNKNOWN)
                setSpawnedType(entityType);
        }

        @Override
        public String getCreatureTypeName() {
            return getSpawner().getSpawner().getMobName();
        }

        @Override
        public EntityType getSpawnedType() {
            try {
                return EntityType.fromName(getSpawner().getSpawner().getMobName());
            } catch (Exception ex) {
                return EntityType.PIG;
            }
        }

        @Override
        public void setSpawnedType(EntityType entityType) {
            if (entityType != null && entityType.getName() != null) {
                getSpawner().getSpawner().setMobName(entityType.getName());
            } else {
                throw new IllegalArgumentException("Can't spawn EntityType " + entityType + " from mobspawners!");
            }
        }

        @Override
        public void updateSpawner(SpawnerUpgrade spawnerUpgrade) {
            MobSpawnerAbstract mobSpawnerAbstract = getSpawner().getSpawner();
            if (mobSpawnerAbstract instanceof NMSSpawners_v1_8_R2.StackedMobSpawner) {
                ((NMSSpawners_v1_8_R2.StackedMobSpawner) mobSpawnerAbstract).minSpawnDelay = spawnerUpgrade.getMinSpawnDelay();
                ((NMSSpawners_v1_8_R2.StackedMobSpawner) mobSpawnerAbstract).maxSpawnDelay = spawnerUpgrade.getMaxSpawnDelay();
                ((NMSSpawners_v1_8_R2.StackedMobSpawner) mobSpawnerAbstract).spawnCount = spawnerUpgrade.getSpawnCount();
                ((NMSSpawners_v1_8_R2.StackedMobSpawner) mobSpawnerAbstract).maxNearbyEntities = spawnerUpgrade.getMaxNearbyEntities();
                ((NMSSpawners_v1_8_R2.StackedMobSpawner) mobSpawnerAbstract).requiredPlayerRange = spawnerUpgrade.getRequiredPlayerRange();
                ((NMSSpawners_v1_8_R2.StackedMobSpawner) mobSpawnerAbstract).spawnRange = spawnerUpgrade.getSpawnRange();
                ((NMSSpawners_v1_8_R2.StackedMobSpawner) mobSpawnerAbstract).updateUpgrade(spawnerUpgrade.getId());
            } else {
                NBTTagCompound nbtTagCompound = new NBTTagCompound();
                mobSpawnerAbstract.b(nbtTagCompound);

                nbtTagCompound.setShort("MinSpawnDelay", (short) spawnerUpgrade.getMinSpawnDelay());
                nbtTagCompound.setShort("MaxSpawnDelay", (short) spawnerUpgrade.getMaxSpawnDelay());
                nbtTagCompound.setShort("SpawnCount", (short) spawnerUpgrade.getSpawnCount());
                nbtTagCompound.setShort("MaxNearbyEntities", (short) spawnerUpgrade.getMaxNearbyEntities());
                nbtTagCompound.setShort("RequiredPlayerRange", (short) spawnerUpgrade.getRequiredPlayerRange());
                nbtTagCompound.setShort("SpawnRange", (short) spawnerUpgrade.getSpawnRange());

                mobSpawnerAbstract.a(nbtTagCompound);
            }
        }

        @Override
        public SpawnerCachedData readData() {
            MobSpawnerAbstract mobSpawnerAbstract = getSpawner().getSpawner();
            if (mobSpawnerAbstract instanceof NMSSpawners_v1_8_R2.StackedMobSpawner) {
                NMSSpawners_v1_8_R2.StackedMobSpawner stackedMobSpawner = (NMSSpawners_v1_8_R2.StackedMobSpawner) mobSpawnerAbstract;
                return new SpawnerCachedData(
                        stackedMobSpawner.minSpawnDelay,
                        stackedMobSpawner.maxSpawnDelay,
                        stackedMobSpawner.spawnCount,
                        stackedMobSpawner.maxNearbyEntities,
                        stackedMobSpawner.requiredPlayerRange,
                        stackedMobSpawner.spawnRange,
                        stackedMobSpawner.spawnDelay / 20,
                        stackedMobSpawner.failureReason
                );
            } else {
                NBTTagCompound nbtTagCompound = new NBTTagCompound();
                mobSpawnerAbstract.b(nbtTagCompound);
                return new SpawnerCachedData(
                        nbtTagCompound.getShort("MinSpawnDelay"),
                        nbtTagCompound.getShort("MaxSpawnDelay"),
                        nbtTagCompound.getShort("SpawnCount"),
                        nbtTagCompound.getShort("MaxNearbyEntities"),
                        nbtTagCompound.getShort("RequiredPlayerRange"),
                        nbtTagCompound.getShort("SpawnRange"),
                        nbtTagCompound.getShort("Delay") / 20
                );
            }
        }

        TileEntityMobSpawner getSpawner() {
            return (TileEntityMobSpawner) world.getTileEntity(blockPosition);
        }

    }

}
