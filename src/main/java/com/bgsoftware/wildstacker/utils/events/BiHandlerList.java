package com.bgsoftware.wildstacker.utils.events;

import com.bgsoftware.common.reflection.ReflectField;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;

import java.util.ArrayList;
import java.util.Collection;

public class BiHandlerList extends HandlerList {

    private static final ReflectField<ArrayList<HandlerList>> ALL_HANDLERS = new ReflectField<>(
            HandlerList.class, ArrayList.class, "allLists");

    private final HandlerList original;
    private Mode mode = Mode.NEW;
    private boolean freeze = false;

    public BiHandlerList(HandlerList original) {
        this.original = original;
        removeHandlerList(original);
    }

    private static void removeHandlerList(HandlerList handlerList) {
        ArrayList<HandlerList> allHandlers = ALL_HANDLERS.get(null);
        allHandlers.remove(handlerList);
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public void freeze() {
        this.freeze = true;
    }

    @Override
    public synchronized void register(RegisteredListener listener) {
        if (this.freeze || this.mode == Mode.ORIGINAL) {
            original.register(listener);
        } else {
            super.register(listener);
        }
    }

    @Override
    public void registerAll(Collection<RegisteredListener> listeners) {
        if (this.freeze || this.mode == Mode.ORIGINAL) {
            original.registerAll(listeners);
        } else {
            super.registerAll(listeners);
        }
    }

    @Override
    public synchronized void unregister(RegisteredListener listener) {
        original.unregister(listener);
        super.unregister(listener);
    }

    @Override
    public synchronized void unregister(Plugin plugin) {
        super.unregister(plugin);
        original.unregister(plugin);
    }

    @Override
    public synchronized void unregister(Listener listener) {
        super.unregister(listener);
        original.unregister(listener);
    }

    @Override
    public synchronized void bake() {
        if (this.mode == Mode.ORIGINAL) {
            original.bake();
        } else {
            super.bake();
        }
    }

    @Override
    public RegisteredListener[] getRegisteredListeners() {
        if (this.mode == Mode.ORIGINAL) {
            return original.getRegisteredListeners();
        } else {
            return super.getRegisteredListeners();
        }
    }

    public enum Mode {

        ORIGINAL,
        NEW

    }

}
