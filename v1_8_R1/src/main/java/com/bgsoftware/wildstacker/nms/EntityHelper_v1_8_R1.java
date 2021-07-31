package com.bgsoftware.wildstacker.nms;

import net.minecraft.server.v1_8_R1.BlockPosition;
import net.minecraft.server.v1_8_R1.EntityArmorStand;
import net.minecraft.server.v1_8_R1.EntityBat;
import net.minecraft.server.v1_8_R1.EntityBlaze;
import net.minecraft.server.v1_8_R1.EntityCaveSpider;
import net.minecraft.server.v1_8_R1.EntityChicken;
import net.minecraft.server.v1_8_R1.EntityCow;
import net.minecraft.server.v1_8_R1.EntityCreeper;
import net.minecraft.server.v1_8_R1.EntityEnderDragon;
import net.minecraft.server.v1_8_R1.EntityEnderman;
import net.minecraft.server.v1_8_R1.EntityEndermite;
import net.minecraft.server.v1_8_R1.EntityGhast;
import net.minecraft.server.v1_8_R1.EntityGiantZombie;
import net.minecraft.server.v1_8_R1.EntityGuardian;
import net.minecraft.server.v1_8_R1.EntityHorse;
import net.minecraft.server.v1_8_R1.EntityInsentient;
import net.minecraft.server.v1_8_R1.EntityIronGolem;
import net.minecraft.server.v1_8_R1.EntityMagmaCube;
import net.minecraft.server.v1_8_R1.EntityMushroomCow;
import net.minecraft.server.v1_8_R1.EntityOcelot;
import net.minecraft.server.v1_8_R1.EntityPig;
import net.minecraft.server.v1_8_R1.EntityPigZombie;
import net.minecraft.server.v1_8_R1.EntityRabbit;
import net.minecraft.server.v1_8_R1.EntitySheep;
import net.minecraft.server.v1_8_R1.EntitySilverfish;
import net.minecraft.server.v1_8_R1.EntitySkeleton;
import net.minecraft.server.v1_8_R1.EntitySlime;
import net.minecraft.server.v1_8_R1.EntitySnowman;
import net.minecraft.server.v1_8_R1.EntitySpider;
import net.minecraft.server.v1_8_R1.EntitySquid;
import net.minecraft.server.v1_8_R1.EntityVillager;
import net.minecraft.server.v1_8_R1.EntityWitch;
import net.minecraft.server.v1_8_R1.EntityWither;
import net.minecraft.server.v1_8_R1.EntityWolf;
import net.minecraft.server.v1_8_R1.EntityZombie;
import net.minecraft.server.v1_8_R1.World;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R1.CraftWorld;
import org.bukkit.entity.Ambient;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Bat;
import org.bukkit.entity.Blaze;
import org.bukkit.entity.CaveSpider;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.ComplexLivingEntity;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Endermite;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.Giant;
import org.bukkit.entity.Golem;
import org.bukkit.entity.Guardian;
import org.bukkit.entity.Horse;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.MagmaCube;
import org.bukkit.entity.MushroomCow;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Pig;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Player;
import org.bukkit.entity.Rabbit;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Silverfish;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Snowman;
import org.bukkit.entity.Spider;
import org.bukkit.entity.Squid;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Witch;
import org.bukkit.entity.Wither;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.CreatureSpawnEvent;

public final class EntityHelper_v1_8_R1 {

    static net.minecraft.server.v1_8_R1.Entity createEntity(Location location, Class<? extends Entity> clazz) throws IllegalArgumentException {
        if (location != null && clazz != null) {
            World world = ((CraftWorld) location.getWorld()).getHandle();
            net.minecraft.server.v1_8_R1.Entity entity = null;
            double x = location.getX();
            double y = location.getY();
            double z = location.getZ();
            float pitch = location.getPitch();
            float yaw = location.getYaw();
            if (LivingEntity.class.isAssignableFrom(clazz)) {
                if (Chicken.class.isAssignableFrom(clazz)) {
                    entity = new EntityChicken(world);
                } else if (Cow.class.isAssignableFrom(clazz)) {
                    if (MushroomCow.class.isAssignableFrom(clazz)) {
                        entity = new EntityMushroomCow(world);
                    } else {
                        entity = new EntityCow(world);
                    }
                } else if (Golem.class.isAssignableFrom(clazz)) {
                    if (Snowman.class.isAssignableFrom(clazz)) {
                        entity = new EntitySnowman(world);
                    } else if (IronGolem.class.isAssignableFrom(clazz)) {
                        entity = new EntityIronGolem(world);
                    }
                } else if (Creeper.class.isAssignableFrom(clazz)) {
                    entity = new EntityCreeper(world);
                } else if (Ghast.class.isAssignableFrom(clazz)) {
                    entity = new EntityGhast(world);
                } else if (Pig.class.isAssignableFrom(clazz)) {
                    entity = new EntityPig(world);
                } else if (!Player.class.isAssignableFrom(clazz)) {
                    if (Sheep.class.isAssignableFrom(clazz)) {
                        entity = new EntitySheep(world);
                    } else if (Horse.class.isAssignableFrom(clazz)) {
                        entity = new EntityHorse(world);
                    } else if (Skeleton.class.isAssignableFrom(clazz)) {
                        entity = new EntitySkeleton(world);
                    } else if (Slime.class.isAssignableFrom(clazz)) {
                        if (MagmaCube.class.isAssignableFrom(clazz)) {
                            entity = new EntityMagmaCube(world);
                        } else {
                            entity = new EntitySlime(world);
                        }
                    } else if (Spider.class.isAssignableFrom(clazz)) {
                        if (CaveSpider.class.isAssignableFrom(clazz)) {
                            entity = new EntityCaveSpider(world);
                        } else {
                            entity = new EntitySpider(world);
                        }
                    } else if (Squid.class.isAssignableFrom(clazz)) {
                        entity = new EntitySquid(world);
                    } else if (Tameable.class.isAssignableFrom(clazz)) {
                        if (Wolf.class.isAssignableFrom(clazz)) {
                            entity = new EntityWolf(world);
                        } else if (Ocelot.class.isAssignableFrom(clazz)) {
                            entity = new EntityOcelot(world);
                        }
                    } else if (PigZombie.class.isAssignableFrom(clazz)) {
                        entity = new EntityPigZombie(world);
                    } else if (Zombie.class.isAssignableFrom(clazz)) {
                        entity = new EntityZombie(world);
                    } else if (Giant.class.isAssignableFrom(clazz)) {
                        entity = new EntityGiantZombie(world);
                    } else if (Silverfish.class.isAssignableFrom(clazz)) {
                        entity = new EntitySilverfish(world);
                    } else if (Enderman.class.isAssignableFrom(clazz)) {
                        entity = new EntityEnderman(world);
                    } else if (Blaze.class.isAssignableFrom(clazz)) {
                        entity = new EntityBlaze(world);
                    } else if (Villager.class.isAssignableFrom(clazz)) {
                        entity = new EntityVillager(world);
                    } else if (Witch.class.isAssignableFrom(clazz)) {
                        entity = new EntityWitch(world);
                    } else if (Wither.class.isAssignableFrom(clazz)) {
                        entity = new EntityWither(world);
                    } else if (ComplexLivingEntity.class.isAssignableFrom(clazz)) {
                        if (EnderDragon.class.isAssignableFrom(clazz)) {
                            entity = new EntityEnderDragon(world);
                        }
                    } else if (Ambient.class.isAssignableFrom(clazz)) {
                        if (Bat.class.isAssignableFrom(clazz)) {
                            entity = new EntityBat(world);
                        }
                    } else if (Rabbit.class.isAssignableFrom(clazz)) {
                        entity = new EntityRabbit(world);
                    } else if (Endermite.class.isAssignableFrom(clazz)) {
                        entity = new EntityEndermite(world);
                    } else if (Guardian.class.isAssignableFrom(clazz)) {
                        entity = new EntityGuardian(world);
                    } else if (ArmorStand.class.isAssignableFrom(clazz)) {
                        entity = new EntityArmorStand(world, x, y, z);
                    }
                }

                if (entity != null) {
                    entity.setLocation(x, y, z, yaw, pitch);
                }
            }

            if (entity != null) {
                if (entity instanceof EntityOcelot) {
                    ((EntityOcelot) entity).spawnBonus = false;
                }

                return entity;
            } else {
                throw new IllegalArgumentException("Cannot spawn an entity for " + clazz.getName());
            }
        } else {
            throw new IllegalArgumentException("Location or entity class cannot be null");
        }
    }

    static void addEntity(net.minecraft.server.v1_8_R1.Entity entity, CreatureSpawnEvent.SpawnReason reason) throws IllegalArgumentException {
        if (entity == null)
            throw new IllegalArgumentException("Cannot spawn null entity");

        World world = entity.world;

        if (entity instanceof EntityInsentient) {
            ((EntityInsentient) entity).prepare(world.E(new BlockPosition(entity)), null);
        }

        world.addEntity(entity, reason);
    }

}
