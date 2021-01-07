package com.bgsoftware.wildstacker.database;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.utils.threads.Executor;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class StatementHolder {

    private static final WildStackerPlugin plugin = WildStackerPlugin.getPlugin();

    private static final EnumMap<Query, IncreasableInteger> queryCalls = new EnumMap<>(Query.class);

    private final List<Map<Integer, Object>> batches = new ArrayList<>();

    private final String query;
    private final Query queryEnum;
    private final Map<Integer, Object> values = new HashMap<>();
    private int currentIndex = 1;

    private boolean isBatch = false;

    StatementHolder(Query query){
        this.queryEnum = query;
        this.query = query.getStatement();
    }

    public StatementHolder setString(String value){
        values.put(currentIndex++, value);
        return this;
    }

    public StatementHolder setInt(int value){
        values.put(currentIndex++, value);
        return this;
    }

    public StatementHolder setShort(short value){
        values.put(currentIndex++, value);
        return this;
    }

    public StatementHolder setDouble(double value){
        values.put(currentIndex++, value);
        return this;
    }

    public StatementHolder setBoolean(boolean value){
        values.put(currentIndex++, value);
        return this;
    }

    public StatementHolder setItemStack(ItemStack itemStack){
        values.put(currentIndex++, itemStack == null ? "" : plugin.getNMSAdapter().serialize(itemStack));
        return this;
    }

    public StatementHolder setLocation(Location loc){
        values.put(currentIndex++, loc == null ? "" : loc.getWorld().getName() + "," + loc.getBlockX() + "," +
                loc.getBlockY() + "," + loc.getBlockZ());
        return this;
    }

    public void addBatch(){
        batches.add(new HashMap<>(values));
        values.clear();
        currentIndex = 1;
    }

    public void prepareBatch(){
        isBatch = true;
    }

    public void execute(boolean async) {
        if(async && !Executor.isDataThread()){
            Executor.data(() -> execute(false));
            return;
        }

        SQLHelper.waitForConnection();

        try {
            StringHolder errorQuery = new StringHolder(query);

            synchronized (SQLHelper.getMutex()) {
                queryCalls.computeIfAbsent(queryEnum, q -> new IncreasableInteger()).increase();
                SQLHelper.buildStatement(query, preparedStatement -> {
                    if (isBatch) {
                        if (batches.isEmpty()) {
                            isBatch = false;
                            return;
                        }

                        SQLHelper.setAutoCommit(false);

                        for (Map<Integer, Object> values : batches) {
                            for (Map.Entry<Integer, Object> entry : values.entrySet()) {
                                preparedStatement.setObject(entry.getKey(), entry.getValue());
                                errorQuery.value = errorQuery.value.replaceFirst("\\?", entry.getValue() + "");
                            }
                            preparedStatement.addBatch();
                        }

                        preparedStatement.executeBatch();
                        try {
                            SQLHelper.commit();
                        }catch(Throwable ignored){}

                        SQLHelper.setAutoCommit(true);
                    } else {
                        for (Map.Entry<Integer, Object> entry : values.entrySet()) {
                            preparedStatement.setObject(entry.getKey(), entry.getValue());
                            errorQuery.value = errorQuery.value.replaceFirst("\\?", entry.getValue() + "");
                        }
                        preparedStatement.executeUpdate();
                    }
                }, ex -> {
                    WildStackerPlugin.log("&cFailed to execute query " + errorQuery);
                    ex.printStackTrace();
                });
            }
        } finally {
            values.clear();
        }
    }

    public static EnumMap<Query, IncreasableInteger> getQueryCalls() {
        return queryCalls;
    }

    private static class StringHolder{

        private String value;

        StringHolder(String value){
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    public static final class IncreasableInteger{

        private int value = 0;

        IncreasableInteger(){

        }

        public int get() {
            return value;
        }

        public void increase(){
            value++;
        }

    }

}
