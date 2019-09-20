package com.bgsoftware.wildstacker.hooks;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.utils.entity.EntityUtils;
import com.bgsoftware.wildstacker.utils.items.ItemUtils;
import com.bgsoftware.wildstacker.utils.legacy.Materials;
import com.bgsoftware.wildstacker.utils.reflection.ReflectionUtils;
import net.brcdev.shopgui.ShopGuiPlusApi;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class PluginHook_SpawnerProvider {

    public static void register(){
        if (!Bukkit.getPluginManager().isPluginEnabled("PickUpSpawners") &&
                !Bukkit.getPluginManager().isPluginEnabled("SilkSpawners") && !Bukkit.getPluginManager().isPluginEnabled("EpicSpawners")) {
            if(ReflectionUtils.isPluginEnabled("net.brcdev.shopgui.spawner.external.provider.ExternalSpawnerProvider") ?
                    new NewSpawnerProvider().register() : new OldSpawnerProvider().register()){
                WildStackerPlugin.log("Found ShopGUIPlus - Hooked as SpawnerProvider!");
            }
        }
    }

    static class NewSpawnerProvider implements net.brcdev.shopgui.spawner.external.provider.ExternalSpawnerProvider{

        @Override
        public String getName() {
            return "WildStacker-Spawners";
        }

        @Override
        public ItemStack getSpawnerItem(EntityType entityType) {
            return ItemUtils.getSpawnerItem(entityType, 1);
        }

        @Override
        public EntityType getSpawnerEntityType(ItemStack itemStack) {
            EntityType entityType = EntityType.UNKNOWN;

            if(itemStack != null && itemStack.getType() == Materials.SPAWNER.toBukkitType()) {
                BlockStateMeta blockStateMeta = (BlockStateMeta) itemStack.getItemMeta();
                CreatureSpawner creatureSpawner = (CreatureSpawner) blockStateMeta.getBlockState();
                entityType = creatureSpawner.getSpawnedType();
            }

            return entityType;
        }

        boolean register(){
            WildStackerPlugin.log("New Spawners Provider");
            try {
                //noinspection JavaReflectionMemberAccess
                Method method = ShopGuiPlusApi.class.getMethod("registerSpawnerProvider", net.brcdev.shopgui.spawner.external.provider.ExternalSpawnerProvider.class);
                method.invoke(null, this);
                return true;
            } catch (InvocationTargetException ex) {
                ex.getTargetException().printStackTrace();
            } catch(Exception ex){
                ex.printStackTrace();
            }
            return false;
        }

    }

    static class OldSpawnerProvider extends net.brcdev.shopgui.provider.spawner.SpawnerProvider{

        @Override
        public net.brcdev.shopgui.provider.spawner.SpawnerProvider hook(Plugin plugin) {
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
            return EntityUtils.getFormattedType(getSpawnerEntityId(itemStack));
        }

        boolean register(){
            ShopGuiPlusApi.registerSpawnerProvider(this);
            return true;
        }

    }

}
