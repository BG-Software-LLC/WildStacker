package com.bgsoftware.wildstacker.hooks;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.hooks.listeners.IEntityDeathListener;
import com.gmail.nossr50.config.experience.ExperienceConfig;
import com.gmail.nossr50.datatypes.player.McMMOPlayer;
import com.gmail.nossr50.datatypes.skills.SkillType;
import com.gmail.nossr50.datatypes.skills.XPGainReason;
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
import org.bukkit.plugin.Plugin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@SuppressWarnings("unused")
public final class McMMOHook {

    private static final String CUSTOM_NAME_KEY = "mcMMO: Custom Name";
    private static final String CUSTOM_NAME_VISIBLE_KEY = "mcMMO: Name Visibility";

    // Multiplier tags
    private static final String SPAWNER_ENTITY_KEY = "mcMMO: Spawned Entity";
    private static final String BRED_ANIMAL_KEY = "mcMMO: Bred Animal";

    private static final Set<UUID> spawnerEntities = new HashSet<>();

    private static WildStackerPlugin plugin;
    private static Plugin mcMMO;

    public static void register(WildStackerPlugin plugin) {
        McMMOHook.plugin = plugin;
        mcMMO = com.gmail.nossr50.mcMMO.p;
        plugin.getProviders().registerEntityDeathListener(McMMOHook::handleDeath);
        plugin.getProviders().registerEntityCombatListener(McMMOHook::handleCombat);
        plugin.getProviders().registerNameChangeListener(McMMOHook::updateCachedName);
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
                break;
        }
    }

    private static void updateCachedName(Entity entity) {
        if (!(entity instanceof LivingEntity))
            return;

        if (entity.hasMetadata(CUSTOM_NAME_KEY)) {
            entity.removeMetadata(CUSTOM_NAME_KEY, mcMMO);
            entity.setMetadata(CUSTOM_NAME_KEY, new FixedMetadataValue(mcMMO, plugin.getNMSEntities().getCustomName(entity)));
        }
        if (entity.hasMetadata(CUSTOM_NAME_VISIBLE_KEY)) {
            entity.removeMetadata(CUSTOM_NAME_VISIBLE_KEY, mcMMO);
            entity.setMetadata(CUSTOM_NAME_VISIBLE_KEY, new FixedMetadataValue(mcMMO, plugin.getNMSEntities().isCustomNameVisible(entity)));
        }
    }

    private static void handleCombat(LivingEntity target, Player attacker, Entity entityAttacker, double finalDamage) {
        ItemStack heldItem = attacker.getItemInHand();

        McMMOPlayer mcMMOPlayer = UserManager.getPlayer(attacker);

        if (mcMMOPlayer == null)
            return;

        SkillType skillType;

        if (entityAttacker instanceof Wolf) {
            skillType = SkillType.TAMING;
        } else if (entityAttacker instanceof Arrow) {
            skillType = SkillType.ARCHERY;
        } else if (ItemUtils.isSword(heldItem)) {
            skillType = SkillType.SWORDS;
        } else if (ItemUtils.isAxe(heldItem)) {
            skillType = SkillType.AXES;
        } else if (ItemUtils.isUnarmed(heldItem)) {
            skillType = SkillType.UNARMED;
        } else {
            return;
        }

        if (!skillType.shouldProcess(target) || !skillType.getPermissions(attacker))
            return;

        EntityType type = target.getType();

        double baseXp = 0;

        if (target instanceof Animals) {
            baseXp = ExperienceConfig.getInstance().getAnimalsXP();
        } else if (target instanceof Monster) {
            baseXp = ExperienceConfig.getInstance().getCombatXP(type);
        } else if (type == EntityType.IRON_GOLEM) {
            if (!((IronGolem) target).isPlayerCreated()) {
                baseXp = ExperienceConfig.getInstance().getCombatXP(type);
            }
        } else {
            baseXp = ExperienceConfig.getInstance().getCombatXP(type);
        }

        if (isSpawnerEntity(target)) {
            baseXp *= ExperienceConfig.getInstance().getSpawnedMobXpMultiplier();
        } else if (isAnimalBred(target)) {
            baseXp *= ExperienceConfig.getInstance().getBredMobXpMultiplier();
        }

        baseXp *= 10;

        if (baseXp > 0) {
            mcMMOPlayer.beginXpGain(skillType, (float) ((int) (finalDamage * baseXp)), XPGainReason.PVE);
        }
    }

    private static void updateSpawnedEntity(LivingEntity livingEntity) {
        livingEntity.setMetadata(SPAWNER_ENTITY_KEY, new FixedMetadataValue(mcMMO, true));
    }

    private static boolean isSpawnerEntity(LivingEntity livingEntity) {
        return livingEntity.hasMetadata(SPAWNER_ENTITY_KEY);
    }

    private static boolean isAnimalBred(LivingEntity livingEntity) {
        return livingEntity.hasMetadata(BRED_ANIMAL_KEY);
    }

}
