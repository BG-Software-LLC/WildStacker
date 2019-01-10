package xyz.wildseries.wildstacker.api.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import xyz.wildseries.wildstacker.api.objects.StackedObject;

@SuppressWarnings("WeakerAccess")
public abstract class StackEvent extends Event implements Cancellable {

    private boolean cancelled;

    protected final StackedObject object, target;

    public StackEvent(StackedObject object, StackedObject target){
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
