package com.bgsoftware.wildstacker.utils.entity;

import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class EntityBox implements Iterable<Entity> {

    public static final EntityBox EMPTY_BOX = new EntityBox();

    private final Set<Entity> entities;
    private final Predicate<Entity> filter;

    public EntityBox(){
        this(Collections.newSetFromMap(new ConcurrentHashMap<>()), null);
    }

    public EntityBox(Set<Entity> entities, Predicate<Entity> filter){
        this.entities = entities;
        this.filter = filter;
    }

    public void feed(Collection<Entity> entities){
        this.entities.addAll(entities.stream().filter(Objects::nonNull).collect(Collectors.toSet()));
    }

    public EntityBox withFilter(Predicate<Entity> filter){
        return new EntityBox(entities, filter);
    }

    public void clear(){
        entities.clear();
    }

    @NotNull
    @Override
    public Iterator<Entity> iterator() {
        return entities.iterator();
    }

    public Stream<Entity> stream(){
        return filter == null ? entities.stream() : entities.stream().filter(filter);
    }

    public Stream<Entity> filter(Predicate<Entity> filter){
        return stream().filter(filter);
    }

    public <R> Stream<R> map(Function<? super Entity, ? extends R> mapper){
        return stream().map(mapper);
    }

    public boolean anyMatch(Predicate<Entity> entityPredicate){
        return stream().anyMatch(entityPredicate);
    }

}
