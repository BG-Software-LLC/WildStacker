package com.bgsoftware.wildstacker.hooks;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.gmail.nossr50.config.experience.ExperienceConfig;
import com.gmail.nossr50.datatypes.experience.XPGainReason;
import com.gmail.nossr50.datatypes.experience.XPGainSource;
import com.gmail.nossr50.datatypes.meta.RuptureTaskMeta;
import com.gmail.nossr50.datatypes.player.McMMOPlayer;
import com.gmail.nossr50.datatypes.skills.PrimarySkillType;
import com.gmail.nossr50.datatypes.skills.SkillType;
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
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;

public final class McMMOHook {

    private static final WildStackerPlugin plugin = WildStackerPlugin.getPlugin();

    private static final String CUSTOM_NAME_KEY = "mcMMO: Custom Name";
    private static final String CUSTOM_NAME_VISIBLE_KEY = "mcMMO: Name Visibility";

    // Multiplier tags
    private static final String SPAWNER_ENTITY_KEY = "mcMMO: Spawned Entity";
    private static final String BRED_ANIMAL_KEY = "mcMMO: Bred Animal";

    private static final String RUPTURE_TASK_KEY = "mcMMO: RuptureTask";

    private static Plugin mcMMO;
    private static Method gainXPMethod = null;
    private static Object persistentDataLayerObject = null;

    static {
        try {
            //noinspection JavaReflectionMemberAccess
            gainXPMethod = McMMOPlayer.class.getMethod("beginXpGain", SkillType.class, float.class, com.gmail.nossr50.datatypes.skills.XPGainReason.class);
        } catch (Throwable ignored) {
        }
    }

    public static void updateCachedName(LivingEntity livingEntity) {
        if (mcMMO != null) {
            if (livingEntity.hasMetadata(CUSTOM_NAME_KEY)) {
                livingEntity.removeMetadata(CUSTOM_NAME_KEY, mcMMO);
                livingEntity.setMetadata(CUSTOM_NAME_KEY, new FixedMetadataValue(mcMMO, plugin.getNMSAdapter().getCustomName(livingEntity)));
            }
            if (livingEntity.hasMetadata(CUSTOM_NAME_VISIBLE_KEY)) {
                livingEntity.removeMetadata(CUSTOM_NAME_VISIBLE_KEY, mcMMO);
                livingEntity.setMetadata(CUSTOM_NAME_VISIBLE_KEY, new FixedMetadataValue(mcMMO, plugin.getNMSAdapter().isCustomNameVisible(livingEntity)));
            }
        }
    }

    public static void handleCombat(Player attacker, Entity entityAttacker, LivingEntity target, double finalDamage) {
        if (mcMMO == null)
            return;

        ItemStack heldItem = attacker.getItemInHand();

        McMMOPlayer mcMMOPlayer = UserManager.getPlayer(attacker);

        if (mcMMOPlayer == null)
            return;

        Object skillType = null;

        if (entityAttacker instanceof Wolf) {
            if (!shouldProcess(target, "TAMING")) {
                return;
            }

            if (getPermissions(attacker, "TAMING")) {
                skillType = getSkill("TAMING");
            }
        } else if (entityAttacker instanceof Arrow) {
            if (!shouldProcess(target, "ARCHERY")) {
                return;
            }

            if (getPermissions(attacker, "ARCHERY")) {
                skillType = getSkill("ARCHERY");
            }
        } else if (ItemUtils.isSword(heldItem)) {
            if (!shouldProcess(target, "SWORDS")) {
                return;
            }

            if (getPermissions(attacker, "SWORDS")) {
                skillType = getSkill("SWORDS");
            }
        } else if (ItemUtils.isAxe(heldItem)) {
            if (!shouldProcess(target, "AXES")) {
                return;
            }

            if (getPermissions(attacker, "AXES")) {
                skillType = getSkill("AXES");
            }
        } else if (ItemUtils.isUnarmed(heldItem)) {
            if (!shouldProcess(target, "UNARMED")) {
                return;
            }

            if (getPermissions(attacker, "UNARMED")) {
                skillType = getSkill("UNARMED");
            }
        }

        if (skillType == null)
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

        if (baseXp > 0) {
            try {
                gainXPMethod.invoke(mcMMOPlayer, skillType, (float) ((int) (finalDamage * baseXp)), com.gmail.nossr50.datatypes.skills.XPGainReason.PVE);
            } catch (Throwable ex) {
                mcMMOPlayer.beginXpGain((PrimarySkillType) skillType, (float) ((int) (finalDamage * baseXp)), XPGainReason.PVE, XPGainSource.SELF);
            }
        }
    }

    public static void updateSpawnedEntity(LivingEntity livingEntity) {
        if (mcMMO != null) {
            if (persistentDataLayerObject != null) {
                com.gmail.nossr50.util.compat.layers.persistentdata.AbstractPersistentDataLayer persistentDataLayer =
                        (com.gmail.nossr50.util.compat.layers.persistentdata.AbstractPersistentDataLayer) persistentDataLayerObject;
                persistentDataLayer.flagMetadata(com.gmail.nossr50.util.compat.layers.persistentdata.MobMetaFlagType.MOB_SPAWNER_MOB, livingEntity);
            } else {
                livingEntity.setMetadata(SPAWNER_ENTITY_KEY, new FixedMetadataValue(mcMMO, true));
            }
        }
    }

    public static void cancelRuptureTask(LivingEntity livingEntity) {
        if (mcMMO != null) {
            for(MetadataValue metadataValue : livingEntity.getMetadata(RUPTURE_TASK_KEY)){
                if(metadataValue instanceof RuptureTaskMeta){
                    ((RuptureTaskMeta) metadataValue).getRuptureTimerTask().cancel();
                }
            }
        }
    }

    public static boolean isSpawnerEntity(LivingEntity livingEntity) {
        return hasTag(livingEntity, "MOB_SPAWNER_MOB", SPAWNER_ENTITY_KEY);
    }

    public static boolean isSpawnEggEntity(LivingEntity livingEntity) {
        return hasTag(livingEntity, "EGG_MOB", "UNKNOWN");
    }

    public static boolean isNetherPortal(LivingEntity livingEntity) {
        return hasTag(livingEntity, "NETHER_PORTAL_MOB", "UNKNOWN");
    }

    public static boolean isAnimalBred(LivingEntity livingEntity) {
        return hasTag(livingEntity, "PLAYER_BRED_MOB", BRED_ANIMAL_KEY);
    }

    public static boolean isTamedAnimal(LivingEntity livingEntity) {
        return hasTag(livingEntity, "PLAYER_TAMED_MOB", "UNKNOWN");
    }

    public static void setEnabled(boolean enabled) {
        if (enabled) {
            mcMMO = com.gmail.nossr50.mcMMO.p;
            try {
                Method getCompatibilityManagerMethod = com.gmail.nossr50.mcMMO.class.getMethod("getCompatibilityManager");
                com.gmail.nossr50.util.compat.CompatibilityManager compatibilityManager =
                        (com.gmail.nossr50.util.compat.CompatibilityManager) getCompatibilityManagerMethod.invoke(mcMMO);
                persistentDataLayerObject = compatibilityManager.getPersistentDataLayer();
            } catch (Throwable ignored) {
            }
        } else {
            mcMMO = null;
        }
    }

    private static boolean shouldProcess(LivingEntity target, String type) {
        try {
            return SkillType.valueOf(type).shouldProcess(target);
        } catch (Throwable ex) {
            return PrimarySkillType.valueOf(type).shouldProcess(target);
        }
    }

    private static boolean getPermissions(Player attacker, String type) {
        try {
            return SkillType.valueOf(type).getPermissions(attacker);
        } catch (Throwable ex) {
            return PrimarySkillType.valueOf(type).getPermissions(attacker);
        }
    }

    private static Object getSkill(String type) {
        try {
            return SkillType.valueOf(type);
        } catch (Throwable ex) {
            return PrimarySkillType.valueOf(type);
        }
    }

    private static double getAnimalsXP(EntityType type) {
        try {
            return ExperienceConfig.getInstance().getAnimalsXP(type);
        } catch (Throwable ex) {
            return ExperienceConfig.getInstance().getAnimalsXP();
        }
    }

    private static boolean hasTag(LivingEntity livingEntity, String mobMetaFlagType, String metadataKey) {
        if (mcMMO == null)
            return false;

        if (persistentDataLayerObject != null) {
            com.gmail.nossr50.util.compat.layers.persistentdata.AbstractPersistentDataLayer persistentDataLayer =
                    (com.gmail.nossr50.util.compat.layers.persistentdata.AbstractPersistentDataLayer) persistentDataLayerObject;
            return persistentDataLayer.hasMobFlag(com.gmail.nossr50.util.compat.layers.persistentdata.MobMetaFlagType.valueOf(mobMetaFlagType), livingEntity);
        } else {
            return livingEntity.hasMetadata(metadataKey);
        }
    }

}
