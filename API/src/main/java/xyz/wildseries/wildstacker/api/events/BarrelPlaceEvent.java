package xyz.wildseries.wildstacker.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import xyz.wildseries.wildstacker.api.objects.StackedBarrel;

@SuppressWarnings("unused")
public class BarrelPlaceEvent extends PlaceEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    public BarrelPlaceEvent(Player player, StackedBarrel barrel){
        super(player, barrel);
    }

    public StackedBarrel getBarrel() {
        return (StackedBarrel) object;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
