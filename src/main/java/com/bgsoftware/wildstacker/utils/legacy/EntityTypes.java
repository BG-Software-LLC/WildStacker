package com.bgsoftware.wildstacker.utils.legacy;

import com.bgsoftware.wildstacker.utils.ServerVersion;
import org.bukkit.entity.Guardian;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.MushroomCow;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Zombie;

public enum  EntityTypes {

    BAT,
    BEE,
    BLAZE,
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
    GHAST,
    GIANT,
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
    SNOWMAN,
    SPIDER,
    SQUID,
    STRAY,
    STRIDER,
    TRADER_LLAMA,
    TROPICAL_FISH,
    TURTLE,
    UNKNOWN,
    VEX,
    VILLAGER,
    VINDICATOR,
    WANDERING_TRADER,
    WITCH,
    WITHER,
    WITHER_SKELETON,
    WOLF,
    ZOGLIN,
    ZOMBIE,
    ZOMBIE_HORSE,
    ZOMBIE_PIGMAN,
    ZOMBIE_VILLAGER;

    public static EntityTypes fromName(String name){
        try{
            return EntityTypes.valueOf(name);
        }catch(IllegalArgumentException ignored){}

        switch (name){
            case "MUSHROOM_COW":
                return MOOSHROOM;
            case "ZOMBIFIED_PIGLIN":
            case "PIG_ZOMBIE":
                return ZOMBIE_PIGMAN;
        }

        throw new IllegalArgumentException("Couldn't cast " + name + " into a EntityTypes enum. Contact Ome_R!");
    }

    public static EntityTypes fromEntity(LivingEntity livingEntity){
        if(ServerVersion.isLessThan(ServerVersion.v1_11)) {
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

        if(livingEntity instanceof MushroomCow)
            return EntityTypes.MOOSHROOM;

        return livingEntity instanceof PigZombie ? EntityTypes.ZOMBIE_PIGMAN : valueOf(livingEntity.getType().name());
    }

    public boolean isSlime() {
        return this == SLIME || this == MAGMA_CUBE;
    }

    public boolean isRaider() {
        return ServerVersion.isAtLeast(ServerVersion.v1_14) && (this == EVOKER || this == PILLAGER || this == VINDICATOR);
    }

}
