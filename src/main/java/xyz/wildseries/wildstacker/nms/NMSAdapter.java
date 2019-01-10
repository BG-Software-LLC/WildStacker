package xyz.wildseries.wildstacker.nms;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

public interface NMSAdapter {

    Object getNBTTagCompound(LivingEntity livingEntity);

    void setNBTTagCompound(LivingEntity livingEntity, Object _nbtTagCompound);

    boolean isInLove(Entity entity);

    void setInLove(Entity entity, boolean inLove);

}
