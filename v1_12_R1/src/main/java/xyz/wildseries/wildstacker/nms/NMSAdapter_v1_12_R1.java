package xyz.wildseries.wildstacker.nms;

import net.minecraft.server.v1_12_R1.EnchantmentManager;
import net.minecraft.server.v1_12_R1.EntityAnimal;
import net.minecraft.server.v1_12_R1.EntityInsentient;
import net.minecraft.server.v1_12_R1.EntityLiving;
import net.minecraft.server.v1_12_R1.EnumItemSlot;
import net.minecraft.server.v1_12_R1.ItemStack;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Zombie;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@SuppressWarnings("unused")
public final class NMSAdapter_v1_12_R1 implements NMSAdapter {

    private ThreadLocalRandom random = ThreadLocalRandom.current();

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

    @Override
    public List<org.bukkit.inventory.ItemStack> getEquipment(LivingEntity livingEntity) {
        List<org.bukkit.inventory.ItemStack> equipment = new ArrayList<>();
        EntityInsentient entityLiving = (EntityInsentient) ((CraftLivingEntity) livingEntity).getHandle();

        EnumItemSlot[] enumItemSlots = EnumItemSlot.values();

        for(int i = 0; i < enumItemSlots.length; i++){
            EnumItemSlot slot = enumItemSlots[i];
            ItemStack itemStack = entityLiving.getEquipment(slot);
            float dropChance = slot.a().ordinal() == 1 ? entityLiving.dropChanceHand[slot.b()] : slot.a().ordinal() == 2 ? entityLiving.dropChanceArmor[slot.b()] : 0;

            if(!itemStack.isEmpty() && !EnchantmentManager.shouldNotDrop(itemStack) && (livingEntity.getKiller() != null || dropChance > 1) &&
                    random.nextFloat() - (float)i * 0.01F < dropChance){
                if(dropChance <= 1 && itemStack.f())
                    itemStack.setData(itemStack.k() - random.nextInt(1 + random.nextInt(Math.max(itemStack.k() - 3, 1))));
                equipment.add(CraftItemStack.asBukkitCopy(itemStack));
            }

            if(dropChance >= 1) {
                if(slot.a() == EnumItemSlot.Function.HAND)
                    entityLiving.dropChanceHand[slot.b()] = 0;
                else if(slot.a() == EnumItemSlot.Function.ARMOR)
                    entityLiving.dropChanceArmor[slot.b()] = 0;
            }
        }

        return equipment;
    }


}
