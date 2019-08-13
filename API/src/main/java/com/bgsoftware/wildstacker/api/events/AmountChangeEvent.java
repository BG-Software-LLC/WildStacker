package com.bgsoftware.wildstacker.api.events;

import com.bgsoftware.wildstacker.api.objects.StackedObject;
import org.bukkit.event.Event;

@SuppressWarnings("WeakerAccess")
public abstract class AmountChangeEvent<T extends StackedObject> extends Event {

    private final T stackedObject;
    private final int stackAmount, originalAmount;

    public AmountChangeEvent(T stackedObject, int stackAmount){
        this.stackedObject = stackedObject;
        this.originalAmount = stackedObject.getStackAmount();
        this.stackAmount = stackAmount;
    }

    public T getStackedObject(){
        return stackedObject;
    }

    public int getStackAmount() {
        return stackAmount;
    }

    public int getOriginalAmount() {
        return originalAmount;
    }
}
