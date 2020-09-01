package com.bgsoftware.wildstacker.database;

public enum Query {

    ENTITY_INSERT("REPLACE INTO entities VALUES(?, ?, ?);", 3),
    ENTITIES_DELETE("DELETE FROM entities;", 0),

    ITEM_INSERT("REPLACE INTO items VALUES(?, ?);", 2),
    ITEMS_DELETE("DELETE FROM items;", 0),

    SPAWNER_INSERT("REPLACE INTO spawners VALUES(?, ?);", 2),
    SPAWNER_DELETE("DELETE FROM spawners WHERE location=?;", 1),

    BARREL_INSERT("REPLACE INTO barrels VALUES(?, ?, ?);", 3),
    BARREL_DELETE("DELETE FROM barrels WHERE location=?;", 1);

    private final String query;
    private final int parametersCount;

    Query(String query, int parametersCount) {
        this.query = query;
        this.parametersCount = parametersCount;
    }

    String getStatement(){
        return query;
    }

    int getParametersCount() {
        return parametersCount;
    }

    public QueryParameters insertParameters(){
        return new QueryParameters(this);
    }

}
