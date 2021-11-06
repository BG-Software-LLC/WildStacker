package com.bgsoftware.wildstacker.objects;

import com.bgsoftware.wildstacker.utils.holograms.Hologram;

public abstract class WStackedHologramObject<T> extends WStackedObject<T> {

    protected Hologram hologram;

    protected WStackedHologramObject(T object, int stackAmount) {
        super(object, stackAmount);
    }

    public void removeHologram() {
        if (hologram != null) {
            hologram.removeHologram();
            hologram = null;
        }
    }

    public void setHologramName(String name, boolean createIfNull) {
        if (hologram == null) {
            if (!createIfNull)
                return;
            hologram = plugin.getNMSHolograms().createHologram(getLocation().add(0.5, 1, 0.5));
        }

        hologram.setHologramName(name);
    }

    public Hologram createHologram() {
        hologram = plugin.getNMSHolograms().createHologram(getLocation().add(0.5, 1, 0.5));
        return hologram;
    }

}
