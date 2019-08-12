package com.bgsoftware.wildstacker.hooks;

import com.massivecraft.factions.entity.BoardColl;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.MPlayer;
import com.massivecraft.massivecore.ps.PS;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public final class ClaimsProvider_MassiveFactions implements ClaimsProvider {

    @Override
    public boolean hasClaimAccess(Player player, Location location) {
        MPlayer mPlayer = MPlayer.get(player);
        boolean overriding = false;

        try {
            overriding = mPlayer.isOverriding();
        } catch (Throwable ex) {
            try {
                overriding = (boolean) mPlayer.getClass().getMethod("isUsingAdminMode").invoke(mPlayer);
            } catch (Exception ignored) { }
        }

        Faction faction = BoardColl.get().getFactionAt(PS.valueOf(location));

        return faction == null || faction.getName().equals(ChatColor.DARK_GREEN + "Wilderness") || overriding || (mPlayer.hasFaction() && mPlayer.getFaction().equals(faction));
    }
}
