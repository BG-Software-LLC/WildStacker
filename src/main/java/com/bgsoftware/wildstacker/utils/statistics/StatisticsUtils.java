package com.bgsoftware.wildstacker.utils.statistics;

import org.bukkit.Bukkit;
import org.bukkit.Statistic;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerStatisticIncrementEvent;

public final class StatisticsUtils {

    public static void incrementStatistic(Player player, Statistic statistic, int amount){
        if(callPlayerStatisticIncrement(player, statistic, amount, null))
            player.incrementStatistic(statistic, amount);
    }

    public static void incrementStatistic(Player player, Statistic statistic, EntityType entityType, int amount){
        if(callPlayerStatisticIncrement(player, statistic, amount, entityType))
            player.incrementStatistic(statistic, entityType, amount);
    }

    private static boolean callPlayerStatisticIncrement(Player player, Statistic statistic, int newValue, EntityType entityType){
        try {
            int currentValue = entityType == null ? player.getStatistic(statistic) : player.getStatistic(statistic, entityType);
            PlayerStatisticIncrementEvent playerStatisticIncrementEvent = new PlayerStatisticIncrementEvent(player, statistic, currentValue, currentValue + newValue, entityType);
            Bukkit.getPluginManager().callEvent(playerStatisticIncrementEvent);
            return !playerStatisticIncrementEvent.isCancelled();
        }catch (Exception ex){
            // Can happen with invalid entity-types (MyPet, for example)
            return false;
        }
    }

}
