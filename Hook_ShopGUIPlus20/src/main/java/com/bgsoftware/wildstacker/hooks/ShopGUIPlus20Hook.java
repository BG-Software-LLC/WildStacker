package com.bgsoftware.wildstacker.hooks;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import net.brcdev.shopgui.ShopGuiPlusApi;
import net.brcdev.shopgui.spawner.external.provider.ExternalSpawnerProvider;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

@SuppressWarnings("unused")
public final class ShopGUIPlus20Hook {

    private static WildStackerPlugin plugin;

    public static void register(WildStackerPlugin plugin) {
        ShopGUIPlus20Hook.plugin = plugin;
        ShopGuiPlusApi.registerSpawnerProvider(new WildStackerSpawnerProvider());
        WildStackerPlugin.log("Found ShopGUIPlus - Hooked as SpawnerProvider!");
    }

    private static final class WildStackerSpawnerProvider implements ExternalSpawnerProvider {

        @Override
        public String getName() {
            return "WildStacker";
        }

        @Override
        public ItemStack getSpawnerItem(EntityType entityType) {
            return plugin.getProviders().getSpawnersProvider().getSpawnerItem(entityType, 1, null);
        }

        @Override
        public EntityType getSpawnerEntityType(ItemStack itemStack) {
            return plugin.getProviders().getSpawnersProvider().getSpawnerType(itemStack);
        }

    }

}
