package com.bgsoftware.wildstacker.hooks;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

@SuppressWarnings("unused")
public final class EconomyProvider_Vault implements EconomyProvider {

    private static Economy econ;

    public static boolean isCompatible() {
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (rsp != null)
            econ = rsp.getProvider();

        if (econ != null)
            WildStackerPlugin.log("Using Vault as an economy provider.");

        return econ != null;
    }

    @Override
    public double getMoneyInBank(Player player) {
        if (!econ.hasAccount(player))
            econ.createPlayerAccount(player);

        return econ.getBalance(player);
    }

    @Override
    public void withdrawMoney(Player player, double amount) {
        if (!econ.hasAccount(player))
            econ.createPlayerAccount(player);

        econ.withdrawPlayer(player, amount);
    }

}
