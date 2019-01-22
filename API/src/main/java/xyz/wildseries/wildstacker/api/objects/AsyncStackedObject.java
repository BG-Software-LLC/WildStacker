package xyz.wildseries.wildstacker.api.objects;

import org.bukkit.entity.Entity;

import java.util.List;

public interface AsyncStackedObject<T> extends StackedObject<T> {

    T tryStackAsync(List<Entity> nearbyEntities);

}
