package com.bgsoftware.wildstacker.utils.entity;

import java.util.Optional;

public final class FutureEntityTracker<T> {

    private T trackedData;
    private int entitiesToTrack;

    public FutureEntityTracker() {
    }

    public void startTracking(T trackedData, int entitiesToTrack) {
        this.trackedData = trackedData;
        this.entitiesToTrack = entitiesToTrack;
    }

    public void resetTracker() {
        if (entitiesToTrack > 0) {
            entitiesToTrack = 0;
        }
    }

    public Optional<T> getTrackedData() {
        return Optional.ofNullable(trackedData);
    }

    public void decreaseTrackCount() {
        if (--this.entitiesToTrack == 0)
            this.trackedData = null;
    }

}
