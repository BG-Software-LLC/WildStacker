package com.bgsoftware.wildstacker.hooks;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.WrappedDataValue;
import org.bukkit.entity.Entity;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public final class ProtocolLib_119PacketHandler implements ProtocolLibHook.IPacketHandler {

    private static final WildStackerPlugin plugin = WildStackerPlugin.getPlugin();

    @Override
    public void handlePacketCustomNameUpdate(PacketContainer packetContainer, @Nullable Entity entity) {
        StructureModifier<List<WrappedDataValue>> structureModifier =
                packetContainer.getDataValueCollectionModifier();

        List<WrappedDataValue> wrappedDataValues;

        if (structureModifier.size() >= 1) {
            wrappedDataValues = structureModifier.read(0);
            wrappedDataValues.removeIf(wrappedDataValue ->
                    wrappedDataValue.getIndex() == 2 || wrappedDataValue.getIndex() == 3);
        } else {
            wrappedDataValues = new LinkedList<>();
        }

        Optional<?> customName = entity == null ? Optional.empty() :
                parseCustomName(plugin.getNMSEntities().getCustomName(entity, true));
        boolean nameVisible = entity != null && plugin.getNMSEntities().isCustomNameVisible(entity);

        wrappedDataValues.add(new WrappedDataValue(2, ProtocolLibHook.NAME_SERIALIZER, customName));
        wrappedDataValues.add(new WrappedDataValue(3, ProtocolLibHook.VISIBLE_SERIALIZER, nameVisible));

        structureModifier.write(0, wrappedDataValues);
    }

    private Optional<?> parseCustomName(@Nullable String customName) {
        return customName == null || customName.isEmpty() ? Optional.empty() :
                Optional.of(plugin.getNMSAdapter().getChatMessage(customName));
    }


}
