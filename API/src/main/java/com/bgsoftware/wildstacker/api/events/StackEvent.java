package com.bgsoftware.wildstacker.api.events;

import com.bgsoftware.wildstacker.api.objects.StackedObject;
import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

@SuppressWarnings("WeakerAccess")
public abstract class StackEvent extends Event implements Cancellable {

    private boolean cancelled;

    protected final StackedObject object, target;

    public StackEvent(StackedObject object, StackedObject target){
        super(!Bukkit.isPrimaryThread());
        this.object = object;
        this.target = target;
        cancelled = false;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

}
