package com.bgsoftware.wildstacker.utils.legacy;

import com.bgsoftware.wildstacker.utils.ServerVersion;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Guardian;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Zombie;

public enum EntityTypes {

    ARMADILLO,
    ALLAY,
    AXOLOTL,
    BAT,
    BEE,
    BLAZE,
    BOGGED,
    BREEZE,
    CAMEL,
    CAT,
    CAVE_SPIDER,
    CHICKEN,
    COD,
    COW,
    CREEPER,
    DOLPHIN,
    DONKEY,
    DROWNED,
    ELDER_GUARDIAN,
    ENDER_DRAGON,
    ENDERMAN,
    ENDERMITE,
    EVOKER,
    FOX,
    FROG,
    GHAST,
    GIANT,
    GLOW_SQUID,
    GOAT,
    GUARDIAN,
    HOGLIN,
    HORSE,
    HUSK,
    ILLUSIONER,
    IRON_GOLEM,
    LLAMA,
    MAGMA_CUBE,
    MULE,
    MOOSHROOM,
    OCELOT,
    PANDA,
    PARROT,
    PHANTOM,
    PIG,
    PIGLIN,
    PIGLIN_BRUTE,
    POLAR_BEAR,
    PILLAGER,
    PUFFERFISH,
    RABBIT,
    RAVAGER,
    SALMON,
    SHEEP,
    SHULKER,
    SILVERFISH,
    SKELETON,
    SKELETON_HORSE,
    SLIME,
    SNIFFER,
    SNOW_GOLEM,
    SPIDER,
    SQUID,
    STRAY,
    STRIDER,
    TADPOLE,
    TRADER_LLAMA,
    TROPICAL_FISH,
    TURTLE,
    UNKNOWN,
    VEX,
    VILLAGER,
    VINDICATOR,
    WANDERING_TRADER,
    WARDEN,
    WITCH,
    WITHER,
    WITHER_SKELETON,
    WOLF,
    ZOGLIN,
    ZOMBIE,
    ZOMBIE_HORSE,
    ZOMBIE_PIGMAN,
    ZOMBIE_VILLAGER;

    private static final EntityTypes[] bukkitTypeConverter = new EntityTypes[EntityType.values().length];

    static {
        for (EntityType entityType : EntityType.values()) {
            try {
                bukkitTypeConverter[entityType.ordinal()] = EntityTypes.fromName(entityType.name());
            } catch (IllegalArgumentException ignored) {
            }
        }

        bukkitTypeConverter[EntityType.MUSHROOM_COW.ordinal()] = EntityTypes.MOOSHROOM;
        bukkitTypeConverter[EntityType.PIG_ZOMBIE.ordinal()] = EntityTypes.ZOMBIE_PIGMAN;
        bukkitTypeConverter[EntityType.SNOWMAN.ordinal()] = EntityTypes.SNOW_GOLEM;
    }

    public static EntityTypes fromName(String name) {
        try {
            return EntityTypes.valueOf(name);
        } catch (IllegalArgumentException ignored) {
        }

        switch (name) {
            case "MUSHROOM_COW":
                return MOOSHROOM;
            case "ZOMBIFIED_PIGLIN":
            case "PIG_ZOMBIE":
                return ZOMBIE_PIGMAN;
            case "SNOWMAN":
                return SNOW_GOLEM;
        }

        throw new IllegalArgumentException("Couldn't cast " + name + " into a EntityTypes enum. Contact Ome_R!");
    }

    public static EntityTypes fromEntity(LivingEntity livingEntity) {
        if (ServerVersion.isLessThan(ServerVersion.v1_11)) {
            if (livingEntity instanceof Horse) {
                Horse horse = (Horse) livingEntity;
                if (horse.getVariant() == Horse.Variant.DONKEY)
                    return EntityTypes.DONKEY;
                else if (horse.getVariant() == Horse.Variant.MULE)
                    return EntityTypes.MULE;
                else if (horse.getVariant() == Horse.Variant.SKELETON_HORSE)
                    return EntityTypes.SKELETON_HORSE;
                else if (horse.getVariant() == Horse.Variant.UNDEAD_HORSE)
                    return EntityTypes.ZOMBIE_HORSE;
                else
                    return EntityTypes.HORSE;
            }
            if (livingEntity.getType().name().equals("GUARDIAN")) {
                return ((Guardian) livingEntity).isElder() ? EntityTypes.ELDER_GUARDIAN : EntityTypes.GUARDIAN;
            }
            if (livingEntity instanceof Zombie && !(livingEntity instanceof PigZombie)) {
                return ((Zombie) livingEntity).isVillager() ? EntityTypes.ZOMBIE_VILLAGER : EntityTypes.ZOMBIE;
            }
            if (livingEntity instanceof Skeleton) {
                return ((Skeleton) livingEntity).getSkeletonType() == Skeleton.SkeletonType.WITHER ? EntityTypes.WITHER_SKELETON : EntityTypes.SKELETON;
            }
        }

        EntityTypes convertedType = bukkitTypeConverter[livingEntity.getType().ordinal()];

        if (convertedType == null)
            throw new IllegalArgumentException("No enum constant EntityTypes." + livingEntity.getType().name());

        return convertedType;
    }

    public boolean isSlime() {
        return this == SLIME || this == MAGMA_CUBE;
    }

    public boolean isRaider() {
        return ServerVersion.isAtLeast(ServerVersion.v1_14) && (this == EVOKER || this == PILLAGER || this == VINDICATOR);
    }

}
