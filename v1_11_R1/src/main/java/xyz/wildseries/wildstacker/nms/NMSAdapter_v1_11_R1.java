package xyz.wildseries.wildstacker.nms;

import net.minecraft.server.v1_11_R1.EntityAnimal;
import net.minecraft.server.v1_11_R1.EntityLiving;
import net.minecraft.server.v1_11_R1.NBTTagCompound;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftLivingEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Zombie;

@SuppressWarnings("unused")
public final class NMSAdapter_v1_11_R1 implements NMSAdapter {

    @Override
    public Object getNBTTagCompound(LivingEntity livingEntity) {
        EntityLiving entityLiving = ((CraftLivingEntity) livingEntity).getHandle();
        NBTTagCompound nbtTagCompound = new NBTTagCompound();
        entityLiving.b(nbtTagCompound);
        return nbtTagCompound;
    }

    @Override
    public void setNBTTagCompound(LivingEntity livingEntity, Object _nbtTagCompound) {
        if(!(_nbtTagCompound instanceof NBTTagCompound))
            return;

        EntityLiving entityLiving = ((CraftLivingEntity) livingEntity).getHandle();
        NBTTagCompound nbtTagCompound = (NBTTagCompound) _nbtTagCompound;

        nbtTagCompound.setFloat("Health", 20);
        nbtTagCompound.remove("SaddleItem");
        nbtTagCompound.remove("ArmorItem");
        nbtTagCompound.remove("ArmorItems");
        nbtTagCompound.remove("HandItems");
        if(livingEntity instanceof Zombie)
            ((Zombie) livingEntity).setBaby(nbtTagCompound.hasKey("IsBaby") && nbtTagCompound.getBoolean("IsBaby"));

        entityLiving.a(nbtTagCompound);
    }

    @Override
    public boolean isInLove(org.bukkit.entity.Entity entity) {
        EntityAnimal nmsEntity = (EntityAnimal) ((CraftEntity) entity).getHandle();
        return nmsEntity.isInLove();
    }

    @Override
    public void setInLove(org.bukkit.entity.Entity entity, boolean inLove) {
        EntityAnimal nmsEntity = (EntityAnimal) ((CraftEntity) entity).getHandle();
        nmsEntity.resetLove();
    }

}
