package com.bgsoftware.wildstacker.loot;

import com.bgsoftware.wildstacker.api.loot.LootEntityAttributes;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

public abstract class FilteredLoot {

    private final List<Predicate<LootEntityAttributes>> entityFilters = new LinkedList<>();
    private final List<Predicate<LootEntityAttributes>> killerFilters = new LinkedList<>();

    protected FilteredLoot(List<Predicate<LootEntityAttributes>> entityFilters, List<Predicate<LootEntityAttributes>> killerFilters) {
        this.entityFilters.addAll(entityFilters);
        this.killerFilters.addAll(killerFilters);
    }

    public boolean checkEntity(@Nullable LootEntityAttributes entity) {
        return checkFiltersOnEntity(this.entityFilters, entity);
    }

    public boolean checkKiller(@Nullable LootEntityAttributes killer) {
        return checkFiltersOnEntity(this.killerFilters, killer);
    }

    private static boolean checkFiltersOnEntity(List<Predicate<LootEntityAttributes>> filters, @Nullable LootEntityAttributes entity) {
        if (filters.isEmpty())
            return true;

        if (entity == null)
            return false;

        for (Predicate<LootEntityAttributes> filter : filters) {
            if (filter.test(entity))
                return true;
        }

        return false;
    }

}
