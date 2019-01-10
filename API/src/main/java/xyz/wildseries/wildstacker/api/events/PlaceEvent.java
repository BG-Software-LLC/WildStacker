package xyz.wildseries.wildstacker.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.player.PlayerEvent;
import xyz.wildseries.wildstacker.api.objects.StackedObject;

@SuppressWarnings("WeakerAccess")
public abstract class PlaceEvent extends PlayerEvent implements Cancellable {

    private boolean cancelled;

    protected final StackedObject object;

    public PlaceEvent(Player player, StackedObject object){
        super(player);
        this.object = object;
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
