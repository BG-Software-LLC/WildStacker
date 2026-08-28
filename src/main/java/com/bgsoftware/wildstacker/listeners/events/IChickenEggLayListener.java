package com.bgsoftware.wildstacker.listeners.events;

import org.bukkit.entity.Chicken;
import org.bukkit.entity.Item;

public interface IChickenEggLayListener {

    void apply(Chicken chicken, Item egg);

}
