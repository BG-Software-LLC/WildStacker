package xyz.wildseries.wildstacker.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Animals;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.MushroomCow;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTransformEvent;
import org.bukkit.event.entity.SheepDyeWoolEvent;
import org.bukkit.event.entity.SheepRegrowWoolEvent;
import org.bukkit.event.entity.SlimeSplitEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.ItemStack;
import xyz.wildseries.wildstacker.WildStackerPlugin;
import xyz.wildseries.wildstacker.api.enums.StackSplit;
import xyz.wildseries.wildstacker.api.objects.StackedEntity;
import xyz.wildseries.wildstacker.hooks.CrazyEnchantmentsHook;
import xyz.wildseries.wildstacker.listeners.events.EntityBreedEvent;
import xyz.wildseries.wildstacker.objects.WStackedEntity;
import xyz.wildseries.wildstacker.utils.EntityUtil;
import xyz.wildseries.wildstacker.utils.ItemUtil;
import xyz.wildseries.wildstacker.utils.SafeStacker;
import xyz.wildseries.wildstacker.utils.async.AsyncCallback;
import xyz.wildseries.wildstacker.utils.legacy.Materials;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@SuppressWarnings("unused")
public final class EntitiesListener implements Listener {

    private WildStackerPlugin plugin;

    public static Set<UUID> noStackEntities = new HashSet<>();

    public EntitiesListener(WildStackerPlugin plugin){
        this.plugin = plugin;
        if(plugin.getServer().getBukkitVersion().contains("1.13"))
            plugin.getServer().getPluginManager().registerEvents(new EntitiesListener1_13(), plugin);
    }

    //This method will be fired even if stacking-entities is disabled.
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent e){
        if(e.getEntityType() == EntityType.ARMOR_STAND || e.getEntityType() == EntityType.PLAYER)
            return;

        if(plugin.getSettings().blacklistedEntities.contains(e.getEntityType().name()))
            return;

        if(plugin.getSettings().entitiesDisabledWorlds.contains(e.getEntity().getWorld().getName()))
            return;

        StackedEntity stackedEntity = WStackedEntity.of(e.getEntity());

        if(stackedEntity.isIgnoreDeathEvent()) {
            e.getDrops().clear();
            return;
        }

        EntityDamageEvent.DamageCause lastDamageCause = EntityDamageEvent.DamageCause.CUSTOM;

        if(e.getEntity().getLastDamageCause() != null) {
            lastDamageCause = e.getEntity().getLastDamageCause().getCause();
        }

        int amount = stackedEntity.getStackAmount();

        int lootBonusLevel = 0;
        ItemStack killerItemHand = null;

        if(lastDamageCause == EntityDamageEvent.DamageCause.ENTITY_ATTACK && e.getEntity().getKiller() != null &&
                (killerItemHand = e.getEntity().getKiller().getItemInHand()) != null){
            lootBonusLevel = e.getEntity().getKiller().getItemInHand().getEnchantmentLevel(Enchantment.LOOT_BONUS_MOBS);
        }

        List<ItemStack> drops = new ArrayList<>();

        final int LOOT_BONUS_LEVEL = lootBonusLevel;

        if(!plugin.getSettings().entitiesInstantKills.contains(lastDamageCause.name())){
            Bukkit.getScheduler().runTaskLater(plugin, () -> stackedEntity.tryUnstack(1), 1L);
            new Thread(() -> {
                drops.addAll(stackedEntity.getDrops(LOOT_BONUS_LEVEL, 1));
                Bukkit.getScheduler().runTask(plugin, () ->
                    drops.forEach(itemStack -> ItemUtil.dropItem(itemStack, e.getEntity().getLocation())));
            }).start();
        }

        //Instant-kill drops should only work when entities-stacking is enabled
        else{
            new Thread(() -> {
                drops.addAll(stackedEntity.getDrops(LOOT_BONUS_LEVEL));
                Bukkit.getScheduler().runTask(plugin, () ->
                        drops.forEach(itemStack -> ItemUtil.dropItem(itemStack, e.getEntity().getLocation())));
            }).start();

            stackedEntity.tryUnstack(stackedEntity.getStackAmount());

            e.setDroppedExp(CrazyEnchantmentsHook.getNewExpValue(e.getDroppedExp() * amount, killerItemHand));

            if(e.getEntity().getKiller() != null && amount - 1 > 0) {
                try {
                    e.getEntity().getKiller().incrementStatistic(Statistic.MOB_KILLS, amount - 1);
                    e.getEntity().getKiller().incrementStatistic(Statistic.KILL_ENTITY, stackedEntity.getType(), amount - 1);
                }catch(IllegalArgumentException ignored){}
            }
        }

        e.getDrops().clear();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntitySpawn(CreatureSpawnEvent e){
        if(!plugin.getSettings().entitiesStackingEnabled)
            return;

        if(e.getEntityType() == EntityType.ARMOR_STAND || e.getEntityType() == EntityType.PLAYER)
            return;

        if(plugin.getSettings().blacklistedEntities.contains(e.getEntityType().name()) ||
                plugin.getSettings().blacklistedEntitiesSpawnReasons.contains(e.getSpawnReason().name()))
            return;

        if(plugin.getSettings().nerfedSpawning.contains(e.getSpawnReason().name()))
            EntityUtil.nerfEntity(e.getEntity());

        if(noStackEntities.contains(e.getEntity().getUniqueId())) {
            noStackEntities.remove(e.getEntity().getUniqueId());
            return;
        }

        //EpicSpawners has it's own event
        if(e.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER &&
                Bukkit.getPluginManager().isPluginEnabled("EpicSpawners"))
            return;

        if (!Bukkit.getPluginManager().isPluginEnabled("MergedSpawner") &&
                plugin.getSettings().linkedEntitiesEnabled && e.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER)
            return;

        StackedEntity stackedEntity = WStackedEntity.of(e.getEntity());

        SafeStacker.tryStack(stackedEntity);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSpawnerEggUse(PlayerInteractEvent e){
        if(!plugin.getSettings().entitiesStackingEnabled || e.getItem() == null || e.getAction() != Action.RIGHT_CLICK_BLOCK ||
                plugin.getSettings().blacklistedEntitiesSpawnReasons.contains("SPAWNER_EGG") || !Materials.isValidAndSpawnEgg(e.getItem()))
            return;

        int eggAmount = ItemUtil.getSpawnerItemAmount(e.getItem());

        if(eggAmount > 1){
            EntityType entityType = ItemUtil.getEntityType(e.getItem());

            Block spawnBlock = e.getClickedBlock().getRelative(e.getBlockFace());

            StackedEntity stackedEntity = WStackedEntity.of(
                    plugin.getSystemManager().spawnEntityWithoutStacking(spawnBlock.getLocation().add(0.5, 1, 0.5), entityType.getEntityClass(),
                            CreatureSpawnEvent.SpawnReason.SPAWNER_EGG));

            stackedEntity.setStackAmount(--eggAmount, true);

            Bukkit.getScheduler().runTaskLater(plugin, () -> SafeStacker.tryStack(stackedEntity), 5L);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onSlimeSplit(SlimeSplitEvent e){
        //Fixes a async catch exception with auto-clear task
        if(!Bukkit.isPrimaryThread())
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSheepDye(SheepDyeWoolEvent e){
        if(!plugin.getSettings().entitiesStackingEnabled)
            return;

        StackedEntity stackedEntity = WStackedEntity.of(e.getEntity());

        if(!StackSplit.SHEEP_DYE.isEnabled())
            return;

        int amount = stackedEntity.getStackAmount();

        if (amount > 1)
            stackedEntity.spawnDuplicate(amount - 1);

        e.getEntity().setColor(e.getColor());
        stackedEntity.setStackAmount(1, false);
        SafeStacker.tryStack(stackedEntity);
    }


    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerShear(PlayerShearEntityEvent e){
        if(!plugin.getSettings().entitiesStackingEnabled || !(e.getEntity() instanceof LivingEntity))
            return;

        if(!StackSplit.MUSHROOM_SHEAR.isEnabled() && e.getEntity() instanceof MushroomCow)
            return;

        if(!StackSplit.SHEEP_SHEAR.isEnabled() && e.getEntity() instanceof Sheep)
            return;

        StackedEntity stackedEntity = WStackedEntity.of(e.getEntity());

        if(e.getEntity() instanceof Sheep || e.getEntity() instanceof MushroomCow) {
            stackedEntity.spawnDuplicate(stackedEntity.getStackAmount() - 1);
            if(e.getEntity() instanceof MushroomCow) {
                stackedEntity.remove();
            }
            else{
                ((Sheep) e.getEntity()).setSheared(true);
                stackedEntity.setStackAmount(1, false);
                SafeStacker.tryStack(stackedEntity);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onWoolRegrow(SheepRegrowWoolEvent e){
        if(!plugin.getSettings().entitiesStackingEnabled)
            return;

        StackedEntity stackedEntity = WStackedEntity.of(e.getEntity());

        int amount = stackedEntity.getStackAmount();

        if(!e.getEntity().isSheared())
            return;

        if(amount > 1){
            Sheep sheared = (Sheep) stackedEntity.spawnDuplicate(amount - 1).getLivingEntity();
            sheared.setSheared(true);
            stackedEntity.setStackAmount(1, false);
        }
        e.getEntity().setSheared(false);
        SafeStacker.tryStack(stackedEntity);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onNameTagOrSaddle(PlayerInteractAtEntityEvent e){
        if(!plugin.getSettings().entitiesStackingEnabled || !(e.getRightClicked() instanceof LivingEntity) || !StackSplit.NAME_TAG.isEnabled())
            return;

        ItemStack inHand = e.getPlayer().getInventory().getItemInHand();
        StackedEntity stackedEntity = WStackedEntity.of(e.getRightClicked());

        if(inHand.getType() == Material.NAME_TAG){
            if(!inHand.hasItemMeta() || !inHand.getItemMeta().hasDisplayName() || e.getRightClicked() instanceof EnderDragon || e.getRightClicked() instanceof Player)
                return;
        }else if(inHand.getType() == Material.SADDLE){
            if(!(e.getRightClicked() instanceof Pig))
                return;
        }else return;

        stackedEntity.spawnDuplicate(stackedEntity.getStackAmount() - 1);

        stackedEntity.setStackAmount(1, false);
        stackedEntity.setCustomName(inHand.getItemMeta().getDisplayName());
        stackedEntity.setCustomNameVisible(false);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityFeed(PlayerInteractAtEntityEvent e){
        if(!StackSplit.ENTITY_BREED.isEnabled())
            return;

        if(!(e.getRightClicked() instanceof Animals))
            return;

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if(!plugin.getNMSAdapter().isInLove(e.getRightClicked()))
                return;

            StackedEntity stackedEntity = WStackedEntity.of(e.getRightClicked());

            if(stackedEntity.getStackAmount() > 1){
                StackedEntity duplicated = stackedEntity.spawnDuplicate(stackedEntity.getStackAmount() - 1);
                plugin.getNMSAdapter().setInLove(duplicated.getLivingEntity(), false);
                stackedEntity.setStackAmount(1, true);
            }

        }, 1L);
    }

    @EventHandler
    public void onEntityBreed(EntityBreedEvent e){
        if(plugin.getSettings().stackAfterBreed) {
            SafeStacker.tryStackInto(WStackedEntity.of(e.getFather()), WStackedEntity.of(e.getMother()), new AsyncCallback<Boolean>() {
                @Override
                public void run(Boolean returnValue) {
                    if(returnValue) {
                        LivingEntity livingEntity = WStackedEntity.of(e.getMother()).tryStack();
                        if(livingEntity != null)
                            ((Animals) livingEntity).setBreed(false);
                    }
                }
            });
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent e){
        if(e.getEntity() instanceof Animals)
            try{
                plugin.getNMSAdapter().addCustomPathfinderGoalBreed(e.getEntity());
            }catch(UnsupportedOperationException ignored){}
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent e){
        for(Entity entity : e.getChunk().getEntities()){
            if(entity instanceof LivingEntity && !(entity instanceof Player) && !(entity instanceof ArmorStand)){
                WStackedEntity stackedEntity = (WStackedEntity) WStackedEntity.of(entity);
                if(stackedEntity.isNerfed())
                    EntityUtil.nerfEntity((LivingEntity) entity);
            }
        }
    }

    class EntitiesListener1_13 implements Listener {

        @EventHandler
        public void onEntityTransform(EntityTransformEvent e){
            StackedEntity stackedEntity = WStackedEntity.of(e.getEntity());

            if(stackedEntity.getStackAmount() > 1){
                StackedEntity transformed = WStackedEntity.of(e.getTransformedEntity());
                transformed.setStackAmount(stackedEntity.getStackAmount(), true);
                stackedEntity.remove();
            }
        }

    }

}
