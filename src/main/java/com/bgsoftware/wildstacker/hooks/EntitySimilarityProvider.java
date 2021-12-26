package com.bgsoftware.wildstacker.hooks;

import com.bgsoftware.wildstacker.api.enums.StackCheckResult;
import org.bukkit.entity.Entity;

public interface EntitySimilarityProvider {

    StackCheckResult areSimilar(Entity entity, Entity other);

}
