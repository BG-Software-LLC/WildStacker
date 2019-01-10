package xyz.wildseries.wildstacker.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.entity.EntityTransformEvent;
import xyz.wildseries.wildstacker.WildStackerPlugin;
import xyz.wildseries.wildstacker.api.objects.StackedEntity;
import xyz.wildseries.wildstacker.objects.WStackedEntity;
import xyz.wildseries.wildstacker.utils.async.AsyncUtil;

@SuppressWarnings("unused")
public final class EntitiesListener1_13 implements Listener {

    private WildStackerPlugin plugin;

    public EntitiesListener1_13(WildStackerPlugin plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityTransform(EntityTransformEvent e){
        StackedEntity stackedEntity = WStackedEntity.of(e.getEntity());

        if(stackedEntity.getStackAmount() > 1){
            StackedEntity transformed = WStackedEntity.of(e.getTransformedEntity());
            transformed.setStackAmount(stackedEntity.getStackAmount(), true);
            stackedEntity.remove();
        }
    }

    @EventHandler
    public void onEntityBreed(EntityBreedEvent e){
        if(plugin.getSettings().stackAfterBreed)
            AsyncUtil.tryStackInto(WStackedEntity.of(e.getFather()), WStackedEntity.of(e.getMother()));
    }

}
