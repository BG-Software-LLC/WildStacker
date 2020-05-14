package com.bgsoftware.wildstacker.hooks;

import com.gmail.nossr50.config.experience.ExperienceConfig;
import com.gmail.nossr50.datatypes.experience.XPGainReason;
import com.gmail.nossr50.datatypes.experience.XPGainSource;
import com.gmail.nossr50.datatypes.player.McMMOPlayer;
import com.gmail.nossr50.datatypes.skills.PrimarySkillType;
import com.gmail.nossr50.datatypes.skills.SkillType;
import com.gmail.nossr50.util.ItemUtils;
import com.gmail.nossr50.util.player.UserManager;
import org.bukkit.entity.Animals;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;

public final class McMMOHook {

    private static final String CUSTOM_NAME_KEY = "mcMMO: Custom Name";
    private static final String CUSTOM_NAME_VISIBLE_KEY = "mcMMO: Name Visibility";
    private static final String SPAWNED_ENTITY_KEY = "mcMMO: Spawned Entity";

    private static Plugin mcMMO;
    private static Method gainXPMethod = null;

    static {
        try{
            //noinspection JavaReflectionMemberAccess
            gainXPMethod = McMMOPlayer.class.getMethod("beginXpGain", SkillType.class, float.class, com.gmail.nossr50.datatypes.skills.XPGainReason.class);
        }catch(Throwable ignored){ }
    }

    public static void updateCachedName(LivingEntity livingEntity){
        if(mcMMO != null){
            if(livingEntity.hasMetadata(CUSTOM_NAME_KEY)) {
                livingEntity.removeMetadata(CUSTOM_NAME_KEY, mcMMO);
                livingEntity.setMetadata(CUSTOM_NAME_KEY, new FixedMetadataValue(mcMMO, livingEntity.getCustomName()));
            }
            if(livingEntity.hasMetadata(CUSTOM_NAME_VISIBLE_KEY)) {
                livingEntity.removeMetadata(CUSTOM_NAME_VISIBLE_KEY, mcMMO);
                livingEntity.setMetadata(CUSTOM_NAME_VISIBLE_KEY, new FixedMetadataValue(mcMMO, livingEntity.isCustomNameVisible()));
            }
        }
    }

    public static void handleCombat(Player attacker, LivingEntity target, double finalDamage){
        if(mcMMO == null)
            return;

        ItemStack heldItem = attacker.getItemInHand();

        McMMOPlayer mcMMOPlayer = UserManager.getPlayer(attacker);

        if(mcMMOPlayer == null)
            return;

        Object skillType = null;

        if (ItemUtils.isSword(heldItem)) {
            if (!shouldProcess(target, "SWORDS")) {
                return;
            }

            if (getPermissions(attacker, "SWORDS")) {
                skillType = getSkill("SWORDS");
            }
        }

        else if (ItemUtils.isAxe(heldItem)) {
            if (!shouldProcess(target, "AXES")) {
                return;
            }

            if (getPermissions(attacker, "AXES")) {
                skillType = getSkill("AXES");
            }
        }

        else if (ItemUtils.isUnarmed(heldItem)) {
            if (!shouldProcess(target, "UNARMED")) {
                return;
            }

            if (getPermissions(attacker, "UNARMED")) {
                skillType = getSkill("UNARMED");
            }
        }

        if(skillType == null)
            return;

        EntityType type = target.getType();

        double baseXp = 0;

        if (target instanceof Animals) {
            baseXp = getAnimalsXP(type);
        } else if (target instanceof Monster) {
            baseXp = ExperienceConfig.getInstance().getCombatXP(type);
        } else if (type == EntityType.IRON_GOLEM) {
            if (!((IronGolem)target).isPlayerCreated()) {
                baseXp = ExperienceConfig.getInstance().getCombatXP(type);
            }
        } else {
            baseXp = ExperienceConfig.getInstance().getCombatXP(type);
        }

        if (target.hasMetadata("mcMMO: Spawned Entity")) {
            baseXp *= ExperienceConfig.getInstance().getSpawnedMobXpMultiplier();
        }

        if (target.hasMetadata("mcMMO: Bred Animal")) {
            baseXp *= ExperienceConfig.getInstance().getBredMobXpMultiplier();
        }

        baseXp *= 10;

        if(baseXp > 0) {
            try {
                gainXPMethod.invoke(mcMMOPlayer, skillType, (float) ((int) (finalDamage * baseXp)), com.gmail.nossr50.datatypes.skills.XPGainReason.PVE);
            }catch(Throwable ex){
                mcMMOPlayer.beginXpGain((PrimarySkillType) skillType, (float) ((int) (finalDamage * baseXp)), XPGainReason.PVE, XPGainSource.SELF);
            }
        }
    }

    public static void updateSpawnedEntity(LivingEntity livingEntity){
        if(mcMMO != null){
            livingEntity.setMetadata(SPAWNED_ENTITY_KEY, new FixedMetadataValue(mcMMO, true));
        }
    }

    public static boolean isSpawnedEntity(LivingEntity livingEntity){
        return mcMMO != null && livingEntity.hasMetadata(SPAWNED_ENTITY_KEY);
    }

    public static void setEnabled(boolean enabled){
        mcMMO = enabled ? com.gmail.nossr50.mcMMO.p : null;
    }

    private static boolean shouldProcess(LivingEntity target, String type){
        try{
            return SkillType.valueOf(type).shouldProcess(target);
        }catch(Throwable ex){
            return PrimarySkillType.valueOf(type).shouldProcess(target);
        }
    }

    private static boolean getPermissions(Player attacker, String type){
        try{
            return SkillType.valueOf(type).getPermissions(attacker);
        }catch(Throwable ex){
            return PrimarySkillType.valueOf(type).getPermissions(attacker);
        }
    }

    private static Object getSkill(String type){
        try{
            return SkillType.valueOf(type);
        }catch(Throwable ex){
            return PrimarySkillType.valueOf(type);
        }
    }

    private static double getAnimalsXP(EntityType type){
        try{
            return ExperienceConfig.getInstance().getAnimalsXP(type);
        }catch(Throwable ex){
            return ExperienceConfig.getInstance().getAnimalsXP();
        }
    }

}
