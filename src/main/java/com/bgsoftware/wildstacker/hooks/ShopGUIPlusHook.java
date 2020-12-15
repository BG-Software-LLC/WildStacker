package com.bgsoftware.wildstacker.hooks;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.utils.entity.EntityUtils;
import com.bgsoftware.wildstacker.utils.reflection.ReflectionUtils;
import net.brcdev.shopgui.ShopGuiPlusApi;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;

public final class ShopGUIPlusHook {

    private static final WildStackerPlugin plugin = WildStackerPlugin.getPlugin();

    public static void setEnabled(){
        if(ReflectionUtils.isPluginEnabled("net.brcdev.shopgui.spawner.external.provider.ExternalSpawnerProvider") ?
                new NewSpawnerProvider().register() : new OldSpawnerProvider().register()){
            WildStackerPlugin.log("Found ShopGUIPlus - Hooked as SpawnerProvider!");
        }
    }

    static class NewSpawnerProvider implements net.brcdev.shopgui.spawner.external.provider.ExternalSpawnerProvider{

        @Override
        public String getName() {
            return "WildStacker";
        }

        @Override
        public ItemStack getSpawnerItem(EntityType entityType) {
            return plugin.getProviders().getSpawnerItem(entityType, 1, "");
        }

        @Override
        public EntityType getSpawnerEntityType(ItemStack itemStack) {
            return plugin.getProviders().getSpawnerType(itemStack);
        }

        boolean register(){
            try{
                Method method = ShopGuiPlusApi.class.getMethod("registerSpawnerProvider", net.brcdev.shopgui.spawner.external.provider.ExternalSpawnerProvider.class);
                method.invoke(null, this);
                return true;
            }catch(Exception ex){
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
            return plugin.getProviders().getSpawnerItem(EntityType.valueOf(entityId), 1, "");
        }

        @Override
        public String getSpawnerEntityId(ItemStack itemStack) {
            return plugin.getProviders().getSpawnerType(itemStack).name();
        }

        @Override
        public String getSpawnerEntityName(ItemStack itemStack) {
            return EntityUtils.getFormattedType(getSpawnerEntityId(itemStack));
        }

        boolean register(){
            try{
                Method method = ShopGuiPlusApi.class.getMethod("registerSpawnerProvider", net.brcdev.shopgui.provider.spawner.SpawnerProvider.class);
                method.invoke(null, this);
                return true;
            }catch(Exception ex){
                ex.printStackTrace();
            }
            return false;
        }

    }

}
