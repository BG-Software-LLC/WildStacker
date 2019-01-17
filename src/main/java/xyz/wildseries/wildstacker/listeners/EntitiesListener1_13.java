package xyz.wildseries.wildstacker.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTransformEvent;
import xyz.wildseries.wildstacker.api.objects.StackedEntity;
import xyz.wildseries.wildstacker.objects.WStackedEntity;

@SuppressWarnings("unused")
public final class EntitiesListener1_13 implements Listener {

    @EventHandler
    public void onEntityTransform(EntityTransformEvent e){
        StackedEntity stackedEntity = WStackedEntity.of(e.getEntity());

        if(stackedEntity.getStackAmount() > 1){
            StackedEntity transformed = WStackedEntity.of(e.getTransformedEntity());
            transformed.setStackAmount(stackedEntity.getStackAmount(), true);
            stackedEntity.remove();
        }
    }

}
