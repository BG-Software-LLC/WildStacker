package com.bgsoftware.wildstacker.nms.v1_7_R4.world;

import com.bgsoftware.wildstacker.listeners.EntitiesListener;
import com.bgsoftware.wildstacker.utils.entity.EntityStorage;
import net.minecraft.server.v1_7_R4.Entity;
import net.minecraft.server.v1_7_R4.IWorldAccess;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

public class WorldEntityListener {

    private static final InvocationHandler HANDLER = (proxy, method, args) -> {
        if (method.getParameterCount() == 1 && method.getParameterTypes()[0] == Entity.class) {
            Entity entity = (Entity) args[0];
            if (method.getName().equals("a"))
                EntityStorage.cancelMetadataRemoval(entity.getBukkitEntity());
            else if (method.getName().equals("b"))
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
