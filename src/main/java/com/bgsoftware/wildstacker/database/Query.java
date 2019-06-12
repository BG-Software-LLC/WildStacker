package com.bgsoftware.wildstacker.database;

public enum Query {

    ENTITY_UPDATE_STACK_AMOUNT("UPDATE entities SET stackAmount=? WHERE uuid=?;"),
    ENTITY_UPDATE_SPAWN_CAUSE("UPDATE entities SET spawnCause=? WHERE uuid=?;"),
    ENTITY_INSERT("INSERT INTO entities VALUES(?, ?, ?);"),
    ENTITY_DELETE("DELETE FROM entities WHERE uuid=?;"),

    ITEM_UPDATE_STACK_AMOUNT("UPDATE items SET stackAmount=? WHERE uuid=?;"),
    ITEM_INSERT("INSERT INTO items VALUES(?, ?);"),
    ITEM_DELETE("DELETE FROM items WHERE uuid=?;"),

    SPAWNER_UPDATE_STACK_AMOUNT("UPDATE spawners SET stackAmount=? WHERE location=?;"),
    SPAWNER_INSERT("INSERT INTO spawners VALUES(?, ?);"),
    SPAWNER_DELETE("DELETE FROM spawners WHERE location=?;"),

    BARREL_UPDATE_STACK_AMOUNT("UPDATE spawners SET stackAmount=? WHERE location=?;"),
    BARREL_INSERT("INSERT INTO barrels VALUES(?, ?, ?);"),
    BARREL_DELETE("DELETE FROM barrels WHERE location=?;");

    private String query;

    Query(String query) {
        this.query = query;
    }

    public String getStatement(){
        return query;
    }

    public StatementHolder getStatementHolder(){
        return new StatementHolder(this);
    }
}
