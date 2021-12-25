package com.bgsoftware.wildstacker.hooks;

import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.utils.entity.EntityUtils;
import net.brcdev.shopgui.ShopGuiPlusApi;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public final class ShopGUIPlusHook {

    private static final WildStackerPlugin plugin = WildStackerPlugin.getPlugin();

    public static void setEnabled() {
        boolean registered;

        try {
            Class.forName("net.brcdev.shopgui.spawner.external.provider.ExternalSpawnerProvider");
            registered = new NewSpawnerProvider().register();
        } catch (Throwable ex) {
            registered = new OldSpawnerProvider().register();
        }

        if (registered)
            WildStackerPlugin.log("Found ShopGUIPlus - Hooked as SpawnerProvider!");
    }

    static class NewSpawnerProvider implements net.brcdev.shopgui.spawner.external.provider.ExternalSpawnerProvider {

        private static final ReflectMethod<Void> REGISTER_SPAWNER_PROVIDER = new ReflectMethod<>(ShopGuiPlusApi.class, "registerSpawnerProvider", net.brcdev.shopgui.spawner.external.provider.ExternalSpawnerProvider.class);

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

        boolean register() {
            try {
                REGISTER_SPAWNER_PROVIDER.invoke(null, this);
                return true;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return false;
        }

    }

    static class OldSpawnerProvider extends net.brcdev.shopgui.provider.spawner.SpawnerProvider {

        private static final ReflectMethod<Void> REGISTER_SPAWNER_PROVIDER = new ReflectMethod<>(ShopGuiPlusApi.class, "registerSpawnerProvider", net.brcdev.shopgui.provider.spawner.SpawnerProvider.class);

        @Override
        public net.brcdev.shopgui.provider.spawner.SpawnerProvider hook(Plugin plugin) {
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

        boolean register() {
            try {
                REGISTER_SPAWNER_PROVIDER.invoke(null, this);
                return true;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return false;
        }

    }

}
