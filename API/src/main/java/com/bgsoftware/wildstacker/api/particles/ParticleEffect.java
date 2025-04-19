package com.bgsoftware.wildstacker.api.particles;

import org.bukkit.Location;

/**
 * Represents a generic particle effect.
 */
public interface ParticleEffect {

    /**
     * Spawns this particle at a location.
     *
     * @param location The target location
     */
    void spawnParticle(Location location);
}