package com.bgsoftware.wildstacker.utils.events;

import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.wildstacker.WildStackerPlugin;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.RegisteredListener;

import java.util.ArrayList;
import java.util.Collection;

public class HandlerListWrapper extends HandlerList {

    private static final ReflectField<ArrayList<HandlerList>> ALL_HANDLERS = new ReflectField<>(
            HandlerList.class, ArrayList.class, "allLists");

    private static final ReflectField<EventExecutor> EVENT_EXECUTOR = new ReflectField<EventExecutor>(
            RegisteredListener.class, EventExecutor.class, "executor")
            .removeFinal();

    private static final WildStackerPlugin plugin = WildStackerPlugin.getPlugin();

    private Mode mode = Mode.NEW;

    public HandlerListWrapper(HandlerList original) {
        this.injectHandlerList(original);

        // Copy listeners of WildStacker to this HandlerList
        for (RegisteredListener registeredListener : original.getRegisteredListeners()) {
            register(registeredListener);
        }
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    @Override
    public synchronized void register(RegisteredListener listener) {
        EventExecutor eventExecutor = EVENT_EXECUTOR.get(listener);
        if (!(eventExecutor instanceof WrappedEventExecutor)) {
            Mode callMode = listener.getPlugin() == plugin ? Mode.NEW : Mode.ORIGINAL;
            WrappedEventExecutor wrappedEventExecutor = new WrappedEventExecutor(eventExecutor, callMode);
            EVENT_EXECUTOR.set(listener, wrappedEventExecutor);
        }
        super.register(listener);
    }

    @Override
    public void registerAll(Collection<RegisteredListener> listeners) {
        for (RegisteredListener listener : listeners) {
            register(listener);
        }
    }

    private void injectHandlerList(HandlerList original) {
        ArrayList<HandlerList> allHandlers = ALL_HANDLERS.get(null);
        allHandlers.remove(original);
        allHandlers.add(this);
    }

    private class WrappedEventExecutor implements EventExecutor {

        private final EventExecutor original;
        private final Mode callMode;

        WrappedEventExecutor(EventExecutor original, Mode callMode) {
            this.original = original;
            this.callMode = callMode;
        }

        @Override
        public void execute(Listener listener, Event event) throws EventException {
            if (this.callMode == HandlerListWrapper.this.mode) {
                this.original.execute(listener, event);
            }
        }

    }

    public enum Mode {

        ORIGINAL,
        NEW

    }

}
