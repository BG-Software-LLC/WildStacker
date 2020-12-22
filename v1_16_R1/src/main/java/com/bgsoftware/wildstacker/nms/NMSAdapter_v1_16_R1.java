package com.bgsoftware.wildstacker.nms;

import com.bgsoftware.wildstacker.api.enums.SpawnCause;
import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.api.objects.StackedItem;
import com.bgsoftware.wildstacker.api.upgrades.SpawnerUpgrade;
import com.bgsoftware.wildstacker.objects.WStackedEntity;
import com.bgsoftware.wildstacker.objects.WStackedItem;
import com.bgsoftware.wildstacker.utils.legacy.Materials;
import com.bgsoftware.wildstacker.utils.reflection.Fields;
import com.bgsoftware.wildstacker.utils.reflection.Methods;
import com.bgsoftware.wildstacker.utils.spawners.SyncedCreatureSpawner;
import net.minecraft.server.v1_16_R1.AxisAlignedBB;
import net.minecraft.server.v1_16_R1.BlockPosition;
import net.minecraft.server.v1_16_R1.BlockRotatable;
import net.minecraft.server.v1_16_R1.ChatMessage;
import net.minecraft.server.v1_16_R1.Chunk;
import net.minecraft.server.v1_16_R1.ChunkProviderServer;
import net.minecraft.server.v1_16_R1.DamageSource;
import net.minecraft.server.v1_16_R1.DataWatcher;
import net.minecraft.server.v1_16_R1.DataWatcherObject;
import net.minecraft.server.v1_16_R1.DataWatcherRegistry;
import net.minecraft.server.v1_16_R1.DynamicOpsNBT;
import net.minecraft.server.v1_16_R1.EnchantmentManager;
import net.minecraft.server.v1_16_R1.Entity;
import net.minecraft.server.v1_16_R1.EntityAnimal;
import net.minecraft.server.v1_16_R1.EntityArmorStand;
import net.minecraft.server.v1_16_R1.EntityHuman;
import net.minecraft.server.v1_16_R1.EntityInsentient;
import net.minecraft.server.v1_16_R1.EntityItem;
import net.minecraft.server.v1_16_R1.EntityLiving;
import net.minecraft.server.v1_16_R1.EntityPlayer;
import net.minecraft.server.v1_16_R1.EntityPositionTypes;
import net.minecraft.server.v1_16_R1.EntityRaider;
import net.minecraft.server.v1_16_R1.EntityTurtle;
import net.minecraft.server.v1_16_R1.EntityTypes;
import net.minecraft.server.v1_16_R1.EntityVillager;
import net.minecraft.server.v1_16_R1.EntityZombieVillager;
import net.minecraft.server.v1_16_R1.EnumMobSpawn;
import net.minecraft.server.v1_16_R1.FluidTypes;
import net.minecraft.server.v1_16_R1.GameRules;
import net.minecraft.server.v1_16_R1.IBlockData;
import net.minecraft.server.v1_16_R1.IFluidContainer;
import net.minecraft.server.v1_16_R1.ItemStack;
import net.minecraft.server.v1_16_R1.ItemSword;
import net.minecraft.server.v1_16_R1.MathHelper;
import net.minecraft.server.v1_16_R1.MinecraftKey;
import net.minecraft.server.v1_16_R1.MobSpawnerAbstract;
import net.minecraft.server.v1_16_R1.NBTCompressedStreamTools;
import net.minecraft.server.v1_16_R1.NBTTagCompound;
import net.minecraft.server.v1_16_R1.NBTTagList;
import net.minecraft.server.v1_16_R1.PacketPlayOutCollect;
import net.minecraft.server.v1_16_R1.PacketPlayOutEntityMetadata;
import net.minecraft.server.v1_16_R1.PacketPlayOutSpawnEntity;
import net.minecraft.server.v1_16_R1.SaddleStorage;
import net.minecraft.server.v1_16_R1.SoundEffect;
import net.minecraft.server.v1_16_R1.TileEntityMobSpawner;
import net.minecraft.server.v1_16_R1.World;
import net.minecraft.server.v1_16_R1.WorldServer;
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
import org.bukkit.craftbukkit.v1_16_R1.CraftParticle;
import org.bukkit.craftbukkit.v1_16_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_16_R1.block.CraftBlockEntityState;
import org.bukkit.craftbukkit.v1_16_R1.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftAnimals;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftChicken;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftItem;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftStrider;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftTurtle;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftVillager;
import org.bukkit.craftbukkit.v1_16_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_16_R1.util.CraftMagicNumbers;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.MushroomCow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Strider;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityTransformEvent;
import org.bukkit.inventory.EntityEquipment;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@SuppressWarnings({"unused", "ConstantConditions"})
public final class NMSAdapter_v1_16_R1 implements NMSAdapter {

    private static final DataWatcherObject<Boolean> TURTLE_HAS_EGG = DataWatcher.a(EntityTurtle.class, DataWatcherRegistry.i);
    private static final DataWatcherObject<BlockPosition> TURTLE_HOME = DataWatcher.a(EntityTurtle.class, DataWatcherRegistry.l);

    /*
     *   Entity methods
     */

    @Override
    public <T extends org.bukkit.entity.Entity> T createEntity(Location location, Class<T> type, SpawnCause spawnCause, Consumer<T> beforeSpawnConsumer, Consumer<T> afterSpawnConsumer) {
        CraftWorld world = (CraftWorld) location.getWorld();

        assert world != null;

        Entity nmsEntity = world.createEntity(location, type);
        org.bukkit.entity.Entity bukkitEntity = nmsEntity.getBukkitEntity();

        if(beforeSpawnConsumer != null) {
            //noinspection unchecked
            beforeSpawnConsumer.accept((T) bukkitEntity);
        }

        world.addEntity(nmsEntity, spawnCause.toSpawnReason());

        WStackedEntity.of(bukkitEntity).setSpawnCause(spawnCause);

        if(afterSpawnConsumer != null) {
            //noinspection unchecked
            afterSpawnConsumer.accept((T) bukkitEntity);
        }

        return type.cast(bukkitEntity);
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

        if(entityTransformEvent.isCancelled())
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
        if(inLove)
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
        EntityInsentient entityLiving = (EntityInsentient) ((CraftLivingEntity) livingEntity).getHandle();

        int defaultEntityExp = Fields.ENTITY_EXP.get(entityLiving, Integer.class);
        int exp = entityLiving.getExpReward();

        Fields.ENTITY_EXP.set(entityLiving, defaultEntityExp);

        return exp;
    }

    @Override
    public boolean canDropExp(LivingEntity livingEntity){
        EntityLiving entityLiving = ((CraftLivingEntity) livingEntity).getHandle();
        int lastDamageByPlayerTime = Fields.ENTITY_LAST_DAMAGE_BY_PLAYER_TIME.get(entityLiving, Integer.class);
        boolean alwaysGivesExp = (boolean) Methods.ENTITY_ALWAYS_GIVES_EXP.invoke(entityLiving);
        boolean isDropExperience = (boolean) Methods.ENTITY_IS_DROP_EXPERIENCE.invoke(entityLiving);
        return !entityLiving.world.isClientSide && (lastDamageByPlayerTime > 0 || alwaysGivesExp) && isDropExperience && entityLiving.world.getGameRules().getBoolean(GameRules.DO_MOB_LOOT);
    }

    @Override
    public void updateLastDamageTime(LivingEntity livingEntity) {
        EntityLiving entityLiving = ((CraftLivingEntity) livingEntity).getHandle();
        Fields.ENTITY_LAST_DAMAGE_BY_PLAYER_TIME.set(entityLiving, 100);
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
        return ((CraftChicken) chicken).getHandle().eggLayTime;
    }

    @Override
    public void setNerfedEntity(LivingEntity livingEntity, boolean nerfed) {
        EntityInsentient entityLiving = (EntityInsentient) ((CraftLivingEntity) livingEntity).getHandle();
        try { entityLiving.aware = !nerfed; } catch(Throwable ignored){ }

        if(Fields.ENTITY_FROM_MOB_SPAWNER.exists())
            Fields.ENTITY_FROM_MOB_SPAWNER.set(entityLiving, nerfed);

        if(Fields.ENTITY_SPAWNED_VIA_MOB_SPAWNER.exists())
            Fields.ENTITY_SPAWNED_VIA_MOB_SPAWNER.set(entityLiving, nerfed);
    }

    @Override
    public void setKiller(LivingEntity livingEntity, Player killer) {
        EntityLiving entityLiving = ((CraftLivingEntity) livingEntity).getHandle();
        entityLiving.killer = killer == null ? null : ((CraftPlayer) killer).getHandle();
    }

    @Override
    public boolean canSpawnOn(org.bukkit.entity.Entity bukkitEntity, Location location) {
        assert location.getWorld() != null;
        World world = ((CraftWorld) location.getWorld()).getHandle();
        EntityTypes<?> entityTypes = ((CraftEntity) bukkitEntity).getHandle().getEntityType();
        return EntityPositionTypes.a(entityTypes, world.getMinecraftWorld(), EnumMobSpawn.SPAWNER, new BlockPosition(location.getX(), location.getY(), location.getZ()), world.getRandom());
    }

    @Override
    public List<org.bukkit.entity.Entity> getNearbyEntities(Location location, int range, Predicate<org.bukkit.entity.Entity> filter) {
        List<org.bukkit.entity.Entity> entities = new ArrayList<>();

        World world = ((CraftWorld) location.getWorld()).getHandle();

        AxisAlignedBB axisAlignedBB = new AxisAlignedBB(location.getX() - range, location.getY() - range,
                location.getZ() - range, location.getX() + range, location.getY() + range, location.getZ() + range);

        int minX = MathHelper.floor((axisAlignedBB.minX - 2) / 16.0D);
        int minY = MathHelper.floor((axisAlignedBB.minY - 2) / 16.0D);
        int minZ = MathHelper.floor((axisAlignedBB.minZ - 2) / 16.0D);
        int maxX = MathHelper.floor((axisAlignedBB.maxX + 2) / 16.0D);
        int maxY = MathHelper.floor((axisAlignedBB.maxY + 2) / 16.0D);
        int maxZ = MathHelper.floor((axisAlignedBB.maxZ + 2) / 16.0D);

        for(int x = minX; x <= maxX; x++) {
            for(int z = minZ; z <= maxZ; z++) {
                Chunk chunk;

                if(Methods.WORLD_GET_CHUNK_IF_LOADED_PAPER.isValid()){
                    chunk = (Chunk) Methods.WORLD_GET_CHUNK_IF_LOADED_PAPER.invoke(world, x, z);
                }
                else{
                    chunk = world.getChunkProvider().getChunkAt(x, z, false);
                }

                if(chunk != null) {
                    int chunkMinY = MathHelper.clamp(minY, 0, chunk.entitySlices.length - 1);
                    int chunkMaxY = MathHelper.clamp(maxY, 0, chunk.entitySlices.length - 1);

                    for(int y = chunkMinY; y <= chunkMaxY; y++){
                        List<Entity> entitySlice = chunk.entitySlices[y];
                        if(entitySlice != null) {
                            entities.addAll(entitySlice.stream().filter(entity -> axisAlignedBB.e(entity.locX(), entity.locY(), entity.locZ()) &&
                                    (filter == null || filter.test(entity.getBukkitEntity())))
                                    .map(Entity::getBukkitEntity).collect(Collectors.toList()));
                        }
                    }
                }
            }
        }

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
        return nmsItem != null && nmsItem.e();
    }

    @Override
    public boolean doesStriderHaveSaddle(Strider strider) {
        return ((CraftStrider) strider).getHandle().hasSaddle();
    }

    @Override
    public void removeStriderSaddle(Strider strider) {
        SaddleStorage saddleStorage = Fields.STRIDER_SADDLE_STORAGE.get(((CraftStrider) strider).getHandle(), SaddleStorage.class);
        saddleStorage.setSaddle(false);
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
        ((CraftTurtle) turtle).getHandle().getDataWatcher().set(TURTLE_HAS_EGG, true);
    }

    @Override
    public Location getTurtleHome(org.bukkit.entity.Entity turtle) {
        BlockPosition homePosition = ((CraftTurtle) turtle).getHandle().getDataWatcher().get(TURTLE_HOME);
        return new Location(turtle.getWorld(), homePosition.getX(), homePosition.getY(), homePosition.getZ());
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
        if(usedItem.getType() == Material.AIR || !(CraftItemStack.asNMSCopy(usedItem).getItem() instanceof ItemSword))
            return;

        float sweepDamage = 1.0F + EnchantmentManager.a(entityHuman) * (float) damage;
        List<EntityLiving> nearbyEntities = targetLiving.world.a(EntityLiving.class, targetLiving.getBoundingBox().grow(1.0D, 0.25D, 1.0D));

        for(EntityLiving nearby : nearbyEntities){
            if(nearby != targetLiving && nearby != entityHuman && !entityHuman.r(nearby) && (!(nearby instanceof EntityArmorStand) || !((EntityArmorStand) nearby).isMarker()) && entityHuman.h(nearby) < 9.0D){
                nearby.damageEntity(DamageSource.playerAttack(entityHuman).sweep(), sweepDamage);
            }
        }
    }

    /*
     *   Spawner methods
     */

    @Override
    public SyncedCreatureSpawner createSyncedSpawner(CreatureSpawner creatureSpawner) {
        return new SyncedCreatureSpawnerImpl(creatureSpawner.getBlock());
    }

    @Override
    public boolean isRotatable(Block block) {
        World world = ((CraftWorld) block.getWorld()).getHandle();
        return world.getType(new BlockPosition(block.getX(), block.getY(), block.getZ())).getBlock() instanceof BlockRotatable;
    }

    /*
     *   Item methods
     */

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
        };
    }

    @Override
    public org.bukkit.inventory.ItemStack getPlayerSkull(String texture) {
        ItemStack itemStack = CraftItemStack.asNMSCopy(Materials.PLAYER_HEAD.toBukkitItem());
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

    /*
     *   World methods
     */

    @Override
    public void grandAchievement(Player player, EntityType victim, String name) {
        grandAchievement(player, victim.getKey().toString(), name);
    }

    @Override
    public void grandAchievement(Player player, String criteria, String name) {
        Advancement advancement = Bukkit.getAdvancement(NamespacedKey.minecraft(name));

        if(advancement == null)
            throw new NullPointerException("Invalid advancement " + name);

        AdvancementProgress advancementProgress = player.getAdvancementProgress(advancement);

        if(!advancementProgress.isDone()){
            advancementProgress.awardCriteria(criteria);
        }
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
        Object soundEffect = Methods.ENTITY_SOUND_DEATH.invoke(entityLiving);
        if (soundEffect != null) {
            float soundVolume = (float) Methods.ENTITY_SOUND_VOLUME.invoke(entityLiving);
            float soundPitch = (float) Methods.ENTITY_SOUND_PITCH.invoke(entityLiving);
            entityLiving.playSound((SoundEffect) soundEffect, soundVolume, soundPitch);
        }
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
        if(entityRaider.fc())
            entityRaider.fb().a((Entity) ((CraftPlayer) player).getHandle());
    }

    @Override
    public boolean attemptToWaterLog(Block block) {
        World world = ((CraftWorld) block.getWorld()).getHandle();
        BlockPosition blockPosition = ((CraftBlock) block).getPosition();
        IBlockData blockData = ((CraftBlock) block).getNMS();

        if(blockData.getBlock() instanceof IFluidContainer) {
            ((IFluidContainer) blockData.getBlock()).place(world, blockPosition, blockData, FluidTypes.WATER.a(false));
            return true;
        }

        return false;
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
        if(targetBukkit instanceof Zombie)
            ((Zombie) targetBukkit).setBaby(nbtTagCompound.hasKey("IsBaby") && nbtTagCompound.getBoolean("IsBaby"));

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
        }catch(Exception ex){
            return null;
        }

        return new BigInteger(1, outputStream.toByteArray()).toString(32);
    }

    @Override
    public org.bukkit.inventory.ItemStack deserialize(String serialized) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(new BigInteger(serialized, 32).toByteArray());

        try {
            NBTTagCompound nbtTagCompoundRoot = NBTCompressedStreamTools.a(new DataInputStream(inputStream));

            ItemStack nmsItem = ItemStack.a(nbtTagCompoundRoot);

            return CraftItemStack.asBukkitCopy(nmsItem);
        }catch(Exception ex){
            return null;
        }
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public org.bukkit.inventory.ItemStack setTag(org.bukkit.inventory.ItemStack itemStack, String key, Object value) {
        ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound tagCompound = nmsItem.hasTag() ? nmsItem.getTag() : new NBTTagCompound();

        if(value instanceof Boolean)
            tagCompound.setBoolean(key, (boolean) value);
        else if(value instanceof Integer)
            tagCompound.setInt(key, (int) value);
        else if(value instanceof String)
            tagCompound.setString(key, (String) value);
        else if(value instanceof Double)
            tagCompound.setDouble(key, (double) value);
        else if(value instanceof Short)
            tagCompound.setShort(key, (short) value);
        else if(value instanceof Byte)
            tagCompound.setByte(key, (byte) value);
        else if(value instanceof Float)
            tagCompound.setFloat(key, (float) value);
        else if(value instanceof Long)
            tagCompound.setLong(key, (long) value);

        nmsItem.setTag(tagCompound);

        return CraftItemStack.asBukkitCopy(nmsItem);
    }

    @Override
    public <T> T getTag(org.bukkit.inventory.ItemStack itemStack, String key, Class<T> valueType, Object def) {
        ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);

        if(nmsItem == null)
            return valueType.cast(def);

        NBTTagCompound tagCompound = nmsItem.hasTag() ? nmsItem.getTag() : new NBTTagCompound();

        if(tagCompound != null) {
            if(!tagCompound.hasKey(key))
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

    /*
     *   Data methods
     */

    @Override
    public void saveEntity(StackedEntity stackedEntity) {
        EntityLiving entityLiving = ((CraftLivingEntity) stackedEntity.getLivingEntity()).getHandle();

        // Removing old tags
        entityLiving.getScoreboardTags().removeIf(tag -> tag.startsWith("ws:"));

        entityLiving.addScoreboardTag("ws:stack-amount=" + stackedEntity.getStackAmount());
        entityLiving.addScoreboardTag("ws:stack-cause=" + stackedEntity.getSpawnCause().name());
        if(stackedEntity.hasNameTag())
            entityLiving.addScoreboardTag("ws:name-tag=true");
        int upgradeId = ((WStackedEntity) stackedEntity).getUpgradeId();
        if(upgradeId != 0)
            entityLiving.addScoreboardTag("ws:upgrade=" + upgradeId);
    }

    @Override
    public void loadEntity(StackedEntity stackedEntity) {
        EntityLiving entityLiving = ((CraftLivingEntity) stackedEntity.getLivingEntity()).getHandle();
        for(String scoreboardTag : entityLiving.getScoreboardTags()){
            if(scoreboardTag.startsWith("ws:")) {
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

    @Override
    public void saveItem(StackedItem stackedItem) {
        EntityItem entityItem = (EntityItem) ((CraftItem) stackedItem.getItem()).getHandle();

        // Removing old tags
        entityItem.getScoreboardTags().removeIf(tag -> tag.startsWith("ws:"));

        entityItem.addScoreboardTag("ws:stack-amount=" + stackedItem.getStackAmount());
    }

    @Override
    public void loadItem(StackedItem stackedItem) {
        EntityItem entityItem = (EntityItem) ((CraftItem) stackedItem.getItem()).getHandle();
        for(String scoreboardTag : entityItem.getScoreboardTags()){
            if(scoreboardTag.startsWith("ws:")) {
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

    @SuppressWarnings({"deprecation", "NullableProblems"})
    private static class SyncedCreatureSpawnerImpl extends CraftBlockEntityState<TileEntityMobSpawner> implements SyncedCreatureSpawner {

        private final World world;
        private final BlockPosition blockPosition;
        private final Location blockLocation;

        SyncedCreatureSpawnerImpl(Block block){
            super(block, TileEntityMobSpawner.class);
            world = ((CraftWorld) block.getWorld()).getHandle();
            blockPosition = new BlockPosition(block.getX(), block.getY(), block.getZ());
            blockLocation = block.getLocation();
        }

        @Override
        public EntityType getSpawnedType() {
            try {
                MinecraftKey key = getSpawner().getSpawner().getMobName();
                EntityType entityType = key == null ? EntityType.PIG : EntityType.fromName(key.getKey());
                return entityType == null ? EntityType.PIG : entityType;
            }catch(Exception ex){
                return EntityType.PIG;
            }
        }

        @Override
        public void setSpawnedType(EntityType entityType) {
            if (entityType != null && entityType.getName() != null) {
                getSpawner().getSpawner().setMobName(EntityTypes.a(entityType.getName()).orElse(EntityTypes.PIG));
            } else {
                throw new IllegalArgumentException("Can't spawn EntityType " + entityType + " from mobspawners!");
            }
        }

        @Override
        public void setCreatureTypeByName(String s) {
            EntityType entityType = EntityType.fromName(s);
            if(entityType != null && entityType != EntityType.UNKNOWN)
                setSpawnedType(entityType);
        }

        @Override
        public String getCreatureTypeName() {
            MinecraftKey key = getSpawner().getSpawner().getMobName();
            return key == null ? "PIG" : key.getKey();
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
        public int getMinSpawnDelay() {
            return getSpawner().getSpawner().minSpawnDelay;
        }

        @Override
        public void setMinSpawnDelay(int i) {
            getSpawner().getSpawner().minSpawnDelay = i;
        }

        @Override
        public int getMaxSpawnDelay() {
            return getSpawner().getSpawner().maxSpawnDelay;
        }

        @Override
        public void setMaxSpawnDelay(int i) {
            getSpawner().getSpawner().maxSpawnDelay = i;
        }

        @Override
        public int getSpawnCount() {
            return getSpawner().getSpawner().spawnCount;
        }

        @Override
        public void setSpawnCount(int i) {
            getSpawner().getSpawner().spawnCount = i;
        }

        @Override
        public int getMaxNearbyEntities() {
            return getSpawner().getSpawner().maxNearbyEntities;
        }

        @Override
        public void setMaxNearbyEntities(int i) {
            getSpawner().getSpawner().maxNearbyEntities = i;
        }

        @Override
        public int getRequiredPlayerRange() {
            return getSpawner().getSpawner().requiredPlayerRange;
        }

        @Override
        public void setRequiredPlayerRange(int i) {
            getSpawner().getSpawner().requiredPlayerRange = i;
        }

        @Override
        public int getSpawnRange() {
            return getSpawner().getSpawner().spawnRange;
        }

        @Override
        public void setSpawnRange(int i) {
            getSpawner().getSpawner().spawnRange = i;
        }

        @Override
        public void updateSpawner(SpawnerUpgrade spawnerUpgrade) {
            MobSpawnerAbstract mobSpawnerAbstract = getSpawner().getSpawner();
            mobSpawnerAbstract.minSpawnDelay = spawnerUpgrade.getMinSpawnDelay();
            mobSpawnerAbstract.maxSpawnDelay = spawnerUpgrade.getMaxSpawnDelay();
            mobSpawnerAbstract.spawnCount = spawnerUpgrade.getSpawnCount();
            mobSpawnerAbstract.maxNearbyEntities = spawnerUpgrade.getMaxNearbyEntities();
            mobSpawnerAbstract.requiredPlayerRange = spawnerUpgrade.getRequiredPlayerRange();
            mobSpawnerAbstract.spawnRange = spawnerUpgrade.getSpawnRange();
        }

        @Override
        public boolean update(boolean force, boolean applyPhysics) {
            return blockLocation.getBlock().getState().update(force, applyPhysics);
        }

        TileEntityMobSpawner getSpawner(){
            return (TileEntityMobSpawner) world.getTileEntity(blockPosition);
        }

    }

}
