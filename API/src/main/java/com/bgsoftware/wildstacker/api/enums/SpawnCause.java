package com.bgsoftware.wildstacker.api.enums;

import org.bukkit.event.entity.CreatureSpawnEvent;

public enum SpawnCause {

    /**
     * Vanilla spawn causes.
     */
    BED,BEEHIVE,
    BREEDING,
    BUILD_IRONGOLEM,
    BUILD_SNOWMAN,
    BUILD_WITHER,
    CHUNK_GEN,
    CURED,
    CUSTOM,
    DEFAULT,
    DISPENSE_EGG,
    DROWNED,
    EGG,
    ENDER_PEARL,
    EXPLOSION,
    INFECTION,
    JOCKEY,
    LIGHTNING,
    MOUNT,
    NATURAL,
    NETHER_PORTAL,
    OCELOT_BABY,
    PATROL,
    RAID,
    REINFORCEMENTS,
    SHEARED,
    SHOULDER_ENTITY,
    SILVERFISH_BLOCK,
    SLIME_SPLIT,
    SPAWNER,
    SPAWNER_EGG,
    TRAP,
    VILLAGE_DEFENSE,
    VILLAGE_INVASION,

    /**
     * Custom spawn causes.
     */
    MYTHIC_MOBS,
    CUSTOM_BOSSES,
    BOSS,
    EPIC_BOSSES,
    EPIC_BOSSES_MINION,
    EPIC_SPAWNERS,
    MY_PET;

    public static SpawnCause valueOf(CreatureSpawnEvent.SpawnReason spawnReason){
        return matchCause(spawnReason.name());
    }

    /**
     * Returns a spawn cause from string. If not found, returning DEFAULT.
     * @param name The name to check.
     */
    public static SpawnCause matchCause(String name){
        try{
            return valueOf(name);
        }catch(IllegalArgumentException ex){
            return DEFAULT;
        }
    }

    public CreatureSpawnEvent.SpawnReason toSpawnReason(){
        try{
            return CreatureSpawnEvent.SpawnReason.valueOf(name());
        }catch(Exception ex){
            return CreatureSpawnEvent.SpawnReason.CUSTOM;
        }
    }

}
