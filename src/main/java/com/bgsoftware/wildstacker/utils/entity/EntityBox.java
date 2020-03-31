package com.bgsoftware.wildstacker.utils.entity;

import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class EntityBox implements Iterable<Entity> {

    public static final EntityBox EMPTY_BOX = new EntityBox(null);

    private final Set<Entity> entities = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final Predicate<Entity> filter;

    public EntityBox(Predicate<Entity> filter){
        this.filter = filter;
    }

    public void feed(Collection<Entity> entities){
        this.entities.addAll(filter == null ? entities : entities.stream().filter(filter).collect(Collectors.toList()));
    }

    @NotNull
    @Override
    public Iterator<Entity> iterator() {
        return entities.iterator();
    }

    public Stream<Entity> stream(){
        return entities.stream();
    }

    public <R> Stream<R> map(Function<? super Entity, ? extends R> mapper){
        return stream().map(mapper);
    }

}
