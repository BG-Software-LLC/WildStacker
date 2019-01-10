package xyz.wildseries.wildstacker.api.events;

import org.bukkit.event.HandlerList;
import xyz.wildseries.wildstacker.api.objects.StackedBarrel;

@SuppressWarnings("unused")
public class BarrelStackEvent extends StackEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    public BarrelStackEvent(StackedBarrel barrel, StackedBarrel target){
        super(barrel, target);
    }

    public StackedBarrel getBarrel() {
        return (StackedBarrel) object;
    }

    public StackedBarrel getTarget() {
        return (StackedBarrel) target;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
