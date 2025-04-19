package com.bgsoftware.wildstacker.hooks;

import com.bgsoftware.wildstacker.Locale;
import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.handlers.SystemManager;
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

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("unused")
public final class ProtocolLibHook {

    @Nullable
    public static final WrappedDataWatcher.Serializer NAME_SERIALIZER = ServerVersion.isLegacy() ?
            getSerializer(String.class) :
            WrappedDataWatcher.Registry.getChatComponentSerializer(true);
    @Nullable
    public static final WrappedDataWatcher.Serializer VISIBLE_SERIALIZER = getSerializer(Boolean.class);

    private static final Object VISIBLE_NAME_VALUE_TRUE = ServerVersion.isEquals(ServerVersion.v1_8) ? (byte) 1 : true;
    private static final Object VISIBLE_NAME_VALUE_FALSE = ServerVersion.isEquals(ServerVersion.v1_8) ? (byte) 0 : false;

    private static WildStackerPlugin plugin;
    private static boolean registered = false;
    private static IPacketHandler packetHandler;

    public static void register(WildStackerPlugin plugin) {
        if (registered)
            return;

        ProtocolLibHook.plugin = plugin;

        packetHandler = createPacketHandler();

        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketListener());
        plugin.getServer().getPluginManager().registerEvents(new CommandsListener(), plugin);

        registered = true;
    }

    private static IPacketHandler createPacketHandler() {
        if (ServerVersion.isAtLeast(ServerVersion.v1_19)) {
            try {
                Class.forName("com.comphenix.protocol.wrappers.WrappedDataValue");
                return (IPacketHandler) Class.forName("com.bgsoftware.wildstacker.hooks.ProtocolLib_119PacketHandler")
                        .newInstance();
            } catch (ClassNotFoundException ignored) {
            } catch (Throwable error) {
                throw new RuntimeException(error);
            }
        }

        return new PacketHandler118();
    }

    private static WrappedDataWatcher.Serializer getSerializer(Class<?> serializerClass) {
        return ServerVersion.isEquals(ServerVersion.v1_8) ? null : WrappedDataWatcher.Registry.get(serializerClass);
    }

    private static final class CommandsListener implements Listener {

        @EventHandler
        public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
            handleEntitiesCommand(event);
            handleItemsCommand(event);
        }

        private static void handleEntitiesCommand(PlayerCommandPreprocessEvent event) {
            if (!plugin.getSettings().getEntities().isNamesToggleEnabled())
                return;

            String commandSyntax = "/" + plugin.getSettings().getEntities().getNamesToggleCommand();

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
                            EntityUtils.isStackable(entity) && plugin.getNMSEntities().isCustomNameVisible(entity))
                    .forEach(entity -> updateName(event.getPlayer(), entity));
        }

        private static void handleItemsCommand(PlayerCommandPreprocessEvent event) {
            if (!plugin.getSettings().getItems().isNamesToggleEnabled())
                return;

            String commandSyntax = "/" + plugin.getSettings().getItems().getNamesToggleCommand();

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
                            entity instanceof Item && plugin.getNMSEntities().isCustomNameVisible(entity))
                    .forEach(entity -> updateName(event.getPlayer(), entity));
        }

        private static void updateName(Player player, Entity entity) {
            PacketContainer packetContainer = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_METADATA);
            packetContainer.getIntegers().write(0, entity.getEntityId());

            packetHandler.handlePacketCustomNameUpdate(packetContainer, entity);

            try {
                ProtocolLibrary.getProtocolManager().sendServerPacket(player, packetContainer);
            } catch (Throwable e) {
                WildStackerPlugin.log("There was an error while sending the name toggle packet to " + player.getName() + ":");
                e.printStackTrace();
            }
        }

    }

    private static class PacketListener extends PacketAdapter {

        PacketListener() {
            super(ProtocolLibHook.plugin, ListenerPriority.NORMAL, PacketType.Play.Server.ENTITY_METADATA);
        }

        @Override
        public void onPacketSending(PacketEvent event) {
            if (event.getPacketType() != PacketType.Play.Server.ENTITY_METADATA)
                return;

            try {
                onPacketSendingInternal(event);
            } catch (Throwable error) {
                error.printStackTrace();
            }
        }

        private void onPacketSendingInternal(PacketEvent event) {
            PacketContainer packetContainer = event.getPacket();
            StructureModifier<Entity> entityModifier = packetContainer.getEntityModifier(event);

            if (entityModifier.size() <= 0)
                return;

            Entity entity = entityModifier.read(0);

            SystemManager systemManager = ProtocolLibHook.plugin.getSystemManager();

            if ((entity instanceof Item && systemManager.hasItemNamesToggledOff(event.getPlayer())) ||
                    (entity instanceof LivingEntity && systemManager.hasEntityNamesToggledOff(event.getPlayer()))) {
                packetHandler.handlePacketCustomNameUpdate(packetContainer, null);
            }
        }

    }

    public interface IPacketHandler {

        void handlePacketCustomNameUpdate(PacketContainer packetContainer, @Nullable Entity entity);

    }

    private static void setWatcherObject(WrappedDataWatcher watcher,
                                         int index,
                                         @Nullable WrappedDataWatcher.Serializer serializer,
                                         Object value) {
        if (serializer == null) {
            watcher.setObject(index, value);
        } else {
            watcher.setObject(index, serializer, value);
        }
    }

    private static final class PacketHandler118 implements IPacketHandler {

        private static final boolean isLegacy = ServerVersion.isLegacy();
        private static final Object EMPTY_CUSTOM_NAME = isLegacy ? "" : Optional.empty();

        @Override
        public void handlePacketCustomNameUpdate(PacketContainer packetContainer, @Nullable Entity entity) {
            StructureModifier<List<WrappedWatchableObject>> structureModifier =
                    packetContainer.getWatchableCollectionModifier();

            WrappedDataWatcher watcher;
            if (entity == null) {
                watcher = new WrappedDataWatcher();

                setWatcherObject(watcher, 2, NAME_SERIALIZER, EMPTY_CUSTOM_NAME);
                setWatcherObject(watcher, 3, VISIBLE_SERIALIZER, VISIBLE_NAME_VALUE_FALSE);
            } else {
                watcher = new WrappedDataWatcher(entity);

                Object customName = parseCustomName(plugin.getNMSEntities().getCustomName(entity, true));
                setWatcherObject(watcher, 2, NAME_SERIALIZER, customName);

                boolean nameVisible = plugin.getNMSEntities().isCustomNameVisible(entity);
                setWatcherObject(watcher, 3, VISIBLE_SERIALIZER,
                        nameVisible ? VISIBLE_NAME_VALUE_TRUE : VISIBLE_NAME_VALUE_FALSE);
            }

            structureModifier.write(0, watcher.getWatchableObjects());
        }

        private Object parseCustomName(String customName) {
            return isLegacy ? customName : customName == null || customName.isEmpty() ? Optional.empty() :
                    Optional.of(plugin.getNMSAdapter().getChatMessage(customName));
        }

    }

}
