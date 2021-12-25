package com.bgsoftware.wildstacker.hooks;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.utils.entity.EntityUtils;
import net.brcdev.shopgui.ShopGuiPlusApi;
import net.brcdev.shopgui.provider.spawner.SpawnerProvider;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public final class ShopGUIPlus18Hook {

    private static WildStackerPlugin plugin;

    public static void register(WildStackerPlugin plugin) {
        ShopGUIPlus18Hook.plugin = plugin;
        ShopGuiPlusApi.registerSpawnerProvider(new WildStackerSpawnerProvider());
        WildStackerPlugin.log("Found ShopGUIPlus - Hooked as SpawnerProvider!");
    }

    private static final class WildStackerSpawnerProvider extends SpawnerProvider {

        @Override
        public SpawnerProvider hook(Plugin plugin) {
            return this;
        }

        @Override
        public ItemStack getSpawnerItem(String entityId, String customName) {
            return plugin.getProviders().getSpawnersProvider().getSpawnerItem(EntityType.valueOf(entityId),
                    1, null);
        }

        @Override
        public String getSpawnerEntityId(ItemStack itemStack) {
            return plugin.getProviders().getSpawnersProvider().getSpawnerType(itemStack).name();
        }

        @Override
        public String getSpawnerEntityName(ItemStack itemStack) {
            return EntityUtils.getFormattedType(getSpawnerEntityId(itemStack));
        }

    }

}
