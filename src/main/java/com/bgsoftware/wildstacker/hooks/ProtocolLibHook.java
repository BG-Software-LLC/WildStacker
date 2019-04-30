package com.bgsoftware.wildstacker.hooks;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class ProtocolLibHook {

    public static Set<UUID> itemsDisabledNames = new HashSet<>();
    public static Set<UUID> entitiesDisabledNames = new HashSet<>();

    public static void register(){
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(WildStackerPlugin.getPlugin(), ListenerPriority.NORMAL, PacketType.Play.Server.ENTITY_METADATA) {
            @Override
            public void onPacketSending(PacketEvent event) {
                if (event.getPacketType() == PacketType.Play.Server.ENTITY_METADATA) {
                    PacketContainer packetContainer = event.getPacket();
                    StructureModifier<Entity> entityModifier = packetContainer.getEntityModifier(event);
                    if(entityModifier.size() > 0) {
                        if ((itemsDisabledNames.contains(event.getPlayer().getUniqueId()) && entityModifier.read(0) instanceof Item) ||
                                (entitiesDisabledNames.contains(event.getPlayer().getUniqueId()) && entityModifier.read(0) instanceof LivingEntity)) {
                            StructureModifier<List<WrappedWatchableObject>> structureModifier = packetContainer.getWatchableCollectionModifier();
                            if (structureModifier.size() > 0) {
                                WrappedDataWatcher watcher = new WrappedDataWatcher(structureModifier.read(0));
                                watcher.setObject(2, new WrappedWatchableObject(2, getCustomName("")));
                                watcher.setObject(3, new WrappedWatchableObject(3, false));
                                packetContainer.getWatchableCollectionModifier().write(0, watcher.getWatchableObjects());
                            }
                        }
                    }
                }
            }
        });
//        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(WildStackerPlugin.getPlugin(), ListenerPriority.NORMAL, PacketType.Play.Server.ENTITY_METADATA) {
//            @Override
//            public void onPacketSending(PacketEvent event) {
//                if (event.getPacketType() == PacketType.Play.Server.ENTITY_METADATA) {
//                    PacketContainer packetContainer = event.getPacket();
//                    StructureModifier<Entity> entityModifier = packetContainer.getEntityModifier(event);
//                    if(entityModifier.size() > 0) {
//                        StructureModifier<List<WrappedWatchableObject>> structureModifier = packetContainer.getWatchableCollectionModifier();
//                        if (structureModifier.size() > 0) {
//                            WrappedDataWatcher watcher = new WrappedDataWatcher(structureModifier.read(0));
//                            if(watcher.hasIndex(2)) {
//                                WrappedWatchableObject watchableObject = watcher.getWatchableObject(2);
//                                watchableObject.set
//                                Bukkit.broadcastMessage(watchableObject + "");
//                            }
//                        }
//                    }
//                }
//            }
//        });
    }

    public static void updateName(Player player, Entity entity){
        PacketContainer packetContainer = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_METADATA);
        packetContainer.getIntegers().write(0, entity.getEntityId());
        WrappedDataWatcher watcher = new WrappedDataWatcher();
        watcher.setEntity(entity);

        WrappedDataWatcher.Serializer nameSerializer = getNameSerializer();
        WrappedDataWatcher.Serializer visibleSerializer = WrappedDataWatcher.Registry.get(Boolean.class);

//        watcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(2, nameSerializer), getCustomName(entity.getCustomName()));
        watcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(2, nameSerializer), getCustomName(entity.getCustomName()));
        watcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(3, visibleSerializer), entity.isCustomNameVisible());

        packetContainer.getWatchableCollectionModifier().write(0, watcher.getWatchableObjects());

        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, packetContainer);
        } catch (InvocationTargetException e) {
            WildStackerPlugin.log("There was an error while sending the name toggle packet to " + player.getName() + ":");
            e.printStackTrace();
        }
    }

    private static Object getCustomName(String customName){
        if(Bukkit.getBukkitVersion().contains("1.13")) {
            return Optional.of("");
        }
        return customName;
    }

    private static WrappedDataWatcher.Serializer getNameSerializer(){
        if(Bukkit.getBukkitVersion().contains("1.13"))
            WrappedDataWatcher.Registry.getChatComponentSerializer(true);
        return WrappedDataWatcher.Registry.get(String.class);
    }

}
