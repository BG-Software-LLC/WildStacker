package xyz.wildseries.wildstacker.api.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import xyz.wildseries.wildstacker.api.objects.StackedObject;

@SuppressWarnings("WeakerAccess")
public abstract class UnstackEvent extends Event implements Cancellable {

    private boolean cancelled;

    protected final StackedObject object;
    protected final int unstackAmount;

    public UnstackEvent(StackedObject object, int unstackAmount){
        this.object = object;
        this.unstackAmount = unstackAmount;
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
