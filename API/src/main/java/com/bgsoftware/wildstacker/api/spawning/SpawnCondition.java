package com.bgsoftware.wildstacker.api.spawning;

import com.bgsoftware.wildstacker.api.WildStackerAPI;
import org.bukkit.Location;

import java.util.Objects;
import java.util.function.Predicate;

public abstract class SpawnCondition implements Predicate<Location> {

    private final String id, name;

    protected SpawnCondition(String id){
        this(id, id);
    }

    protected SpawnCondition(String id, String name){
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName(){
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SpawnCondition that = (SpawnCondition) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Register a new spawn condition into the system.
     * If a spawn condition already exists with a similar id, the new one will override the old one.
     * @param spawnCondition The spawn condition to register.
     */
    public static SpawnCondition register(SpawnCondition spawnCondition){
        return WildStackerAPI.getWildStacker().getSystemManager().registerSpawnCondition(spawnCondition);
    }

}
