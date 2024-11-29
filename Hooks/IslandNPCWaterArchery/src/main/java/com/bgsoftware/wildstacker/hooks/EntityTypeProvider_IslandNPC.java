package com.bgsoftware.wildstacker.hooks;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

public final class EntityTypeProvider_IslandNPC implements EntityTypeProvider {

    private final NamespacedKey npcKey;

    public EntityTypeProvider_IslandNPC() {
        Plugin islandNPCPlugin = Bukkit.getPluginManager().getPlugin("IslandNPC");
        npcKey = islandNPCPlugin == null ? null : new NamespacedKey(islandNPCPlugin, "IslandNPC");
    }

    @Override
    public String checkStackEntity(Entity entity) {
        if (npcKey == null || !entity.getPersistentDataContainer().has(npcKey, PersistentDataType.INTEGER))
            return null;

        return "Cannot get a stacked entity from an NPC of IslandNPC.";
    }

}
