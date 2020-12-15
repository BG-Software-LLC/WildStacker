package com.bgsoftware.wildstacker.database;

public enum Query {

    SPAWNER_INSERT("REPLACE INTO spawners VALUES(?, ?, ?);", 3),
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
