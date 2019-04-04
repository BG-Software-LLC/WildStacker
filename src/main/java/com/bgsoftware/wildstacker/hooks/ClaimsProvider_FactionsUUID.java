package com.bgsoftware.wildstacker.hooks;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public final class ClaimsProvider_FactionsUUID implements ClaimsProvider{

    @Override
    public boolean hasClaimAccess(Player player, Location location) {
        FPlayer fPlayer = FPlayers.getInstance().getByPlayer(player);
        Faction faction = Board.getInstance().getFactionAt(new FLocation(location));
        return faction.isWilderness() || fPlayer.isAdminBypassing() || (fPlayer.hasFaction() && fPlayer.getFaction().equals(faction));
    }
}
