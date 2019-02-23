package com.bgsoftware.wildstacker.hooks;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.objects.StackedObject;
import org.bukkit.Location;

public final class HologramsProvider_Default implements HologramsProvider {

    public HologramsProvider_Default(){
        WildStackerPlugin.log(" - Couldn't find any hologram providers, disabling holograms...");
    }

    @Override
    public void createHologram(StackedObject stackedObject, String line) {

    }

    @Override
    public void createHologram(Location location, String line) {

    }

    @Override
    public void deleteHologram(StackedObject stackedObject) {

    }

    @Override
    public void deleteHologram(Location location) {

    }

    @Override
    public void changeLine(StackedObject stackedObject, String newLine, boolean createIfNull) {

    }

    @Override
    public void changeLine(Location location, String newLine, boolean createIfNull) {

    }

    @Override
    public boolean isHologram(Location location) {
        return false;
    }

    @Override
    public void clearHolograms() {

    }
}
