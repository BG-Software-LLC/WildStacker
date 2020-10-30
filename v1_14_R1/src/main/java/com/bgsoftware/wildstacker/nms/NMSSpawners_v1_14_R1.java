package com.bgsoftware.wildstacker.nms;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.enums.StackCheckResult;
import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.api.objects.StackedSpawner;
import com.bgsoftware.wildstacker.objects.WStackedEntity;
import com.bgsoftware.wildstacker.objects.WStackedSpawner;
import com.bgsoftware.wildstacker.utils.GeneralUtils;
import com.bgsoftware.wildstacker.utils.entity.EntityUtils;
import net.minecraft.server.v1_14_R1.AxisAlignedBB;
import net.minecraft.server.v1_14_R1.BlockPosition;
import net.minecraft.server.v1_14_R1.Blocks;
import net.minecraft.server.v1_14_R1.Entity;
import net.minecraft.server.v1_14_R1.EntityInsentient;
import net.minecraft.server.v1_14_R1.EntityTypes;
import net.minecraft.server.v1_14_R1.EnumMobSpawn;
import net.minecraft.server.v1_14_R1.MobSpawnerAbstract;
import net.minecraft.server.v1_14_R1.NBTTagCompound;
import net.minecraft.server.v1_14_R1.Particles;
import net.minecraft.server.v1_14_R1.TileEntity;
import net.minecraft.server.v1_14_R1.TileEntityMobSpawner;
import net.minecraft.server.v1_14_R1.WeightedRandom;
import net.minecraft.server.v1_14_R1.World;
import net.minecraft.server.v1_14_R1.WorldServer;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_14_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_14_R1.event.CraftEventFactory;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("unused")
public final class NMSSpawners_v1_14_R1 implements NMSSpawners {

    private static final WildStackerPlugin plugin = WildStackerPlugin.getPlugin();
    private static Field MOB_SPAWNER_ABSTRACT_FIELD = null;

    static {
        try{
            MOB_SPAWNER_ABSTRACT_FIELD = TileEntityMobSpawner.class.getDeclaredField("a");
            MOB_SPAWNER_ABSTRACT_FIELD.setAccessible(true);

            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(MOB_SPAWNER_ABSTRACT_FIELD, MOB_SPAWNER_ABSTRACT_FIELD.getModifiers() & ~Modifier.FINAL);
        }catch (Exception ignored){}
    }

    @Override
    public void updateStackedSpawner(StackedSpawner stackedSpawner) {
        World world = ((CraftWorld) stackedSpawner.getWorld()).getHandle();
        Location location = stackedSpawner.getLocation();

        TileEntity tileEntity = world.getTileEntity(new BlockPosition(location.getX(), location.getY(), location.getZ()));
        if(tileEntity instanceof TileEntityMobSpawner)
            new StackedMobSpawner((TileEntityMobSpawner) tileEntity, stackedSpawner);
    }

    private static class StackedMobSpawner extends MobSpawnerAbstract {

        private final World world;
        private final BlockPosition position;
        private final WeakReference<WStackedSpawner> stackedSpawner;

        StackedMobSpawner(TileEntityMobSpawner tileEntityMobSpawner, StackedSpawner stackedSpawner){
            this.world = tileEntityMobSpawner.getWorld();
            this.position = tileEntityMobSpawner.getPosition();
            this.stackedSpawner = new WeakReference<>((WStackedSpawner) stackedSpawner);

            if(!(tileEntityMobSpawner.getSpawner() instanceof StackedMobSpawner)) {
                try {
                    MOB_SPAWNER_ABSTRACT_FIELD.set(tileEntityMobSpawner, this);
                }catch (Exception ignored){}
            }
        }

        @Override
        public void a(int i) {
            world.playBlockAction(position, Blocks.SPAWNER, i, 0);
        }

        @Override
        public World a() {
            return world;
        }

        @Override
        public BlockPosition b() {
            return position;
        }

        @Override
        public void c() {
            WStackedSpawner stackedSpawner = this.stackedSpawner.get();

            if(stackedSpawner == null){
                super.c();
                return;
            }

            if(!hasNearbyPlayers())
                return;

            if(world.isClientSide){
                double x = position.getX() + world.random.nextFloat();
                double y = position.getY() + world.random.nextFloat();
                double z = position.getZ() + world.random.nextFloat();
                world.addParticle(Particles.SMOKE, x, y, z, 0.0D, 0.0D, 0.0D);
                world.addParticle(Particles.FLAME, x, y, z, 0.0D, 0.0D, 0.0D);
                if (this.spawnDelay > 0) {
                    --this.spawnDelay;
                }

                return;
            }

            if (this.spawnDelay == -1)
                resetSpawnDelay();

            if (this.spawnDelay > 0) {
                --this.spawnDelay;
                return;
            }

            org.bukkit.entity.Entity demoEntityBukkit = generateEntity(position.getX(), position.getY(), position.getZ(), false);

            if(demoEntityBukkit == null){
                resetSpawnDelay();
                return;
            }

            if(!EntityUtils.isStackable(demoEntityBukkit)){
                super.c();
                return;
            }

            StackedEntity demoEntity = WStackedEntity.of(demoEntityBukkit);
            ((WStackedEntity) demoEntity).setDemoEntity();

            Entity demoNMSEntity = ((CraftEntity) demoEntityBukkit).getHandle();
            ((WorldServer) world).unregisterEntity(demoNMSEntity);

            int stackAmount = stackedSpawner.getStackAmount(), spawnCount = this.spawnCount * stackAmount;

            List<? extends Entity> nearbyEntities = world.a(demoNMSEntity.getClass(), new AxisAlignedBB(
                    position.getX(), position.getY(), position.getZ(),
                    position.getX() + 1, position.getY() + 1, position.getZ() + 1
            ).g(this.spawnRange));

            StackedEntity targetEntity = getTargetEntity(stackedSpawner, demoEntity, nearbyEntities);

            if (targetEntity == null && nearbyEntities.size() >= this.maxNearbyEntities) {
                resetSpawnDelay();
                return;
            }

            spawnCount = stackAmount + world.random.nextInt(spawnCount - stackAmount + 1);

            int amountPerEntity = 1;
            int mobsToSpawn;

            boolean resetDelay = false;

            // Try stacking into the target entity first
            if(targetEntity != null){
                int limit = targetEntity.getStackLimit();
                int newStackAmount = targetEntity.getStackAmount() + spawnCount;

                if(newStackAmount > limit) {
                    mobsToSpawn = limit - targetEntity.getStackAmount();
                    newStackAmount = limit;
                }
                else{
                    mobsToSpawn = 0;
                }

                targetEntity.setStackAmount(newStackAmount, true);

                if(plugin.getSettings().linkedEntitiesEnabled && targetEntity.getLivingEntity() != stackedSpawner.getLinkedEntity())
                    stackedSpawner.setLinkedEntity(targetEntity.getLivingEntity());

                world.triggerEffect(2004, position, 0);

                resetDelay = true;
            }
            else{
                mobsToSpawn = spawnCount;
            }

            if(mobsToSpawn > 0){
                amountPerEntity = !demoEntity.isCached() ? 1 : Math.min(mobsToSpawn, demoEntity.getStackLimit());
                mobsToSpawn = mobsToSpawn / amountPerEntity;
            }

            for(int i = 0; i < mobsToSpawn; i++) {
                double x = position.getX() + (world.random.nextDouble() - world.random.nextDouble()) * spawnRange + 0.5D;
                double y = position.getY() + world.random.nextInt(3) - 1;
                double z = position.getZ() + (world.random.nextDouble() - world.random.nextDouble()) * spawnRange + 0.5D;

                org.bukkit.entity.Entity bukkitEntity = generateEntity(x, y, z, true);

                if (bukkitEntity == null) {
                    resetSpawnDelay();
                    return;
                }

                if(handleEntitySpawn(bukkitEntity, amountPerEntity))
                    resetDelay = true;
            }

            if(resetDelay)
                resetSpawnDelay();
        }

        private boolean hasNearbyPlayers(){
            return world.isPlayerNearby(position.getX() + 0.5D, position.getY() + 0.5D,
                    position.getZ() + 0.5D, requiredPlayerRange);
        }

        private void resetSpawnDelay(){
            if (maxSpawnDelay <= minSpawnDelay) {
                spawnDelay = minSpawnDelay;
            } else {
                spawnDelay = minSpawnDelay + world.random.nextInt(maxSpawnDelay - minSpawnDelay);
            }

            if (!this.mobs.isEmpty()) {
                setSpawnData(WeightedRandom.a(this.a().random, this.mobs));
            }

            a(1);
        }

        private org.bukkit.entity.Entity generateEntity(double x, double y, double z, boolean rotation){
            NBTTagCompound entityCompound = this.spawnData.getEntity();
            Entity entity = EntityTypes.a(entityCompound, world, _entity -> {
                _entity.setPositionRotation(x, y, z, 0f, 0f);

                if(rotation)
                    _entity.yaw = world.random.nextFloat() * 360.0F;

                _entity.world = world;
                _entity.valid = true;
                _entity.dead = false;

                return _entity;
            });
            return entity == null ? null : entity.getBukkitEntity();
        }

        private boolean handleEntitySpawn(org.bukkit.entity.Entity bukkitEntity, int amountPerEntity){
            Entity entity = ((CraftEntity) bukkitEntity).getHandle();
            StackedEntity stackedEntity = null;

            if(amountPerEntity > 1) {
                stackedEntity = WStackedEntity.of(bukkitEntity);
                stackedEntity.setStackAmount(amountPerEntity, true);
            }

            List<? extends Entity> nearbyEntities = world.a(entity.getClass(), new AxisAlignedBB(position.getX(), position.getY(), position.getZ(),
                    position.getX() + 1, position.getY() + 1, position.getZ() + 1).g(this.spawnRange));

            if (nearbyEntities.size() >= this.maxNearbyEntities)
                return true;

            if (entity instanceof EntityInsentient) {
                EntityInsentient entityinsentient = (EntityInsentient)entity;
                if (!entityinsentient.a(world, EnumMobSpawn.SPAWNER) || !entityinsentient.a(world)) {
                    return false;
                }

                if (this.spawnData.getEntity().d() == 1 && this.spawnData.getEntity().hasKeyOfType("id", 8)) {
                    ((EntityInsentient)entity).prepare(world, world.getDamageScaler(new BlockPosition(entity)), EnumMobSpawn.SPAWNER, null, null);
                }

                if (entityinsentient.world.spigotConfig.nerfSpawnerMobs) {
                    entityinsentient.fromMobSpawner = true;
                }
            }

            if (CraftEventFactory.callSpawnerSpawnEvent(entity, position).isCancelled()) {
                Entity vehicle = entity.getVehicle();
                if (vehicle != null) {
                    vehicle.dead = true;
                }
                for(Entity passenger : entity.getAllPassengers())
                    passenger.dead = true;
                if(stackedEntity != null)
                    plugin.getSystemManager().removeStackObject(stackedEntity);
            }

            else {
                addEntity(entity);

                world.triggerEffect(2004, position, 0);

                if (entity instanceof EntityInsentient) {
                    ((EntityInsentient)entity).doSpawnEffect();
                }

                return true;
            }

            return false;
        }

        private void addEntity(Entity entity) {
            ((WorldServer) world).unregisterEntity(entity);
            if (world.addEntity(entity, CreatureSpawnEvent.SpawnReason.SPAWNER))
                entity.getPassengers().forEach(this::addEntity);
        }

        private StackedEntity getTargetEntity(StackedSpawner stackedSpawner, StackedEntity demoEntity,
                                              List<? extends Entity> nearbyEntities){
            LivingEntity linkedEntity = stackedSpawner.getLinkedEntity();

            if(linkedEntity != null)
                return WStackedEntity.of(linkedEntity);

            Optional<CraftEntity> closestEntity = GeneralUtils.getClosestBukkit(stackedSpawner.getLocation(),
                    nearbyEntities.stream().map(Entity::getBukkitEntity).filter(entity ->
                            EntityUtils.isStackable(entity) &&
                                    demoEntity.runStackCheck(WStackedEntity.of(entity)) == StackCheckResult.SUCCESS));

            return closestEntity.map(WStackedEntity::of).orElse(null);
        }

    }

}
