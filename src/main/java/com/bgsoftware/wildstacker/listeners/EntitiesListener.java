package com.bgsoftware.wildstacker.listeners;

import com.bgsoftware.wildstacker.Locale;
import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.enums.SpawnCause;
import com.bgsoftware.wildstacker.api.enums.StackSplit;
import com.bgsoftware.wildstacker.api.enums.UnstackResult;
import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.hooks.ProtocolLibHook;
import com.bgsoftware.wildstacker.listeners.events.EntityPickupItemEvent;
import com.bgsoftware.wildstacker.objects.WStackedEntity;
import com.bgsoftware.wildstacker.utils.Executor;
import com.bgsoftware.wildstacker.utils.ServerVersion;
import com.bgsoftware.wildstacker.utils.entity.EntityUtils;
import com.bgsoftware.wildstacker.utils.items.ItemUtils;
import com.bgsoftware.wildstacker.utils.legacy.EntityTypes;
import com.bgsoftware.wildstacker.utils.legacy.Materials;
import com.bgsoftware.wildstacker.utils.reflection.ReflectionUtils;
import com.google.common.base.Functions;
import com.google.common.collect.ImmutableMap;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Animals;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.MushroomCow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Sheep;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.SheepDyeWoolEvent;
import org.bukkit.event.entity.SheepRegrowWoolEvent;
import org.bukkit.event.entity.SlimeSplitEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@SuppressWarnings({"unused", "WeakerAccess"})
public final class EntitiesListener implements Listener {

    private WildStackerPlugin plugin;

    public static Set<UUID> noStackEntities = new HashSet<>();

    public EntitiesListener(WildStackerPlugin plugin){
        this.plugin = plugin;
        if(ServerVersion.isAtLeast(ServerVersion.v1_13))
            plugin.getServer().getPluginManager().registerEvents(new TransformListener(), plugin);
        if(ReflectionUtils.isPluginEnabled("com.ome_r.wildstacker.enchantspatch.events.EntityKillEvent"))
            plugin.getServer().getPluginManager().registerEvents(new EntityKillListener(), plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDeathMonitor(EntityDeathEvent e){
        if(e.getEntity().hasMetadata("corpse")){
            e.getDrops().clear();
            e.setDroppedExp(0);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityPickup(EntityPickupItemEvent e){
        if(e.getEntity().hasMetadata("corpse")){
            e.setCancelled(true);
        }
    }

    private Set<UUID> deadEntities = new HashSet<>();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCustomEntityDeath(EntityDeathEvent e){
        //Checks if the entity is not a corpse.
        if(e.getEntity().hasMetadata("corpse"))
            return;

        if(deadEntities.contains(e.getEntity().getUniqueId())){
            deadEntities.remove(e.getEntity().getUniqueId());
            return;
        }

        //Calling the onEntityLastDamage function with default parameters.

        //noinspection unchecked
        EntityDamageEvent entityDamageEvent = new EntityDamageEvent(e.getEntity(), EntityDamageEvent.DamageCause.CUSTOM,
                new EnumMap(ImmutableMap.of(EntityDamageEvent.DamageModifier.BASE, e.getEntity().getHealth())),
                new EnumMap(ImmutableMap.of(EntityDamageEvent.DamageModifier.BASE, Functions.constant(-0.0D))));

        onEntityLastDamage(entityDamageEvent);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityLastDamage(EntityDamageEvent e){
        //Checks that it's the last hit of the entity
        if(!EntityUtils.isStackable(e.getEntity()) || ((LivingEntity) e.getEntity()).getHealth() - e.getFinalDamage() > 0)
            return;

        LivingEntity livingEntity = (LivingEntity) e.getEntity();
        StackedEntity stackedEntity = WStackedEntity.of(livingEntity);

        if(plugin.getSettings().entitiesStackingEnabled || stackedEntity.getStackAmount() > 1) {
            EntityDamageEvent.DamageCause lastDamageCause = e.getCause();
            int stackAmount = plugin.getSettings().entitiesInstantKills.contains(lastDamageCause.name()) ||
                    plugin.getSettings().entitiesInstantKills.contains(e.getEntityType().name()) ||
                    ((LivingEntity) e.getEntity()).getHealth() <= 0 ? stackedEntity.getStackAmount() : 1;

            if(plugin.getSettings().nextStackKnockback)
                e.setDamage(0);
            else
                e.setCancelled(true);

            livingEntity.setHealth(livingEntity.getMaxHealth());

            livingEntity.setLastDamageCause(e);

            if(stackedEntity.runUnstack(stackAmount) == UnstackResult.SUCCESS) {
                if (e instanceof EntityDamageByEntityEvent) {
                    if(((EntityDamageByEntityEvent) e).getDamager() instanceof Player) {
                        EntityUtils.setKiller(livingEntity, (Player) ((EntityDamageByEntityEvent) e).getDamager());
                    }
                    else if(((EntityDamageByEntityEvent) e).getDamager() instanceof Projectile){
                        Projectile projectile = (Projectile) ((EntityDamageByEntityEvent) e).getDamager();
                        if(projectile.getShooter() instanceof Player)
                            EntityUtils.setKiller(livingEntity, (Player) projectile.getShooter());
                    }
                } else {
                    EntityUtils.setKiller(livingEntity, null);
                }

                int lootBonusLevel = getFortuneLevel(livingEntity);

                Executor.async(() -> {
                    List<ItemStack> drops = stackedEntity.getDrops(lootBonusLevel, stackAmount);
                    Executor.sync(() -> {
                        deadEntities.add(livingEntity.getUniqueId());

                        plugin.getNMSAdapter().setEntityDead(livingEntity, true);

                        EntityDeathEvent entityDeathEvent = new EntityDeathEvent(livingEntity, new ArrayList<>(drops), stackedEntity.getExp(stackAmount, 0));
                        Bukkit.getPluginManager().callEvent(entityDeathEvent);

                        plugin.getNMSAdapter().setEntityDead(livingEntity, false);

                        List<ItemStack> eventDrops = new ArrayList<>(entityDeathEvent.getDrops());
                        entityDeathEvent.getDrops().clear();
                        entityDeathEvent.getDrops().addAll(eventDrops.stream()
                                .filter(itemStack -> itemStack != null && itemStack.getType() != Material.AIR).collect(Collectors.toList()));

                        //Multiply items that weren't added in the first place
                        subtract(drops, entityDeathEvent.getDrops())
                                .forEach(itemStack -> itemStack.setAmount(itemStack.getAmount() * stackAmount));

                        entityDeathEvent.getDrops().forEach(itemStack -> ItemUtils.dropItem(itemStack, livingEntity.getLocation()));

                        if (entityDeathEvent.getDroppedExp() > 0) {
                            ExperienceOrb experienceOrb = livingEntity.getWorld().spawn(livingEntity.getLocation(), ExperienceOrb.class);
                            experienceOrb.setExperience(entityDeathEvent.getDroppedExp());
                        }
                    });
                });

                if (plugin.getSettings().keepFireEnabled && livingEntity.getFireTicks() > -1)
                    livingEntity.setFireTicks(160);

                if (livingEntity.getKiller() != null) {
                    Player killer = livingEntity.getKiller();

                    try {
                        killer.incrementStatistic(Statistic.MOB_KILLS, stackAmount);
                        killer.incrementStatistic(Statistic.KILL_ENTITY, stackedEntity.getType(), stackAmount);
                    } catch (IllegalArgumentException ignored) { }

                    //Achievements
                    EntityType victimType = livingEntity.getType();

                    //Monster Hunter
                    grandAchievement(killer, victimType, "KILL_ENEMY");
                    grandAchievement(killer, victimType, "adventure/kill_a_mob");
                    //Monsters Hunted
                    grandAchievement(killer, victimType, "adventure/kill_all_mobs");
                    //Sniper Duel
                    if(killer.getLocation().distanceSquared(livingEntity.getLocation()) >= 50*50){
                        grandAchievement(killer, "", "SNIPE_SKELETON");
                        grandAchievement(killer, "killed_skeleton", "adventure/sniper_duel");
                    }

                }
            }
        }
    }

    private void grandAchievement(Player killer, EntityType entityType, String name){
        try{
            plugin.getNMSAdapter().grandAchievement(killer, entityType, name);
        }catch(Throwable ignored){}
    }

    private void grandAchievement(Player killer, String criteria, String name){
        try{
            plugin.getNMSAdapter().grandAchievement(killer, criteria, name);
        }catch(Throwable ignored){}
    }

    private List<ItemStack> subtract(List<ItemStack> list1, List<ItemStack> list2){
        List<ItemStack> toReturn = new ArrayList<>(list2);
        toReturn.removeAll(list1);
        return toReturn;
    }

    private int mooshroomFlag = -1;

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntitySpawn(CreatureSpawnEvent e){
        if(!plugin.getSettings().entitiesStackingEnabled)
            return;

        if(e.getEntityType() == EntityType.ARMOR_STAND || e.getEntityType() == EntityType.PLAYER || e.getEntity().hasMetadata("corpse"))
            return;

        e.getEntity().setMetadata("spawn-cause", new FixedMetadataValue(plugin, SpawnCause.valueOf(e.getSpawnReason())));
        StackedEntity stackedEntity = WStackedEntity.of(e.getEntity());
        stackedEntity.setSpawnCause(SpawnCause.valueOf(e.getSpawnReason()));

        if(mooshroomFlag != -1){
            stackedEntity.setStackAmount(mooshroomFlag, true);
            mooshroomFlag = -1;
        }

        if(stackedEntity.isBlacklisted() || !stackedEntity.isWhitelisted() || stackedEntity.isWorldDisabled())
            return;

        if(noStackEntities.contains(e.getEntity().getUniqueId())) {
            noStackEntities.remove(e.getEntity().getUniqueId());
            return;
        }

        //EpicSpawners has it's own event
        if(stackedEntity.getSpawnCause() == SpawnCause.EPIC_SPAWNERS)
            return;

        if (!Bukkit.getPluginManager().isPluginEnabled("MergedSpawner") &&
                plugin.getSettings().linkedEntitiesEnabled && e.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER)
            return;

        Consumer<Optional<LivingEntity>> nerfEntityConsumer = entityOptional -> {
            if(!entityOptional.isPresent())
                stackedEntity.updateNerfed();
        };

        //Need to add a delay so eggs will get removed from inventory
        if(e.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER_EGG || e.getEntityType() == EntityType.WITHER ||
                e.getEntityType() == EntityType.IRON_GOLEM || e.getEntityType() == EntityType.SNOWMAN ||
                Bukkit.getPluginManager().isPluginEnabled("MythicMobs") || Bukkit.getPluginManager().isPluginEnabled("EpicBosses"))
            Executor.sync(() -> stackedEntity.runStackAsync(nerfEntityConsumer), 1L);
        else
            stackedEntity.runStackAsync(nerfEntityConsumer);

        //Chunk Limit
        Executor.sync(() -> {
            if(isChunkLimit(e.getLocation().getChunk()))
                stackedEntity.remove();
        }, 2L);
    }

    private int nextEntityStackAmount = -1;
    private EntityTypes nextEntityType = null;

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSpawnerEggUse(PlayerInteractEvent e){
        if(!plugin.getSettings().entitiesStackingEnabled || e.getItem() == null || e.getAction() != Action.RIGHT_CLICK_BLOCK ||
                plugin.getSettings().blacklistedEntitiesSpawnReasons.contains("SPAWNER_EGG") || !Materials.isValidAndSpawnEgg(e.getItem()))
            return;

        nextEntityStackAmount = ItemUtils.getSpawnerItemAmount(e.getItem());
        nextEntityType = ItemUtils.getEntityType(e.getItem());

        EntityType nmsEntityType = ItemUtils.getNMSEntityType(e.getItem());
        if(nmsEntityType != null){
            e.setCancelled(true);
            nextEntityType = EntityTypes.fromName(nmsEntityType.name());
            Location toSpawn = e.getClickedBlock().getRelative(e.getBlockFace()).getLocation().add(0.5, 0, 0.5);
            if(toSpawn.getBlock().getType() != Material.AIR)
                toSpawn = toSpawn.add(0, 1, 0);
            plugin.getSystemManager().spawnEntityWithoutStacking(toSpawn, nmsEntityType.getEntityClass(), SpawnCause.SPAWNER_EGG);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntitySpawnFromEgg(CreatureSpawnEvent e){
        if(e.getSpawnReason() != CreatureSpawnEvent.SpawnReason.SPAWNER_EGG || nextEntityStackAmount <= 0 ||
                EntityTypes.fromEntity(e.getEntity()) != nextEntityType)
            return;

        StackedEntity stackedEntity = WStackedEntity.of(e.getEntity());
        stackedEntity.setStackAmount(nextEntityStackAmount, true);

        nextEntityStackAmount = -1;
        nextEntityType = null;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onSlimeSplit(SlimeSplitEvent e){
        //Fixes a async catch exception with auto-clear task
        if(!Bukkit.isPrimaryThread())
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSheepDye(SheepDyeWoolEvent e){
        if(!plugin.getSettings().entitiesStackingEnabled || !StackSplit.SHEEP_DYE.isEnabled())
            return;

        StackedEntity stackedEntity = WStackedEntity.of(e.getEntity());
        int amount = stackedEntity.getStackAmount();

        DyeColor originalColor = e.getEntity().getColor();

        Executor.sync(() -> {
            if(amount > 1) {
                e.getEntity().setColor(originalColor);
                stackedEntity.setStackAmount(amount - 1, true);
                StackedEntity duplicate = stackedEntity.spawnDuplicate(1);
                ((Sheep) duplicate.getLivingEntity()).setColor(e.getColor());
                duplicate.runStackAsync(null);
            }else{
                stackedEntity.runStackAsync(null);
            }
        }, 1L);
    }


    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerShear(PlayerShearEntityEvent e){
        if(!plugin.getSettings().entitiesStackingEnabled || !EntityUtils.isStackable(e.getEntity()))
            return;

        StackedEntity stackedEntity = WStackedEntity.of(e.getEntity());

        if(e.getEntity() instanceof MushroomCow){
            if(StackSplit.MUSHROOM_SHEAR.isEnabled()) {
                stackedEntity.spawnDuplicate(stackedEntity.getStackAmount() - 1);
                stackedEntity.remove();
            }
            else{
                int mushroomAmount = 5 * (stackedEntity.getStackAmount() - 1);
                ItemStack mushroomItem = new ItemStack(Material.RED_MUSHROOM, mushroomAmount);
                ItemUtils.dropItem(mushroomItem, e.getEntity().getLocation());
                mooshroomFlag = stackedEntity.getStackAmount();
            }
        }

        else if(e.getEntity() instanceof Sheep){
            int stackAmount = stackedEntity.getStackAmount();
            if(StackSplit.SHEEP_SHEAR.isEnabled()) {
                Executor.sync(() -> {
                    if (stackAmount > 1) {
                        ((Sheep) e.getEntity()).setSheared(false);
                        stackedEntity.setStackAmount(stackAmount - 1, true);
                        StackedEntity duplicate = stackedEntity.spawnDuplicate(1);
                        ((Sheep) duplicate.getLivingEntity()).setSheared(true);
                        duplicate.runStackAsync(null);
                    } else {
                        stackedEntity.runStackAsync(null);
                    }
                }, 0L);
            }
            else{
                int woolAmount = ThreadLocalRandom.current().nextInt(2 * (stackAmount - 1)) + stackAmount - 1;
                ItemStack woolItem = Materials.getWool((((Sheep) e.getEntity()).getColor()));
                woolItem.setAmount(woolAmount);
                ItemUtils.dropItem(woolItem, e.getEntity().getLocation());
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

        Executor.sync(() -> {
            if(amount > 1){
                e.getEntity().setSheared(true);
                stackedEntity.setStackAmount(amount - 1, true);
                StackedEntity duplicated = stackedEntity.spawnDuplicate(1);
                ((Sheep) duplicated.getLivingEntity()).setSheared(false);
                duplicated.runStackAsync(null);
            }else{
                stackedEntity.runStackAsync(null);
            }
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityNameTag(PlayerInteractAtEntityEvent e){
        if(!plugin.getSettings().entitiesStackingEnabled || !EntityUtils.isStackable(e.getRightClicked()) || !StackSplit.NAME_TAG.isEnabled())
            return;

        ItemStack inHand = e.getPlayer().getInventory().getItemInHand();
        StackedEntity stackedEntity = WStackedEntity.of(e.getRightClicked());

        if(inHand == null || inHand.getType() != Material.NAME_TAG || !inHand.hasItemMeta() || !inHand.getItemMeta().hasDisplayName()
                || e.getRightClicked() instanceof EnderDragon || e.getRightClicked() instanceof Player)
                return;

        int amount = stackedEntity.getStackAmount();

        Executor.sync(() -> {
            if(amount > 1){
                stackedEntity.setCustomName("");
                stackedEntity.setStackAmount(amount - 1, true);
                StackedEntity duplicated = stackedEntity.spawnDuplicate(1);
                duplicated.setCustomName(inHand.getItemMeta().getDisplayName());
                duplicated.runStackAsync(null);
            }else{
                stackedEntity.runStackAsync(null);
            }
        }, 2L);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityFeed(PlayerInteractAtEntityEvent e){
        if(!StackSplit.ENTITY_BREED.isEnabled())
            return;

        if(!(e.getRightClicked() instanceof Animals))
            return;

        Executor.sync(() -> {
            if(!plugin.getNMSAdapter().isInLove(e.getRightClicked()))
                return;

            StackedEntity stackedEntity = WStackedEntity.of(e.getRightClicked());
            int amount = stackedEntity.getStackAmount();

            if(amount > 1){
                plugin.getNMSAdapter().setInLove(stackedEntity.getLivingEntity(), e.getPlayer(), false);
                stackedEntity.setStackAmount(amount - 1, true);
                StackedEntity duplicated = stackedEntity.spawnDuplicate(1);
                plugin.getNMSAdapter().setInLove(duplicated.getLivingEntity(), e.getPlayer(), true);
            }
        }, 1L);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityBreed(CreatureSpawnEvent e){
        if(e.getSpawnReason() == CreatureSpawnEvent.SpawnReason.BREEDING && plugin.getSettings().stackAfterBreed){
            plugin.getNMSAdapter().getNearbyEntities(e.getEntity(), 5, entity -> EntityUtils.isStackable(e.getEntity()) && entity.isValid())
                    .forEach(entity -> WStackedEntity.of(entity).runStackAsync(null));
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityInspect(PlayerInteractAtEntityEvent e){
        if(isOffHand(e) || e.getPlayer().getItemInHand() == null || !e.getPlayer().getItemInHand().isSimilar(plugin.getSettings().inspectTool) ||
                !EntityUtils.isStackable(e.getRightClicked()))
            return;

        e.setCancelled(true);

        StackedEntity stackedEntity = WStackedEntity.of(e.getRightClicked());

        Locale.ENTITY_INFO_HEADER.send(e.getPlayer());
        Locale.ENTITY_INFO_UUID.send(e.getPlayer(), stackedEntity.getUniqueId());
        Locale.ENTITY_INFO_TYPE.send(e.getPlayer(), EntityUtils.getFormattedType(stackedEntity.getType().name()));
        Locale.ENTITY_INFO_AMOUNT.send(e.getPlayer(), stackedEntity.getStackAmount());
        Locale.ENTITY_INFO_SPAWN_REASON.send(e.getPlayer(), stackedEntity.getSpawnCause().name());
        Locale.ENTITY_INFO_NERFED.send(e.getPlayer(), stackedEntity.isNerfed() ? "True" : "False");
        Locale.BARREL_INFO_FOOTER.send(e.getPlayer());
    }

    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent e){
        if(!plugin.getSettings().entitiesNamesToggleEnabled)
            return;

        String commandSyntax = "/" + plugin.getSettings().entitiesNamesToggleCommand;

        if(!e.getMessage().equalsIgnoreCase(commandSyntax) && !e.getMessage().startsWith(commandSyntax + " "))
            return;

        e.setCancelled(true);

        if(!Bukkit.getPluginManager().isPluginEnabled("ProtocolLib")){
            e.getPlayer().sendMessage(ChatColor.RED + "The command is enabled but ProtocolLib is not installed. Please contact the administrators of the server to solve the issue.");
            return;
        }

        if(ProtocolLibHook.entitiesDisabledNames.contains(e.getPlayer().getUniqueId())){
            ProtocolLibHook.entitiesDisabledNames.remove(e.getPlayer().getUniqueId());
            Locale.ENTITY_NAMES_TOGGLE_ON.send(e.getPlayer());
        }
        else{
            ProtocolLibHook.entitiesDisabledNames.add(e.getPlayer().getUniqueId());
            Locale.ENTITY_NAMES_TOGGLE_OFF.send(e.getPlayer());
        }

        //Refresh item names
        plugin.getNMSAdapter().getNearbyEntities(e.getPlayer(), 50, 256, 50,
                entity -> EntityUtils.isStackable(entity) && entity.isCustomNameVisible())
                .forEach(entity -> ProtocolLibHook.updateName(e.getPlayer(), entity));
    }

    private boolean isOffHand(PlayerInteractAtEntityEvent event){
        try{
            return event.getClass().getMethod("getHand").invoke(event).toString().equals("OFF_HAND");
        }catch(Throwable ex){
            return false;
        }
    }

    private EntityDamageEvent.DamageCause getLastDamage(LivingEntity livingEntity){
        return livingEntity.getLastDamageCause() == null ? EntityDamageEvent.DamageCause.CUSTOM : livingEntity.getLastDamageCause().getCause();
    }

    private boolean isChunkLimit(Chunk chunk){
        int chunkLimit = plugin.getSettings().entitiesChunkLimit;

        if(chunkLimit <= 0)
            return false;

        int entitiesInsideChunk = (int) Arrays.stream(chunk.getEntities()).filter(EntityUtils::isStackable).count();
        return entitiesInsideChunk > chunkLimit;
    }

    private int getFortuneLevel(LivingEntity livingEntity){
        int fortuneLevel = 0;

        if(getLastDamage(livingEntity) == EntityDamageEvent.DamageCause.ENTITY_ATTACK &&
                livingEntity.getKiller() != null && livingEntity.getKiller().getItemInHand() != null){
            fortuneLevel = livingEntity.getKiller().getItemInHand().getEnchantmentLevel(Enchantment.LOOT_BONUS_MOBS);
        }

        return fortuneLevel;
    }

    class EntityKillListener implements Listener {

        @EventHandler
        public void onEntityKill(com.ome_r.wildstacker.enchantspatch.events.EntityKillEvent e){
            if(e.getEntity().hasMetadata("corpse"))
                return;

            e.setCancelled(true);

            //noinspection unchecked
            EntityDamageEvent entityDamageEvent = new EntityDamageEvent(e.getEntity(), EntityDamageEvent.DamageCause.CUSTOM,
                    new EnumMap(ImmutableMap.of(EntityDamageEvent.DamageModifier.BASE, e.getEntity().getHealth())),
                    new EnumMap(ImmutableMap.of(EntityDamageEvent.DamageModifier.BASE, Functions.constant(-0.0D))));

            onEntityLastDamage(entityDamageEvent);
        }

    }

    static class TransformListener implements Listener {

        @EventHandler
        public void onEntityTransform(org.bukkit.event.entity.EntityTransformEvent e){
            StackedEntity stackedEntity = WStackedEntity.of(e.getEntity());

            if(stackedEntity.getStackAmount() > 1){
                StackedEntity transformed = WStackedEntity.of(e.getTransformedEntity());
                transformed.setStackAmount(stackedEntity.getStackAmount(), true);
                stackedEntity.remove();
            }
        }

    }

}
