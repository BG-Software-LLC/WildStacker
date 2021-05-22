package com.bgsoftware.wildstacker.api.enums;

import org.bukkit.event.entity.CreatureSpawnEvent;

import java.util.Arrays;

public enum SpawnCause {

    /**
     * Vanilla spawn causes.
     */
    BED(0),
    BEEHIVE(1),
    BREEDING(2),
    BUILD_IRONGOLEM(3),
    BUILD_SNOWMAN(4),
    BUILD_WITHER(5),
    CHUNK_GEN(6),
    CURED(7),
    CUSTOM(8),
    DEFAULT(9),
    DISPENSE_EGG(10),
    DROWNED(11),
    EGG(12),
    ENDER_PEARL(13),
    EXPLOSION(14),
    INFECTION(15),
    JOCKEY(16),
    LIGHTNING(17),
    MOUNT(18),
    NATURAL(19),
    NETHER_PORTAL(20),
    OCELOT_BABY(21),
    PATROL(22),
    RAID(23),
    REINFORCEMENTS(24),
    SHEARED(25),
    SHOULDER_ENTITY(26),
    SILVERFISH_BLOCK(27),
    SLIME_SPLIT(28),
    SPAWNER(29),
    SPAWNER_EGG(30),
    TRAP(31),
    VILLAGE_DEFENSE(32),
    VILLAGE_INVASION(33),

    /**
     * Custom spawn causes.
     */
    MYTHIC_MOBS(101),
    CUSTOM_BOSSES(102),
    BOSS(103),
    EPIC_BOSSES(104),
    EPIC_BOSSES_MINION(105),
    EPIC_SPAWNERS(106),
    MY_PET(107),
    ECHO_PET(108),
    ELITE_BOSSES(109),
    MORE_BOSSES(110);

    private final int id;

    SpawnCause(int id){
        this.id = id;
    }

    public static SpawnCause valueOf(CreatureSpawnEvent.SpawnReason spawnReason){
        return matchCause(spawnReason.name());
    }

    public static SpawnCause valueOf(int id){
        return Arrays.stream(values()).filter(spawnCause -> spawnCause.getId() == id).findFirst().orElse(DEFAULT);
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

    public int getId() {
        return id;
    }

}
