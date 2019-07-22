package com.bgsoftware.wildstacker.hooks;

import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.loot.LootTable;
import com.bgsoftware.wildstacker.loot.LootTableTemp;
import com.songoda.epicbosses.CustomBosses;
import com.songoda.epicbosses.droptable.DropTable;
import com.songoda.epicbosses.droptable.elements.DropTableElement;
import com.songoda.epicbosses.droptable.elements.GiveTableElement;
import com.songoda.epicbosses.droptable.elements.GiveTableSubElement;
import com.songoda.epicbosses.droptable.elements.SprayTableElement;
import com.songoda.epicbosses.entity.BossEntity;
import com.songoda.epicbosses.handlers.IGetDropTableListItem;
import com.songoda.epicbosses.handlers.droptable.GetDropTableCommand;
import com.songoda.epicbosses.handlers.droptable.GetDropTableItemStack;
import com.songoda.epicbosses.holder.ActiveBossHolder;
import com.songoda.epicbosses.holder.DeadBossHolder;
import com.songoda.epicbosses.managers.BossDropTableManager;
import com.songoda.epicbosses.managers.BossEntityManager;
import com.songoda.epicbosses.utils.NumberUtils;
import com.songoda.epicbosses.utils.RandomUtils;
import com.songoda.epicbosses.utils.ServerUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class EpicBossesHook {

    private static BossEntityManager bossEntityManager;
    private static BossDropTableManager bossDropTableManager;
    private static IGetDropTableListItem<ItemStack> getDropTableItemStack;
    private static IGetDropTableListItem<List<String>> getDropTableCommand;

    static{
        bossEntityManager = CustomBosses.get().getBossEntityManager();
        bossDropTableManager = CustomBosses.get().getBossDropTableManager();
        getDropTableItemStack = new GetDropTableItemStack(CustomBosses.get());
        getDropTableCommand = new GetDropTableCommand();
//        try{
//            Field dropTableItemStack = BossDropTableManager.class.getDeclaredField("getDropTableItemStack");
//            dropTableItemStack.setAccessible(true);
//            //noinspection unchecked
//            getDropTableItemStack = (IGetDropTableListItem<ItemStack>) dropTableItemStack.get(bossDropTableManager);
//            dropTableItemStack.setAccessible(false);
//            Field dropTableCommand = BossDropTableManager.class.getDeclaredField("getDropTableCommand");
//            dropTableCommand.setAccessible(true);
//            //noinspection unchecked
//            getDropTableCommand = (IGetDropTableListItem<List<String>>) dropTableCommand.get(bossDropTableManager);
//            dropTableCommand.setAccessible(false);
//        }catch(Exception ignored) { }
    }

    public static LootTable getLootTable(){
        return new LootTableTemp(){

            @Override
            public List<ItemStack> getDrops(StackedEntity stackedEntity, int lootBonusLevel, int stackAmount) {
                List<ItemStack> drops = new ArrayList<>();
                for(int i = 0; i < stackAmount; i++)
                    drops.addAll(getBossDrops(stackedEntity.getLivingEntity()));
                return drops;
            }

        };
    }

    private static List<ItemStack> getBossDrops(LivingEntity livingEntity){
        List<ItemStack> drops = new ArrayList<>();

        ActiveBossHolder activeBossHolder = bossEntityManager.getActiveBossHolder(livingEntity);

        BossEntity bossEntity = activeBossHolder.getBossEntity();
        Map<UUID, Double> mapOfDamage = bossEntityManager.getSortedMapOfDamage(activeBossHolder);
        Map<UUID, Double> mapOfPercent = bossEntityManager.getPercentageMap(mapOfDamage);

        DeadBossHolder deadBossHolder = new DeadBossHolder(bossEntity, livingEntity.getLocation(), mapOfDamage, mapOfPercent);
        DropTable dropTable = bossEntityManager.getDropTable(bossEntity);

        String dropType = dropTable.getDropType();

        if (dropType == null) {
            return drops;
        }

        if(dropType.equalsIgnoreCase("SPRAY")){
            SprayTableElement sprayTableElement = dropTable.getSprayTableData();
            drops.addAll(bossDropTableManager.getSprayItems(sprayTableElement));
        }

        else if (dropType.equalsIgnoreCase("GIVE")) {
            GiveTableElement giveTableElement = dropTable.getGiveTableData();
            drops.addAll(getDropItems(giveTableElement, deadBossHolder));
        }

        else if (dropType.equalsIgnoreCase("DROP")) {
            DropTableElement dropTableElement = dropTable.getDropTableData();
            drops.addAll(bossDropTableManager.getDropItems(dropTableElement));
        }

        return drops;
    }

    private static List<ItemStack> getDropItems(GiveTableElement giveTableElement, DeadBossHolder deadBossHolder) {
        Map<String, Map<String, GiveTableSubElement>> rewards = giveTableElement.getGiveRewards();
        Map<UUID, Double> mapOfDamage = deadBossHolder.getSortedDamageMap();
        Map<UUID, Double> percentMap = deadBossHolder.getPercentageMap();
        List<UUID> positions = new ArrayList<>(mapOfDamage.keySet());
        ServerUtils serverUtils = ServerUtils.get();

        List<ItemStack> drops = new ArrayList<>();

        rewards.forEach((positionString, lootMap) -> {
            if(!NumberUtils.get().isInt(positionString)) {
                return;
            }

            int position = NumberUtils.get().getInteger(positionString) - 1;

            if(position >= positions.size()) return;

            UUID uuid = positions.get(position);
            Player player = Bukkit.getPlayer(uuid);
            double percentage = percentMap.getOrDefault(uuid, -1.0);
            List<ItemStack> totalRewards = new ArrayList<>();
            List<String> totalCommands = new ArrayList<>();

            if(player == null) return;

            lootMap.forEach((key, subElement) -> {
                Double requiredPercentage = subElement.getRequiredPercentage();
                Integer maxDrops = subElement.getMaxDrops(), maxCommands = subElement.getMaxCommands();
                Boolean randomDrops = subElement.getRandomDrops(), randomCommands = subElement.getRandomCommands();

                if(requiredPercentage == null) requiredPercentage = 0.0D;
                if(maxDrops == null) maxDrops = -1;
                if(maxCommands == null) maxCommands = -1;
                if(randomDrops == null) randomDrops = false;
                if(randomCommands == null) randomCommands = false;

                if(requiredPercentage > percentage) return;

                totalRewards.addAll(getCustomRewards(randomDrops, maxDrops, subElement.getItems()));
                totalCommands.addAll(getCommands(randomCommands, maxCommands, subElement.getCommands()));
            });

            totalCommands.replaceAll(s -> s = s.replace("%player%", player.getName()));
            totalCommands.forEach(serverUtils::sendConsoleCommand);

            drops.addAll(totalRewards);
        });

        return drops;
    }

    private static List<ItemStack> getCustomRewards(boolean random, int max, Map<String, Double> chanceMap) {
        List<ItemStack> newListToMerge = new ArrayList<>();

        if(chanceMap == null) return newListToMerge;

        List<String> keyList = new ArrayList<>(chanceMap.keySet());

        if(random) Collections.shuffle(keyList);

        for(String itemName : keyList) {
            Double chance = chanceMap.get(itemName);

            if(!RandomUtils.get().canPreformAction(chance)) continue;
            if((max > 0) && (newListToMerge.size() >= max)) break;

            ItemStack itemStack = getDropTableItemStack.getListItem(itemName);

            if(itemStack == null) {
                continue;
            }

            newListToMerge.add(itemStack);
        }

        return newListToMerge;
    }

    private static List<String> getCommands(boolean random, int max, Map<String, Double> chanceMap) {
        List<String> newListToMerge = new ArrayList<>();

        if(chanceMap == null) return newListToMerge;

        List<String> keyList = new ArrayList<>(chanceMap.keySet());

        if(random) Collections.shuffle(keyList);

        for(String itemName : keyList) {
            Double chance = chanceMap.get(itemName);

            if(!RandomUtils.get().canPreformAction(chance)) continue;
            if((max > 0) && (newListToMerge.size() >= max)) break;

            List<String> commands = getDropTableCommand.getListItem(itemName);

            if(commands == null) {
                continue;
            }

            newListToMerge.addAll(commands);
        }

        return newListToMerge;
    }



}
