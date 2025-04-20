package com.bgsoftware.wildstacker;


import com.bgsoftware.wildstacker.errors.ManagerLoadException;

public abstract class Manager {

    protected final WildStackerPlugin plugin;

    protected Manager(WildStackerPlugin plugin) {
        this.plugin = plugin;
    }

    public abstract void loadData() throws ManagerLoadException;

}
