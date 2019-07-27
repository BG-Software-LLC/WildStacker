package com.bgsoftware.wildstacker.utils.legacy;

import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Guardian;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.MushroomCow;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Zombie;

public enum  EntityTypes {

    BAT,
    BLAZE,
    CAT,
    CAVE_SPIDER,
    CHICKEN,
    COD,
    COW,
    CREEPER,
    DOLPHIN,
    DONKEY("HORSE"),
    DROWNED,
    ELDER_GUARDIAN("GUARDIAN"),
    ENDER_DRAGON,
    ENDERMAN,
    ENDERMITE,
    EVOKER,
    FOX,
    GHAST,
    GIANT,
    GUARDIAN,
    HORSE,
    HUSK,
    ILLUSIONER,
    IRON_GOLEM,
    LLAMA,
    MAGMA_CUBE,
    MULE("HORSE"),
    MOOSHROOM("MUSHROOM_COW"),
    OCELOT,
    PANDA,
    PARROT,
    PHANTOM,
    PIG,
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
    SKELETON_HORSE("HORSE"),
    SLIME,
    SNOWMAN,
    SPIDER,
    SQUID,
    STRAY,
    TRADER_LLAMA,
    TROPICAL_FISH,
    TURTLE,
    VEX,
    VILLAGER,
    VINDICATOR,
    WANDERING_TRADER,
    WITCH,
    WITHER,
    WITHER_SKELETON("SKELETON"),
    WOLF,
    ZOMBIE,
    ZOMBIE_HORSE("HORSE"),
    ZOMBIE_PIGMAN("PIG_ZOMBIE"),
    ZOMBIE_VILLAGER("ZOMBIE");

    EntityTypes(){
        this.bukkitEntityType = name();
    }

    EntityTypes(String bukkitEntityType){
        this.bukkitEntityType = bukkitEntityType;
    }

    private String bukkitEntityType;

    public EntityType toBukkit(){
        try {
            try {
                return EntityType.valueOf(bukkitEntityType);
            } catch (IllegalArgumentException ex) {
                return EntityType.valueOf(name());
            }
        }catch(Exception ex){
            throw new IllegalArgumentException("Couldn't cast " + name() + " into a bukkit enum. Contact Ome_R!");
        }
    }

    private static boolean v1_8 = Bukkit.getBukkitVersion().contains("1.8");
    private static boolean v1_9 = Bukkit.getBukkitVersion().contains("1.9");
    private static boolean v1_10 = Bukkit.getBukkitVersion().contains("1.10");

    public static EntityTypes fromName(String name){
        try{
            return EntityTypes.valueOf(name);
        }catch(IllegalArgumentException ignored){}

        for(EntityTypes entityType : values()){
            if(entityType.bukkitEntityType.equals(name))
                return entityType;
        }

        throw new IllegalArgumentException("Couldn't cast " + name + " into a EntityTypes enum. Contact Ome_R!");
    }

    public static EntityTypes fromEntity(LivingEntity livingEntity){
        if(v1_8 || v1_9 || v1_10) {
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
            if (livingEntity instanceof Guardian) {
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

}
