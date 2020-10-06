package com.bgsoftware.wildstacker.nms;

import com.bgsoftware.wildstacker.utils.holograms.Hologram;
import org.bukkit.Location;

@SuppressWarnings("unused")
public final class NMSHolograms_v1_7_R4 implements NMSHolograms {

    @Override
    public Hologram createHologram(Location location) {
        return new EmptyHologram();
    }

    private static class EmptyHologram implements Hologram{

        @Override
        public void setHologramName(String name) {

        }

        @Override
        public void removeHologram() {

        }
    }

}
