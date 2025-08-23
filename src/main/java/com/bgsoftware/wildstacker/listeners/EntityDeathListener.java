package com.bgsoftware.wildstacker.listeners;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.enums.EntityFlag;
import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.config.section.EntitiesSection;
import com.bgsoftware.wildstacker.objects.WStackedEntity;
import com.bgsoftware.wildstacker.utils.GeneralUtils;
import com.bgsoftware.wildstacker.utils.ServerVersion;
import com.bgsoftware.wildstacker.utils.entity.EntityDamageData;
import com.bgsoftware.wildstacker.utils.entity.EntityStorage;
import com.bgsoftware.wildstacker.utils.entity.EntityUtils;
import com.bgsoftware.wildstacker.utils.entity.logic.DeathSimulation;
import com.bgsoftware.wildstacker.utils.events.HandlerListWrapper;
import com.bgsoftware.wildstacker.utils.threads.Executor;
import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;

public class EntityDeathListener implements Listener {

    private static final Map<EntityDamageEvent.DamageModifier, ? extends Function<? super Double, Double>> damageModifiersFunctions =
            Maps.newEnumMap(ImmutableMap.of(EntityDamageEvent.DamageModifier.BASE, Functions.constant(-0.0D)));

    private final Map<EntityDamageEvent, EntityDamageData> damageResults = new IdentityHashMap<>();

    private final WildStackerPlugin plugin;

    public EntityDeathListener(WildStackerPlugin plugin) {
        this.plugin = plugin;

        // We register the event in a delay so it will be the last listener to be called.
        // We want to restore the event after all the other ones will be called on the wanted data.
        Executor.sync(() -> plugin.getServer().getPluginManager().registerEvents(
                new EntityDamageRestoreListener(), plugin), 10L);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCustomEntityDeath(EntityDeathEvent e) {
        //Checks if the entity is not a corpse.
        if (EntityStorage.hasMetadata(e.getEntity(), EntityFlag.CORPSE) || !EntityUtils.isStackable(e.getEntity()))
            return;

        StackedEntity stackedEntity = WStackedEntity.of(e.getEntity());

        if (stackedEntity.hasFlag(EntityFlag.DEAD_ENTITY)) {
            return;
        }

        stackedEntity.setDrops(e.getDrops()); // Fixing issues with plugins changing the drops in this event.
        stackedEntity.setFlag(EntityFlag.EXP_TO_DROP, e.getDroppedExp());

        //Calling the onEntityLastDamage function with default parameters.
        handleEntityDamage(createDamageEvent(e.getEntity(), EntityDamageEvent.DamageCause.CUSTOM,
                e.getEntity().getHealth(), null), true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void handleEntityDamage(EntityDamageEvent e) {
        handleEntityDamage(e, false);
    }

    private void handleEntityDamage(EntityDamageEvent damageEvent, boolean fromDeathEvent) {
        // Making sure the entity is stackable and that we can proceed with handling entity damage
        if (damageEvent.isCancelled() || !plugin.getSettings().getEntities().isEnabled() ||
                !EntityUtils.isStackable(damageEvent.getEntity())) {
            // If not, we still want to re-call the damage event
            recallDamageEvent(damageEvent);
            return;
        }

        LivingEntity livingEntity = (LivingEntity) damageEvent.getEntity();
        StackedEntity stackedEntity = WStackedEntity.of(livingEntity);

        // If the entity is already considered as "dead", then we don't deal any damage and return.
        if (stackedEntity.hasFlag(EntityFlag.DEAD_ENTITY) || stackedEntity.hasFlag(EntityFlag.REMOVED_ENTITY)) {
            damageEvent.setDamage(0);
            return;
        }

        // We call a fake event and cancel the original one, if needed.

        EntityDamageData entityDamageData = null;

        if (!fromDeathEvent) {
            damageEvent.setCancelled(true);
            entityDamageData = new EntityDamageData(damageEvent);
            recallDamageEvent(damageEvent);
        }

        if (!damageEvent.isCancelled())
            damageEvent.getEntity().setLastDamageCause(damageEvent);

        try {
            EntityDamageData damageResult = handleEntityDamageInternal(damageEvent, stackedEntity, fromDeathEvent);

            // We want to restore the original values of the event.
            // If we were called from the death event, we restore it now.
            // Otherwise, the values will be restored in #onEntityDamageMonitor
            // Reminder: The original event in that case is cancelled, therefore no other plugins should touch it.
            if (fromDeathEvent) {
                restoreDamageResult(damageResult, damageEvent);
            } else {
                this.damageResults.put(damageEvent, damageResult);
            }
        } finally {
            if (entityDamageData != null)
                entityDamageData.applyToEvent(damageEvent);
        }
    }

    private EntityDamageData handleEntityDamageInternal(EntityDamageEvent damageEvent, StackedEntity stackedEntity, boolean fromDeathEvent) {
        if (damageEvent.isCancelled())
            return new EntityDamageData(true, 0);

        Entity directDamager = EntityUtils.getDamagerFromEvent(damageEvent, true, true);
        Entity sourceDamager = EntityUtils.getDamagerFromEvent(damageEvent, true, false);

        boolean shouldSimulateDeath;

        if (stackedEntity.getHealth() - damageEvent.getFinalDamage() <= 0) {
            shouldSimulateDeath = true;
        } else if (sourceDamager instanceof Player) {
            ItemStack damagerTool = ((Player) sourceDamager).getItemInHand();
            // In case the entity has enough health to deal with the damage, we check for one shot.
            boolean hasAvoidOneShot = stackedEntity.getAndRemoveFlag(EntityFlag.AVOID_ONE_SHOT) != null;
            shouldSimulateDeath = !hasAvoidOneShot &&
                    plugin.getSettings().getEntities().isOneShotEnabled() &&
                    GeneralUtils.contains(((EntitiesSection) plugin.getSettings().getEntities()).getOneShotWhitelist(), stackedEntity) &&
                    plugin.getSettings().getEntities().getOneShotTools().contains(damagerTool.getType().toString());
        } else {
            shouldSimulateDeath = false;
        }

        if (!shouldSimulateDeath) {
            // The entity will not be killed, therefore the damage result should be identical to the event's results.
            return new EntityDamageData(damageEvent);
        }

        return DeathSimulation.simulateDeath(stackedEntity, damageEvent,
                directDamager, sourceDamager, fromDeathEvent).setShouldEntityDie();
    }

    private static void recallDamageEvent(EntityDamageEvent damageEvent) {
        HandlerListWrapper entityDamageHandlerList = (HandlerListWrapper) EntityDamageEvent.getHandlerList();
        try {
            entityDamageHandlerList.setMode(HandlerListWrapper.Mode.ORIGINAL);
            // Call the event again.
            Bukkit.getPluginManager().callEvent(damageEvent);
        } finally {
            entityDamageHandlerList.setMode(HandlerListWrapper.Mode.NEW);
        }
    }

    private void restoreDamageResult(EntityDamageData damageResult, EntityDamageEvent damageEvent) {
        damageEvent.setCancelled(damageResult.isCancelled());

        if (ServerVersion.isEquals(ServerVersion.v1_8) && damageResult.isShouldEntityDie()) {
            // In 1.8, EntityLiving#die does not check for dead flag, causing the entity to actually die.
            // Therefore, we set the health to 0.1 and later restore it.
            plugin.getNMSEntities().setHealthDirectly((LivingEntity) damageEvent.getEntity(), 0.01f, true);
            damageEvent.setDamage(0);
        } else {
            damageResult.applyToEvent(damageEvent);
        }
    }

    private static EntityDamageEvent createDamageEvent(Entity entity, EntityDamageEvent.DamageCause damageCause, double damage, Entity damager) {
        Map<EntityDamageEvent.DamageModifier, Double> damageModifiers = Maps.newEnumMap(ImmutableMap.of(EntityDamageEvent.DamageModifier.BASE, damage));
        if (damager == null) {
            return new EntityDamageEvent(entity, damageCause, damageModifiers, damageModifiersFunctions);
        } else {
            return new EntityDamageByEntityEvent(damager, entity, damageCause, damageModifiers, damageModifiersFunctions);
        }
    }

    private class EntityDamageRestoreListener implements Listener {

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false /* Not ignoring cancel status as the event should be cancelled. */)
        public void onEntityDamageRestore(EntityDamageEvent e) {
            Optional.ofNullable(damageResults.remove(e)).ifPresent(result -> restoreDamageResult(result, e));
        }

    }

}
