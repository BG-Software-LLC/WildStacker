package com.bgsoftware.wildstacker.hooks;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public  final class EconomyHook {

    private static Economy economy = null;
    private static boolean vaultEnabled = Bukkit.getPluginManager().getPlugin("Vault") != null;

    public static double getMoneyInBank(Player player){
        if(isVaultEnabled()) {
            if (!economy.hasAccount(player))
                economy.createPlayerAccount(player);

            return economy.getBalance(player);
        }
        return 0;
    }

    public static void withdrawMoney(Player player, double amount){
        if(isVaultEnabled()) {
            if (!economy.hasAccount(player))
                economy.createPlayerAccount(player);

            economy.withdrawPlayer(player, amount);
        }
    }

    public static boolean isVaultEnabled(){
        return vaultEnabled;
    }

    public static void register(){
        if (isVaultEnabled()) {
            RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
            if (rsp != null) {
                economy = rsp.getProvider();
            }
        }
    }

}
