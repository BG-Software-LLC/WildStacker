package com.bgsoftware.wildstacker.nms.v1_7_R4.serializer;

import com.bgsoftware.wildstacker.api.enums.SpawnCause;
import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.api.objects.StackedItem;
import com.bgsoftware.wildstacker.hooks.IDataSerializer;
import com.bgsoftware.wildstacker.objects.WStackedEntity;
import net.minecraft.server.v1_7_R4.EntityItem;
import net.minecraft.server.v1_7_R4.EntityLiving;
import net.minecraft.server.v1_7_R4.IScoreboardCriteria;
import net.minecraft.server.v1_7_R4.MobEffect;
import net.minecraft.server.v1_7_R4.MobEffectList;
import net.minecraft.server.v1_7_R4.Scoreboard;
import net.minecraft.server.v1_7_R4.ScoreboardObjective;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftItem;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftLivingEntity;

import java.util.UUID;

public class DataSerializerImpl implements IDataSerializer {

    private static final MobEffectCustomData STACK_AMOUNT = MobEffectCustomData.newEffect(31)
            .b("ws.stackAmount").withVanillaEffect(MobEffectList.FASTER_DIG);
    private static final MobEffectCustomData SPAWN_CAUSE = MobEffectCustomData.newEffect(30)
            .b("ws.spawnCause").withVanillaEffect(MobEffectList.SLOWER_DIG);
    private static final MobEffectCustomData HAS_NAMETAG = MobEffectCustomData.newEffect(29)
            .b("ws.hasNametag").withVanillaEffect(MobEffectList.SATURATION);
    private static final MobEffectCustomData UPGRADE = MobEffectCustomData.newEffect(28)
            .b("ws.upgrade").withVanillaEffect(MobEffectList.BLINDNESS);

    private static final DataSerializerImpl INSTANCE = new DataSerializerImpl();

    public static DataSerializerImpl getInstance() {
        return INSTANCE;
    }

    private DataSerializerImpl() {

    }

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

            worldScoreboard.resetPlayerScores(entityLiving.getUniqueID() + "");
        }

        {
            // This section is used to convert the effects into custom ones.
            // This is done because we cannot save custom effects, otherwise 1.8 clients crash.
            {
                MobEffect stackAmountLoad = (MobEffect) entityLiving.effects.get(STACK_AMOUNT.getVanillaEffect().id),
                        spawnCauseLoad = (MobEffect) entityLiving.effects.get(SPAWN_CAUSE.getVanillaEffect().id),
                        hasNametagLoad = (MobEffect) entityLiving.effects.get(HAS_NAMETAG.getVanillaEffect().id),
                        hasUpgradeLoad = (MobEffect) entityLiving.effects.get(UPGRADE.getVanillaEffect().id);

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
        //noinspection unchecked
        entityLiving.effects.put(mobEffect.getCustomId(), mobEffect);
    }

}
