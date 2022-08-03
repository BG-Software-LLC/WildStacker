package com.bgsoftware.wildstacker.nms.v1_7_R4;

import com.bgsoftware.wildstacker.utils.holograms.Hologram;
import org.bukkit.Location;

@SuppressWarnings("unused")
public final class NMSHolograms implements com.bgsoftware.wildstacker.nms.NMSHolograms {

    @Override
    public Hologram createHologram(Location location) {
        return new EmptyHologram();
    }

    private static class EmptyHologram implements Hologram {

        @Override
        public void setHologramName(String name) {

        }

        @Override
        public void removeHologram() {

        }
    }

}
