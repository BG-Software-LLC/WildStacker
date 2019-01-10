package xyz.wildseries.wildstacker.loot;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import xyz.wildseries.wildstacker.utils.legacy.EntityTypes;
import xyz.wildseries.wildstacker.utils.legacy.Materials;

import java.util.List;

public class LootTableCreeper extends LootTable{

    private static Materials[] records = new Materials[]{
            Materials.MUSIC_DISC_13, Materials.MUSIC_DISC_CAT, Materials.MUSIC_DISC_BLOCKS, Materials.MUSIC_DISC_CHIRP,
            Materials.MUSIC_DISC_FAR, Materials.MUSIC_DISC_MALL, Materials.MUSIC_DISC_MELLOHI, Materials.MUSIC_DISC_STAL,
            Materials.MUSIC_DISC_STRAD, Materials.MUSIC_DISC_WARD, Materials.MUSIC_DISC_11, Materials.MUSIC_DISC_WAIT};

    @Override
    public int getMaximumAmount() {
        return 2;
    }

    @Override
    public int getMinimumAmount() {
        return 0;
    }

    @Override
    public ItemStack getLoot() {
        return Materials.GUNPOWDER.toBukkitItem();
    }

    @Override
    public List<ItemStack> getDeathLoot(int lootBonusLevel) {
        List<ItemStack> deathLoot = super.getDeathLoot(lootBonusLevel);

        if(livingEntity.getLastDamageCause() instanceof EntityDamageByEntityEvent){
            EntityDamageByEntityEvent event = (EntityDamageByEntityEvent) livingEntity.getLastDamageCause();
            if(event.getDamager() instanceof Projectile && ((Projectile) event.getDamager()).getShooter() instanceof LivingEntity){
                LivingEntity shooter = (LivingEntity) ((Projectile) event.getDamager()).getShooter();
                EntityTypes entityTypes = EntityTypes.fromEntity(shooter);
                if(entityTypes == EntityTypes.SKELETON || entityTypes == EntityTypes.STRAY) {
                    for (int i = 0; i < getStackAmount(); i++) {
                        deathLoot.add(records[this.random.nextInt(records.length)].toBukkitItem());
                    }
                }
            }
        }

        return deathLoot;
    }
}
