package com.bgsoftware.wildstacker.hooks.listeners;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public interface IEntityCombatListener {

    void handleCombat(LivingEntity livingEntity, Player killer, Entity entityDamager, double finalDamage);

}
