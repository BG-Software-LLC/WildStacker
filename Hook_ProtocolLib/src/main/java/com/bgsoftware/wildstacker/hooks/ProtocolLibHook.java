package com.bgsoftware.wildstacker.hooks;

import com.bgsoftware.wildstacker.Locale;
import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.utils.ServerVersion;
import com.bgsoftware.wildstacker.utils.entity.EntitiesGetter;
import com.bgsoftware.wildstacker.utils.entity.EntityUtils;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("unused")
public final class ProtocolLibHook {

    private static WildStackerPlugin plugin;
    private static boolean registered = false;

    public static void register(WildStackerPlugin plugin) {
        if (registered)
            return;

        ProtocolLibHook.plugin = plugin;
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketListener());
        plugin.getServer().getPluginManager().registerEvents(new CommandsListener(), plugin);

        registered = true;
    }

    private static Object getCustomName(String customName) {
        return ServerVersion.isLegacy() ? customName : customName.isEmpty() ? Optional.empty() :
                Optional.of(plugin.getNMSAdapter().getChatMessage(customName));
    }

    private static WrappedDataWatcher.Serializer getNameSerializer() {
        return ServerVersion.isLegacy() ? WrappedDataWatcher.Registry.get(String.class) :
                WrappedDataWatcher.Registry.getChatComponentSerializer(true);
    }

    private static final class CommandsListener implements Listener {

        @EventHandler
        public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
            handleEntitiesCommand(event);
            handleItemsCommand(event);
        }

        private static void handleEntitiesCommand(PlayerCommandPreprocessEvent event) {
            if (!plugin.getSettings().entitiesNamesToggleEnabled)
                return;

            String commandSyntax = "/" + plugin.getSettings().entitiesNamesToggleCommand;

            if (!event.getMessage().equalsIgnoreCase(commandSyntax) && !event.getMessage().startsWith(commandSyntax + " "))
                return;

            event.setCancelled(true);

            if (plugin.getSystemManager().hasEntityNamesToggledOff(event.getPlayer())) {
                Locale.ENTITY_NAMES_TOGGLE_ON.send(event.getPlayer());
            } else {
                Locale.ENTITY_NAMES_TOGGLE_OFF.send(event.getPlayer());
            }

            plugin.getSystemManager().toggleEntityNames(event.getPlayer());

            //Refresh item names
            EntitiesGetter.getNearbyEntities(event.getPlayer().getLocation(), 48, entity ->
                            EntityUtils.isStackable(entity) && plugin.getNMSAdapter().isCustomNameVisible(entity))
                    .forEach(entity -> updateName(event.getPlayer(), entity));
        }

        private static void handleItemsCommand(PlayerCommandPreprocessEvent event) {
            if (!plugin.getSettings().itemsNamesToggleEnabled)
                return;

            String commandSyntax = "/" + plugin.getSettings().itemsNamesToggleCommand;

            if (!event.getMessage().equalsIgnoreCase(commandSyntax) && !event.getMessage().startsWith(commandSyntax + " "))
                return;

            event.setCancelled(true);

            if (plugin.getSystemManager().hasItemNamesToggledOff(event.getPlayer())) {
                Locale.ITEM_NAMES_TOGGLE_ON.send(event.getPlayer());
            } else {
                Locale.ITEM_NAMES_TOGGLE_OFF.send(event.getPlayer());
            }

            plugin.getSystemManager().toggleItemNames(event.getPlayer());

            //Refresh item names
            EntitiesGetter.getNearbyEntities(event.getPlayer().getLocation(), 48, entity ->
                            entity instanceof Item && plugin.getNMSAdapter().isCustomNameVisible(entity))
                    .forEach(entity -> updateName(event.getPlayer(), entity));
        }

        private static void updateName(Player player, Entity entity) {
            PacketContainer packetContainer = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_METADATA);
            packetContainer.getIntegers().write(0, entity.getEntityId());
            WrappedDataWatcher watcher = new WrappedDataWatcher();
            watcher.setEntity(entity);

            WrappedDataWatcher.Serializer nameSerializer = getNameSerializer();
            WrappedDataWatcher.Serializer visibleSerializer = WrappedDataWatcher.Registry.get(Boolean.class);

            watcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(2, nameSerializer),
                    getCustomName(plugin.getNMSAdapter().getCustomName(entity)));
            watcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(3, visibleSerializer),
                    plugin.getNMSAdapter().isCustomNameVisible(entity));

            packetContainer.getWatchableCollectionModifier().write(0, watcher.getWatchableObjects());

            try {
                ProtocolLibrary.getProtocolManager().sendServerPacket(player, packetContainer);
            } catch (InvocationTargetException e) {
                WildStackerPlugin.log("There was an error while sending the name toggle packet to " + player.getName() + ":");
                e.printStackTrace();
            }
        }

    }

    private static final class PacketListener extends PacketAdapter {

        PacketListener() {
            super(ProtocolLibHook.plugin, ListenerPriority.NORMAL, PacketType.Play.Server.ENTITY_METADATA);
        }

        @Override
        public void onPacketSending(PacketEvent event) {
            if (event.getPacketType() == PacketType.Play.Server.ENTITY_METADATA) {
                PacketContainer packetContainer = event.getPacket();
                try {
                    StructureModifier<Entity> entityModifier = packetContainer.getEntityModifier(event);
                    if (entityModifier.size() > 0) {
                        if ((ProtocolLibHook.plugin.getSystemManager().hasItemNamesToggledOff(event.getPlayer()) &&
                                entityModifier.read(0) instanceof Item) ||
                                (ProtocolLibHook.plugin.getSystemManager().hasEntityNamesToggledOff(event.getPlayer()) &&
                                        entityModifier.read(0) instanceof LivingEntity)) {
                            StructureModifier<List<WrappedWatchableObject>> structureModifier = packetContainer.getWatchableCollectionModifier();
                            if (structureModifier.size() > 0) {
                                WrappedDataWatcher watcher = new WrappedDataWatcher(structureModifier.read(0));
                                watcher.setObject(2, new WrappedWatchableObject(2, getCustomName("")));
                                watcher.setObject(3, new WrappedWatchableObject(3, false));
                                packetContainer.getWatchableCollectionModifier().write(0, watcher.getWatchableObjects());
                            }
                        }
                    }
                } catch (Throwable ignored) {
                }
            }
        }

    }

}
