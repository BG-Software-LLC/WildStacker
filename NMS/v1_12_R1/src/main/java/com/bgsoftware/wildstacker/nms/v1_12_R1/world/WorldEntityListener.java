package com.bgsoftware.wildstacker.nms.v1_12_R1.world;

import com.bgsoftware.wildstacker.listeners.EntitiesListener;
import net.minecraft.server.v1_12_R1.Entity;
import net.minecraft.server.v1_12_R1.IWorldAccess;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

public class WorldEntityListener {

    private static final InvocationHandler HANDLER = (proxy, method, args) -> {
        if (method.getParameterCount() == 1 && method.getParameterTypes()[0] == Entity.class && method.getName().equals("b")) {
            Entity entity = (Entity) args[0];
            EntitiesListener.IMP.handleEntityRemove(entity.getBukkitEntity());
        }

        return null;
    };

    public static final IWorldAccess LISTENER = (IWorldAccess) Proxy.newProxyInstance(IWorldAccess.class.getClassLoader(),
            new Class[]{IWorldAccess.class},
            HANDLER);

    private WorldEntityListener() {

    }

}
