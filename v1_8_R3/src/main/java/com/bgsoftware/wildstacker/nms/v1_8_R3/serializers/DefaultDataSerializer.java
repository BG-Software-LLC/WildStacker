package com.bgsoftware.wildstacker.nms.v1_8_R3.serializers;

import com.bgsoftware.wildstacker.api.enums.SpawnCause;
import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.api.objects.StackedItem;
import com.bgsoftware.wildstacker.hooks.IDataSerializer;
import com.bgsoftware.wildstacker.objects.WStackedEntity;
import net.minecraft.server.v1_8_R3.EntityItem;
import net.minecraft.server.v1_8_R3.EntityLiving;
import net.minecraft.server.v1_8_R3.IScoreboardCriteria;
import net.minecraft.server.v1_8_R3.MinecraftKey;
import net.minecraft.server.v1_8_R3.MobEffect;
import net.minecraft.server.v1_8_R3.MobEffectList;
import net.minecraft.server.v1_8_R3.Scoreboard;
import net.minecraft.server.v1_8_R3.ScoreboardObjective;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftItem;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;

import java.util.UUID;

public class DefaultDataSerializer implements IDataSerializer {

    public static MobEffectCustomData STACK_AMOUNT = MobEffectCustomData.newEffect(31, new MinecraftKey("ws:stackAmount"))
            .c("ws.stackAmount").withVanillaEffect(MobEffectList.FASTER_DIG);
    public static MobEffectCustomData SPAWN_CAUSE = MobEffectCustomData.newEffect(30, new MinecraftKey("ws:spawnCause"))
            .c("ws.spawnCause").withVanillaEffect(MobEffectList.SLOWER_DIG);
    public static MobEffectCustomData HAS_NAMETAG = MobEffectCustomData.newEffect(29, new MinecraftKey("ws:hasNametag"))
            .c("ws.hasNametag").withVanillaEffect(MobEffectList.SATURATION);
    public static MobEffectCustomData UPGRADE = MobEffectCustomData.newEffect(28, new MinecraftKey("ws:upgrade"))
            .c("ws.upgrade").withVanillaEffect(MobEffectList.BLINDNESS);

    @Override
    public void saveEntity(StackedEntity stackedEntity) {
        EntityLiving entityLiving = ((CraftLivingEntity) stackedEntity.getLivingEntity()).getHandle();
        setEffect(entityLiving, new CustomMobEffect(STACK_AMOUNT, stackedEntity.getStackAmount()));
        setEffect(entityLiving, new CustomMobEffect(SPAWN_CAUSE, stackedEntity.getSpawnCause().getId()));
        if (stackedEntity.hasNameTag())
            setEffect(entityLiving, new CustomMobEffect(HAS_NAMETAG, 1));
        int upgradeId = ((WStackedEntity) stackedEntity).getUpgradeId();
        if (upgradeId != 0)
            setEffect(entityLiving, new CustomMobEffect(UPGRADE, upgradeId));
    }

    @Override
    public void loadEntity(StackedEntity stackedEntity) {
        EntityLiving entityLiving = ((CraftLivingEntity) stackedEntity.getLivingEntity()).getHandle();

        // Loading old data from scoreboards
        {
            Scoreboard worldScoreboard = entityLiving.world.getScoreboard();

            int stackAmount = getData(worldScoreboard, entityLiving.getUniqueID(), "ws:stack-amount");
            int spawnCause = getData(worldScoreboard, entityLiving.getUniqueID(), "ws:stack-cause");
            int nameTag = getData(worldScoreboard, entityLiving.getUniqueID(), "ws:name-tag");

            if (stackAmount > 0)
                stackedEntity.setStackAmount(stackAmount, false);

            if (spawnCause > 0)
                stackedEntity.setSpawnCause(SpawnCause.valueOf(spawnCause));

            if (nameTag == 1)
                ((WStackedEntity) stackedEntity).setNameTag();

            worldScoreboard.resetPlayerScores(entityLiving.getUniqueID() + "", null);
        }

        {
            // This section is used to convert the effects into custom ones.
            // This is done because we cannot save custom effects, otherwise 1.8 clients crash.
            {
                MobEffect stackAmountLoad = entityLiving.effects.get(STACK_AMOUNT.vanillaEffect.id),
                        spawnCauseLoad = entityLiving.effects.get(SPAWN_CAUSE.vanillaEffect.id),
                        hasNametagLoad = entityLiving.effects.get(HAS_NAMETAG.vanillaEffect.id),
                        hasUpgradeLoad = entityLiving.effects.get(UPGRADE.vanillaEffect.id);

                if (stackAmountLoad != null && stackAmountLoad.getDuration() > 2140000000) {
                    setEffect(entityLiving, new CustomMobEffect(STACK_AMOUNT, stackAmountLoad.getAmplifier()));
                    entityLiving.effects.remove(stackAmountLoad.getEffectId());
                }

                if (spawnCauseLoad != null && spawnCauseLoad.getDuration() > 2140000000) {
                    setEffect(entityLiving, new CustomMobEffect(SPAWN_CAUSE, spawnCauseLoad.getAmplifier()));
                    entityLiving.effects.remove(spawnCauseLoad.getEffectId());
                }

                if (hasNametagLoad != null && hasNametagLoad.getDuration() > 2140000000) {
                    setEffect(entityLiving, new CustomMobEffect(HAS_NAMETAG, hasNametagLoad.getAmplifier()));
                    entityLiving.effects.remove(hasNametagLoad.getEffectId());
                }

                if (hasUpgradeLoad != null && hasUpgradeLoad.getDuration() > 2140000000) {
                    setEffect(entityLiving, new CustomMobEffect(UPGRADE, hasUpgradeLoad.getAmplifier()));
                    entityLiving.effects.remove(hasUpgradeLoad.getEffectId());
                }
            }

            // Loading data from custom effects
            {
                MobEffect stackAmount = entityLiving.getEffect(STACK_AMOUNT),
                        spawnCause = entityLiving.getEffect(SPAWN_CAUSE),
                        hasNametag = entityLiving.getEffect(HAS_NAMETAG),
                        upgrade = entityLiving.getEffect(UPGRADE);

                if (stackAmount != null)
                    stackedEntity.setStackAmount(stackAmount.getAmplifier(), false);

                if (spawnCause != null)
                    stackedEntity.setSpawnCause(SpawnCause.valueOf(spawnCause.getAmplifier()));

                if (hasNametag != null && hasNametag.getAmplifier() == 1)
                    ((WStackedEntity) stackedEntity).setNameTag();

                if (upgrade != null && upgrade.getAmplifier() != 0)
                    ((WStackedEntity) stackedEntity).setUpgradeId(upgrade.getAmplifier());
            }
        }
    }

    @Override
    public void saveItem(StackedItem stackedItem) {
        if (stackedItem.getStackAmount() > stackedItem.getItemStack().getType().getMaxStackSize()) {
            EntityItem entityItem = (EntityItem) ((CraftItem) stackedItem.getItem()).getHandle();
            Scoreboard worldScoreboard = entityItem.world.getScoreboard();
            saveData(worldScoreboard, entityItem.getUniqueID(), "ws:stack-amount", stackedItem.getStackAmount());
        }
    }

    @Override
    public void loadItem(StackedItem stackedItem) {
        EntityItem entityItem = (EntityItem) ((CraftItem) stackedItem.getItem()).getHandle();
        Scoreboard worldScoreboard = entityItem.world.getScoreboard();

        int stackAmount = getData(worldScoreboard, entityItem.getUniqueID(), "ws:stack-amount");
        if (stackAmount > 0)
            stackedItem.setStackAmount(stackAmount, false);
    }

    private static void saveData(Scoreboard scoreboard, UUID entity, String key, int value) {
        ScoreboardObjective objective = scoreboard.getObjective(key);
        if (objective == null)
            objective = scoreboard.registerObjective(key, IScoreboardCriteria.b);

        scoreboard.getPlayerScoreForObjective(entity + "", objective).setScore(value);
    }

    private static int getData(Scoreboard scoreboard, UUID entity, String key) {
        ScoreboardObjective objective = scoreboard.getObjective(key);

        if (objective == null || !scoreboard.getPlayers().contains(entity + ""))
            return -1;

        return scoreboard.getPlayerScoreForObjective(entity + "", objective).getScore();
    }

    private static void setEffect(EntityLiving entityLiving, CustomMobEffect mobEffect) {
        entityLiving.effects.put(mobEffect.getCustomId(), mobEffect);
    }

    private static final class MobEffectCustomData extends MobEffectList {

        private MobEffectList vanillaEffect;

        MobEffectCustomData(int id, MinecraftKey minecraftKey) {
            super(id, minecraftKey, false, 16262179);
        }

        static MobEffectCustomData newEffect(int id, MinecraftKey minecraftKey) {
            try {
                new MobEffectCustomData(id, minecraftKey);
            } catch (Exception ignored) {
            }
            return (MobEffectCustomData) MobEffectList.byId[id];
        }

        public MobEffectCustomData withVanillaEffect(MobEffectList vanillaEffect) {
            this.vanillaEffect = vanillaEffect;
            return this;
        }

        public MobEffectList getVanillaEffect() {
            return vanillaEffect;
        }

        @Override
        public MobEffectCustomData c(String s) {
            return (MobEffectCustomData) super.c(s);
        }

    }

    private static final class CustomMobEffect extends MobEffect {

        private final int customId;

        CustomMobEffect(MobEffectCustomData mobEffectCustomData, int value) {
            super(mobEffectCustomData.vanillaEffect.id, Integer.MAX_VALUE, value, false, false);
            customId = mobEffectCustomData.id;
        }

        public int getCustomId() {
            return customId;
        }
    }

}
