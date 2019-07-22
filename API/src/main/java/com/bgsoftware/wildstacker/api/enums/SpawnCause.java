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

    MYTHIC_MOBS,
    CUSTOM_BOSSES,
    EPIC_BOSSES,
    EPIC_SPAWNERS;

    public static SpawnCause valueOf(CreatureSpawnEvent.SpawnReason spawnReason){
        return valueOf(spawnReason.name());
    }

    public CreatureSpawnEvent.SpawnReason toSpawnReason(){
        try{
            return CreatureSpawnEvent.SpawnReason.valueOf(name());
        }catch(Exception ex){
            return CreatureSpawnEvent.SpawnReason.CUSTOM;
        }
    }

}
