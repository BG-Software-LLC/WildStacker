package com.bgsoftware.wildstacker.hooks;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.utils.EntityUtil;
import com.bgsoftware.wildstacker.utils.legacy.Materials;
import net.brcdev.shopgui.ShopGuiPlugin;
import net.brcdev.shopgui.provider.spawner.SpawnerProvider;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;

public final class PluginHook_SpawnerProvider extends SpawnerProvider {

    @Override
    public SpawnerProvider hook(Plugin plugin) {
        return this;
    }

    @Override
    public ItemStack getSpawnerItem(String entityId, String customName) {
        ItemStack itemStack = Materials.SPAWNER.toBukkitItem();

        BlockStateMeta blockStateMeta = (BlockStateMeta) itemStack.getItemMeta();
        CreatureSpawner creatureSpawner = (CreatureSpawner) blockStateMeta.getBlockState();

        creatureSpawner.setSpawnedType(EntityType.valueOf(entityId.toUpperCase()));

        blockStateMeta.setBlockState(creatureSpawner);

        if(!customName.isEmpty())
            blockStateMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', customName));

        itemStack.setItemMeta(blockStateMeta);

        return itemStack;
    }

    @Override
    public String getSpawnerEntityId(ItemStack itemStack) {
        String entityId = EntityType.PIG.name();

        if(itemStack != null && itemStack.getType() == Materials.SPAWNER.toBukkitType()) {
            BlockStateMeta blockStateMeta = (BlockStateMeta) itemStack.getItemMeta();
            CreatureSpawner creatureSpawner = (CreatureSpawner) blockStateMeta.getBlockState();

            entityId = creatureSpawner.getSpawnedType().name();
        }

        return entityId;
    }

    @Override
    public String getSpawnerEntityName(ItemStack itemStack) {
        return EntityUtil.getFormattedType(getSpawnerEntityId(itemStack));
    }

    public static void register() throws Throwable{
        ShopGuiPlugin plugin = JavaPlugin.getPlugin(ShopGuiPlugin.class);
        Field field = ShopGuiPlugin.class.getDeclaredField("spawnerProvider");
        field.setAccessible(true);
        if(!Bukkit.getPluginManager().isPluginEnabled("PickUpSpawners") &&
                !Bukkit.getPluginManager().isPluginEnabled("SilkSpawners") && !Bukkit.getPluginManager().isPluginEnabled("EpicSpawners")) {
            WildStackerPlugin.log("Found ShopGUIPlus - Hooked as SpawnerProvider!");
            field.set(plugin, new PluginHook_SpawnerProvider());
        }
        field.setAccessible(false);
    }

}
