package com.bgsoftware.wildstacker.hooks;

import org.bukkit.entity.Player;

public interface EconomyProvider {

    double getMoneyInBank(Player player);

    void withdrawMoney(Player player, double amount);

}
