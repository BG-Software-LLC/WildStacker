package com.bgsoftware.wildstacker.hooks;

import org.bukkit.Location;

import java.util.List;

public interface RegionsProvider {

    List<String> getRegionNames(Location bukkitLocation);

}
