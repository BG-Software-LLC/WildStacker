package com.bgsoftware.wildstacker.utils.particles;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.particles.ParticleEffect;
import org.bukkit.Location;

public final class ParticleWrapper implements ParticleEffect {

    private static final WildStackerPlugin plugin = WildStackerPlugin.getPlugin();

    private final String particle;
    private final int count, offsetX, offsetY, offsetZ;
    private final double extra;

    public ParticleWrapper(String particle, int count, int offsetX, int offsetY, int offsetZ, double extra) {
        this.particle = particle;
        this.count = count;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;
        this.extra = extra;
    }

    public void spawnParticle(Location location) {
        plugin.getNMSWorld().playParticle(particle, location, count, offsetX, offsetY, offsetZ, extra);
    }

}
