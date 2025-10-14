package com.bgsoftware.wildstacker.listeners;

import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.enums.EntityFlag;
import com.bgsoftware.wildstacker.api.objects.StackedItem;
import com.bgsoftware.wildstacker.objects.WStackedItem;
import com.bgsoftware.wildstacker.utils.entity.EntityStorage;
import com.bgsoftware.wildstacker.utils.events.HandlerListWrapper;
import com.bgsoftware.wildstacker.utils.items.ItemUtils;
import com.bgsoftware.wildstacker.utils.legacy.Materials;
import org.bukkit.Bukkit;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.function.Consumer;

public class PickupItemListener {

    private static final EnumMap<EventType, HandlerListWrapper> CACHED_HANDLER_LIST = new EnumMap<>(EventType.class);

    private final WildStackerPlugin plugin;

    public PickupItemListener(WildStackerPlugin plugin) {
        this.plugin = plugin;

        if (EventType.PLAYER_ATTEMPT_PICKUP_ITEM_EVENT.eventClass != null) {
            plugin.getServer().getPluginManager().registerEvents(new PlayerAttemptPickup(), plugin);
            EventType.PLAYER_ATTEMPT_PICKUP_ITEM_EVENT.isFirstPickupEvent = true;
        }

        if (EventType.PLAYER_PICKUP_ITEM_EVENT.eventClass != null) {
            plugin.getServer().getPluginManager().registerEvents(new PlayerPickup(), plugin);
            if (EventType.PLAYER_ATTEMPT_PICKUP_ITEM_EVENT.eventClass == null)
                EventType.PLAYER_PICKUP_ITEM_EVENT.isFirstPickupEvent = true;
        }

        if (EventType.ENTITY_PICKUP_ITEM_EVENT.eventClass != null) {
            plugin.getServer().getPluginManager().registerEvents(new EntityPickup(), plugin);
        }
    }

    /**
     * Handles picking up the item {@param item} by {@param entityPicker}.
     * Returns whether the original event was cancelled.
     */
    private boolean handleItemPickup(EventType eventType, Item item, LivingEntity entityPicker, int remaining) {
        if (!ItemUtils.isStackable(item))
            return false;

        StackedItem stackedItem = WStackedItem.of(item);

        if (((WStackedItem) stackedItem).isRemoved()) {
            // We did not call the original event, however this item is removed and we pretend we did,
            // so the entity won't pick this item as well.
            // https://github.com/BG-Software-LLC/WildStacker/issues/1074
            return true;
        }

        if (EntityStorage.hasMetadata(item, EntityFlag.RECENTLY_PICKED_UP)) {
            EntityStorage.removeMetadata(item, EntityFlag.RECENTLY_PICKED_UP);
            stackedItem.remove();
            return false;
        }

        ItemStack itemStack = stackedItem.getItemStack();

        // We have a custom pickup logic for items that needs to be called.
        // Because there are 3 different pickup events, and we only want to run it once for an item,
        // we call it for the following events:
        //  1) PlayerAttemptPickupItemEvent - if exists
        //  2) PlayerPickupItemEvent - if `PlayerAttemptPickupItemEvent` does not exist
        //  3) EntityPickupItemEvent - if the picker is not a player
        if (!eventType.isFirstPickupEvent && (eventType != EventType.ENTITY_PICKUP_ITEM_EVENT || entityPicker instanceof Player))
            return false;

        // It should be called only if one of the following conditions is met:
        //  1) Item stacking is enabled
        //  2) The item's stack size is larger than the maximum stack size
        //  3) Buckets stacking is enabled and the item is a bucket
        if (plugin.getSettings().itemsStackingEnabled ||
                stackedItem.getStackAmount() > itemStack.getMaxStackSize() ||
                (plugin.getSettings().bucketsStackerEnabled && Materials.isBucket(itemStack.getType()))) {
            return plugin.getNMSEntities().handleItemPickup(entityPicker, stackedItem, remaining);
        }

        return false;
    }

    public static void injectHandlerLists() {
        for (EventType eventType : EventType.values()) {
            // Changes HandlerList of the event to only include WildStacker's listeners.
            try {
                Class<?> eventClass = Class.forName(eventType.eventClassName);

                ReflectField<HandlerList> handlerListField = findHandlerListField(eventClass);

                HandlerList original = handlerListField.get(null);
                HandlerListWrapper newHandlerList = new HandlerListWrapper(original);
                handlerListField.set(null, newHandlerList);

                CACHED_HANDLER_LIST.put(eventType, newHandlerList);
            } catch (ClassNotFoundException ignored) {
            }
        }
    }

    public static void forEachHandlerList(Consumer<HandlerListWrapper> consumer) {
        for (HandlerListWrapper handlerListWrapper : CACHED_HANDLER_LIST.values()) {
            consumer.accept(handlerListWrapper);
        }
    }

    private static ReflectField<HandlerList> findHandlerListField(Class<?> eventClass) {
        ReflectField<HandlerList> field = new ReflectField<>(
                eventClass, HandlerList.class, "handlers");

        if (!field.isValid()) {
            field = new ReflectField<>(
                    eventClass, HandlerList.class, "HANDLER_LIST");
        }

        return field.removeFinal();
    }

    private static void recallPickupItemEvent(EventType eventType, Event event) {
        HandlerListWrapper pickupItemHandlerList = CACHED_HANDLER_LIST.get(eventType);

        if (pickupItemHandlerList == null)
            throw new IllegalStateException("Cannot find handler list for event " + event + " @ " + eventType);

        try {
            pickupItemHandlerList.setOriginal();
            // Call the event again.
            Bukkit.getPluginManager().callEvent(event);
        } finally {
            pickupItemHandlerList.setNew();
        }
    }

    private class PlayerAttemptPickup implements Listener {

        @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
        public void onPlayerAttemptPickupItem(org.bukkit.event.player.PlayerAttemptPickupItemEvent e) {
            boolean calledOriginalEvent = handleItemPickup(EventType.PLAYER_ATTEMPT_PICKUP_ITEM_EVENT,
                    e.getItem(), e.getPlayer(), e.getRemaining());

            if (calledOriginalEvent) {
                e.setCancelled(true);
                e.setFlyAtPlayer(false);
            } else {
                recallPickupItemEvent(EventType.PLAYER_ATTEMPT_PICKUP_ITEM_EVENT, e);
            }
        }

    }

    private class PlayerPickup implements Listener {

        @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
        public void onPlayerPickupItem(org.bukkit.event.player.PlayerPickupItemEvent e) {
            boolean calledOriginalEvent = handleItemPickup(EventType.PLAYER_PICKUP_ITEM_EVENT,
                    e.getItem(), e.getPlayer(), e.getRemaining());

            if (calledOriginalEvent) {
                e.setCancelled(true);
            } else {
                recallPickupItemEvent(EventType.PLAYER_PICKUP_ITEM_EVENT, e);
            }
        }

    }

    private class EntityPickup implements Listener {

        @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
        public void onEntityPickupItem(org.bukkit.event.entity.EntityPickupItemEvent e) {
            boolean calledOriginalEvent = handleItemPickup(EventType.ENTITY_PICKUP_ITEM_EVENT,
                    e.getItem(), e.getEntity(), e.getRemaining());

            if (calledOriginalEvent) {
                e.setCancelled(true);
            } else {
                recallPickupItemEvent(EventType.ENTITY_PICKUP_ITEM_EVENT, e);
            }
        }

    }

    private enum EventType {

        PLAYER_ATTEMPT_PICKUP_ITEM_EVENT("org.bukkit.event.player.PlayerAttemptPickupItemEvent"),
        ENTITY_PICKUP_ITEM_EVENT("org.bukkit.event.entity.EntityPickupItemEvent"),
        PLAYER_PICKUP_ITEM_EVENT("org.bukkit.event.player.PlayerPickupItemEvent");

        private final String eventClassName;

        @Nullable
        private final Class<?> eventClass;

        private boolean isFirstPickupEvent = false;

        EventType(String eventClassName) {
            this.eventClassName = eventClassName;

            Class<?> eventClass;
            try {
                eventClass = Class.forName(eventClassName);
            } catch (ClassNotFoundException error) {
                eventClass = null;
            }

            this.eventClass = eventClass;
        }

    }

}
