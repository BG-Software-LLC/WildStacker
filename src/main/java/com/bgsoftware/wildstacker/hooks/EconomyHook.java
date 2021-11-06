package com.bgsoftware.wildstacker.hooks;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public final class EconomyHook {

    private static Economy economy = null;

    public static double getMoneyInBank(Player player) {
        if (!economy.hasAccount(player))
            economy.createPlayerAccount(player);

        return economy.getBalance(player);
    }

    public static void withdrawMoney(Player player, double amount) {
        if (!economy.hasAccount(player))
            economy.createPlayerAccount(player);

        economy.withdrawPlayer(player, amount);
    }

    public static void setEnabled(boolean enabled) {
        try {
            if (enabled) {
                RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
                if (rsp != null) {
                    economy = rsp.getProvider();
                    PluginHooks.isVaultEnabled = true;
                    return;
                }
            }
        } catch (Throwable ignored) {
        }

        PluginHooks.isVaultEnabled = false;
    }

}
