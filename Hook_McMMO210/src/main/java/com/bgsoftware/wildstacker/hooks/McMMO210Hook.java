package com.bgsoftware.wildstacker.hooks;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.hooks.listeners.IEntityDeathListener;
import com.gmail.nossr50.config.experience.ExperienceConfig;
import com.gmail.nossr50.datatypes.experience.XPGainReason;
import com.gmail.nossr50.datatypes.experience.XPGainSource;
import com.gmail.nossr50.datatypes.meta.RuptureTaskMeta;
import com.gmail.nossr50.datatypes.player.McMMOPlayer;
import com.gmail.nossr50.datatypes.skills.PrimarySkillType;
import com.gmail.nossr50.metadata.MobMetaFlagType;
import com.gmail.nossr50.util.ItemUtils;
import com.gmail.nossr50.util.player.UserManager;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@SuppressWarnings("unused")
public final class McMMO210Hook {

    private static final String CUSTOM_NAME_KEY = "mcMMO: Custom Name";
    private static final String CUSTOM_NAME_VISIBLE_KEY = "mcMMO: Name Visibility";

    private static final String RUPTURE_TASK_KEY = "mcMMO: RuptureTask";

    private static final Set<UUID> spawnerEntities = new HashSet<>();

    private static WildStackerPlugin plugin;
    private static com.gmail.nossr50.mcMMO mcMMO;

    public static void register(WildStackerPlugin plugin) {
        McMMO210Hook.plugin = plugin;
        mcMMO = com.gmail.nossr50.mcMMO.p;
        plugin.getProviders().registerEntityDeathListener(McMMO210Hook::handleDeath);
        plugin.getProviders().registerEntityCombatListener(McMMO210Hook::handleCombat);
        plugin.getProviders().registerNameChangeListener(McMMO210Hook::updateCachedName);
    }

    private static void handleDeath(StackedEntity stackedEntity, IEntityDeathListener.Type type) {
        LivingEntity livingEntity = stackedEntity.getLivingEntity();

        switch (type) {
            case BEFORE_DEATH_EVENT:
                updateCachedName(livingEntity);
                if (isSpawnerEntity(livingEntity))
                    spawnerEntities.add(livingEntity.getUniqueId());
                break;
            case AFTER_DEATH_EVENT:
                if (spawnerEntities.remove(livingEntity.getUniqueId()))
                    updateSpawnedEntity(livingEntity);

                cancelRuptureTask(livingEntity);
                break;
        }
    }

    private static void updateCachedName(Entity entity) {
        if (!(entity instanceof LivingEntity))
            return;

        if (entity.hasMetadata(CUSTOM_NAME_KEY)) {
            entity.removeMetadata(CUSTOM_NAME_KEY, mcMMO);
            entity.setMetadata(CUSTOM_NAME_KEY, new FixedMetadataValue(mcMMO, plugin.getNMSAdapter().getCustomName(entity)));
        }
        if (entity.hasMetadata(CUSTOM_NAME_VISIBLE_KEY)) {
            entity.removeMetadata(CUSTOM_NAME_VISIBLE_KEY, mcMMO);
            entity.setMetadata(CUSTOM_NAME_VISIBLE_KEY, new FixedMetadataValue(mcMMO, plugin.getNMSAdapter().isCustomNameVisible(entity)));
        }
    }

    private static void handleCombat(LivingEntity target, Player attacker, Entity entityAttacker, double finalDamage) {
        ItemStack heldItem = attacker.getItemInHand();

        McMMOPlayer mcMMOPlayer = UserManager.getPlayer(attacker);

        if (mcMMOPlayer == null)
            return;

        PrimarySkillType primarySkillType;

        if (entityAttacker instanceof Wolf) {
            primarySkillType = PrimarySkillType.TAMING;
        } else if (entityAttacker instanceof Arrow) {
            primarySkillType = PrimarySkillType.ARCHERY;
        } else if (ItemUtils.isSword(heldItem)) {
            primarySkillType = PrimarySkillType.SWORDS;
        } else if (ItemUtils.isAxe(heldItem)) {
            primarySkillType = PrimarySkillType.AXES;
        } else if (ItemUtils.isUnarmed(heldItem)) {
            primarySkillType = PrimarySkillType.UNARMED;
        } else {
            return;
        }

        //noinspection deprecation
        if (!primarySkillType.shouldProcess(target) || !primarySkillType.getPermissions(attacker))
            return;

        EntityType type = target.getType();

        double baseXp = 0;

        if (target instanceof Animals) {
            baseXp = getAnimalsXP(type);
        } else if (target instanceof Monster) {
            baseXp = ExperienceConfig.getInstance().getCombatXP(type);
        } else if (type == EntityType.IRON_GOLEM) {
            if (!((IronGolem) target).isPlayerCreated()) {
                baseXp = ExperienceConfig.getInstance().getCombatXP(type);
            }
        } else {
            baseXp = ExperienceConfig.getInstance().getCombatXP(type);
        }

        if (isSpawnEggEntity(target)) {
            baseXp *= ExperienceConfig.getInstance().getEggXpMultiplier();
        } else if (isSpawnerEntity(target)) {
            baseXp *= ExperienceConfig.getInstance().getSpawnedMobXpMultiplier();
        } else if (isNetherPortal(target)) {
            baseXp *= ExperienceConfig.getInstance().getNetherPortalXpMultiplier();
        } else if (isAnimalBred(target)) {
            baseXp *= ExperienceConfig.getInstance().getBredMobXpMultiplier();
        } else if (isTamedAnimal(target)) {
            baseXp *= ExperienceConfig.getInstance().getTamedMobXpMultiplier();
        }

        baseXp *= 10;

        if (baseXp > 0)
            mcMMOPlayer.beginXpGain(primarySkillType, (float) ((int) (finalDamage * baseXp)),
                    XPGainReason.PVE, XPGainSource.SELF);
    }

    private static void updateSpawnedEntity(LivingEntity livingEntity) {
        com.gmail.nossr50.mcMMO.getMetadataService().getMobMetadataService()
                .flagMetadata(MobMetaFlagType.MOB_SPAWNER_MOB, livingEntity);
    }

    private static void cancelRuptureTask(LivingEntity livingEntity) {
        for (MetadataValue metadataValue : livingEntity.getMetadata(RUPTURE_TASK_KEY)) {
            if (metadataValue instanceof RuptureTaskMeta) {
                ((RuptureTaskMeta) metadataValue).getRuptureTimerTask().cancel();
            }
        }
    }

    private static boolean isSpawnerEntity(LivingEntity livingEntity) {
        return hasTag(livingEntity, MobMetaFlagType.MOB_SPAWNER_MOB);
    }

    private static boolean isSpawnEggEntity(LivingEntity livingEntity) {
        return hasTag(livingEntity, MobMetaFlagType.EGG_MOB);
    }

    private static boolean isNetherPortal(LivingEntity livingEntity) {
        return hasTag(livingEntity, MobMetaFlagType.NETHER_PORTAL_MOB);
    }

    private static boolean isAnimalBred(LivingEntity livingEntity) {
        return hasTag(livingEntity, MobMetaFlagType.PLAYER_BRED_MOB);
    }

    private static boolean isTamedAnimal(LivingEntity livingEntity) {
        return hasTag(livingEntity, MobMetaFlagType.PLAYER_TAMED_MOB);
    }

    private static double getAnimalsXP(EntityType type) {
        try {
            return ExperienceConfig.getInstance().getAnimalsXP(type);
        } catch (Throwable ex) {
            return ExperienceConfig.getInstance().getAnimalsXP();
        }
    }

    private static boolean hasTag(LivingEntity livingEntity, MobMetaFlagType mobMetaFlagType) {
        return com.gmail.nossr50.mcMMO.getMetadataService().getMobMetadataService()
                .hasMobFlag(mobMetaFlagType, livingEntity);
    }

}
