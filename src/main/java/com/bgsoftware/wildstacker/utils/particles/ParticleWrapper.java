package com.bgsoftware.wildstacker.utils.particles;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import org.bukkit.Location;

public final class ParticleWrapper {

    private static WildStackerPlugin plugin = WildStackerPlugin.getPlugin();

    private String particle;
    private int count, offsetX, offsetY, offsetZ;
    private double extra;

    public ParticleWrapper(String particle, int count, int offsetX, int offsetY, int offsetZ, double extra) {
        this.particle = particle;
        this.count = count;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;
        this.extra = extra;
    }

    public void spawnParticle(Location location) {
        plugin.getNMSAdapter().playParticle(particle, location, count, offsetX, offsetY, offsetZ, extra);
    }

}
