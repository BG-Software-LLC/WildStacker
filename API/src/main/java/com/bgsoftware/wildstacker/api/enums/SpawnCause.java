package com.bgsoftware.wildstacker.api.enums;

import org.bukkit.event.entity.CreatureSpawnEvent;

public enum SpawnCause {

    NATURAL,
    JOCKEY,
    CHUNK_GEN,
    SPAWNER,
    EGG,
    SPAWNER_EGG,
    LIGHTNING,
    BUILD_SNOWMAN,
    BUILD_IRONGOLEM,
    BUILD_WITHER,
    VILLAGE_DEFENSE,
    VILLAGE_INVASION,
    BREEDING,
    SLIME_SPLIT,
    REINFORCEMENTS,
    NETHER_PORTAL,
    DISPENSE_EGG,
    INFECTION,
    CURED,
    OCELOT_BABY,
    SILVERFISH_BLOCK,
    MOUNT,
    TRAP,
    ENDER_PEARL,
    SHOULDER_ENTITY,
    DROWNED,
    SHEARED,
    CUSTOM,
    DEFAULT,
    BED,
    EXPLOSION,
    PATROL,
    RAID,
    BEEHIVE,

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
