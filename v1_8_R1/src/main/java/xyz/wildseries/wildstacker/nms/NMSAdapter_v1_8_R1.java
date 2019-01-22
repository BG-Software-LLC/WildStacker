package xyz.wildseries.wildstacker.nms;

import net.minecraft.server.v1_8_R1.EntityAnimal;
import net.minecraft.server.v1_8_R1.EntityInsentient;
import net.minecraft.server.v1_8_R1.EntityLiving;
import net.minecraft.server.v1_8_R1.ItemStack;
import net.minecraft.server.v1_8_R1.NBTTagCompound;
import net.minecraft.server.v1_8_R1.PathfinderGoalBreed;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftAnimals;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_8_R1.inventory.CraftItemStack;
import org.bukkit.entity.Animals;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Zombie;
import xyz.wildseries.wildstacker.listeners.events.EntityBreedEvent;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@SuppressWarnings("unused")
public final class NMSAdapter_v1_8_R1 implements NMSAdapter {

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

        nbtTagCompound.setFloat("HealF", 20);
        nbtTagCompound.setFloat("Health", 20);
        nbtTagCompound.remove("SaddleItem");
        nbtTagCompound.remove("ArmorItem");
        nbtTagCompound.remove("Equipment");
        nbtTagCompound.remove("DropChances");
        if(livingEntity instanceof Zombie)
            ((Zombie) livingEntity).setBaby(nbtTagCompound.hasKey("IsBaby") && nbtTagCompound.getBoolean("IsBaby"));

        entityLiving.a(nbtTagCompound);
    }

    @Override
    public boolean isInLove(org.bukkit.entity.Entity entity) {
        EntityAnimal nmsEntity = (EntityAnimal) ((CraftEntity) entity).getHandle();
        return nmsEntity.cp();
    }

    @Override
    public void setInLove(org.bukkit.entity.Entity entity, boolean inLove) {
        EntityAnimal nmsEntity = (EntityAnimal) ((CraftEntity) entity).getHandle();
        nmsEntity.cq();
    }

    @Override
    public List<org.bukkit.inventory.ItemStack> getEquipment(LivingEntity livingEntity) {
        List<org.bukkit.inventory.ItemStack> equipment = new ArrayList<>();
        EntityInsentient entityLiving = (EntityInsentient) ((CraftLivingEntity) livingEntity).getHandle();

        for(int i = 0; i < entityLiving.getEquipment().length; i++){
            try {
                ItemStack itemStack = entityLiving.getEquipment(i);
                double dropChance = entityLiving.dropChances[i];

                if (itemStack != null && (livingEntity.getKiller() != null || dropChance > 1) && random.nextFloat() - (float) i * 0.01F < dropChance) {
                    if (dropChance <= 1 && itemStack.e()) {
                        int maxData = Math.max(itemStack.j() - 25, 1);
                        int data = itemStack.j() - this.random.nextInt(this.random.nextInt(maxData) + 1);

                        if (data > maxData) {
                            data = maxData;
                        }

                        if (data < 1) {
                            data = 1;
                        }
                        itemStack.setData(data);
                    }
                    equipment.add(CraftItemStack.asBukkitCopy(itemStack));
                }

                if (dropChance >= 1)
                    entityLiving.dropChances[i] = 0;
            }catch(Exception ignored){}
        }

        return equipment;
    }

    @Override
    public void addCustomPathfinderGoalBreed(LivingEntity livingEntity) {
        if(livingEntity instanceof Animals) {
            EntityAnimal entityLiving = ((CraftAnimals) livingEntity).getHandle();
            entityLiving.goalSelector.a(2, new EventablePathfinderGoalBreed(entityLiving, 1.0D));
        }
    }

    private class EventablePathfinderGoalBreed extends PathfinderGoalBreed{

        private EventablePathfinderGoalBreed(EntityAnimal entityanimal, double d0) {
            super(entityanimal, d0);
        }

        @Override
        public void e() {
            super.e();
            try {
                Field bField = PathfinderGoalBreed.class.getDeclaredField("b");
                bField.setAccessible(true);
                int b = (int) bField.get(this);
                bField.setAccessible(false);

                Field animalField = PathfinderGoalBreed.class.getDeclaredField("d");
                animalField.setAccessible(true);
                EntityAnimal animal = (EntityAnimal) animalField.get(this);
                animalField.setAccessible(false);

                Field partnerField = PathfinderGoalBreed.class.getDeclaredField("e");
                partnerField.setAccessible(true);
                EntityAnimal partner = (EntityAnimal) partnerField.get(this);
                partnerField.setAccessible(false);

                if (b >= 60 && animal.h(partner) < 9.0D){
                    EntityBreedEvent event = new EntityBreedEvent((LivingEntity) animal.getBukkitEntity(), (LivingEntity) partner.getBukkitEntity());
                    Bukkit.getPluginManager().callEvent(event);
                }
            }catch(Exception ex){
                ex.printStackTrace();
            }
        }
    }

}
