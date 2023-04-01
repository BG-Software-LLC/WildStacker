package com.bgsoftware.wildstacker.utils;

import org.bukkit.Location;

public class MergeBox {

    private final double maxX;
    private final double maxY;
    private final double maxZ;
    private final double minX;
    private final double minY;
    private final double minZ;

    public MergeBox(Location location, int radius) {
        this.maxX = location.getX() + radius;
        this.maxY = location.getY() + radius;
        this.maxZ = location.getZ() + radius;
        this.minX = location.getX() - radius;
        this.minY = location.getY() - radius;
        this.minZ = location.getZ() - radius;
    }

    public boolean isInside(Location location) {
        return location.getX() >= minX && location.getX() <= maxX &&
                location.getY() >= minY && location.getY() <= maxY &&
                location.getZ() >= minZ && location.getZ() <= maxZ;
    }

}
