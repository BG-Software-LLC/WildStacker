package com.bgsoftware.wildstacker.database;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.utils.threads.Executor;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatementHolder {

    private static final WildStackerPlugin plugin = WildStackerPlugin.getPlugin();

    private final List<Map<Integer, Object>> batches = new ArrayList<>();

    private final String query;
    private StackTraceElement[] originalStackTrace = new StackTraceElement[0];
    private final Map<Integer, Object> values = new HashMap<>();
    private int currentIndex = 1;

    StatementHolder(Query query){
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

    public StatementHolder setLong(long value){
        values.put(currentIndex++, value);
        return this;
    }

    public StatementHolder setFloat(float value){
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
        values.put(currentIndex++, SQLHelper.getLocation(loc));
        return this;
    }

    public void addBatch(){
        batches.add(new HashMap<>(values));
        values.clear();
        currentIndex = 1;
    }

    public int getBatchesSize(){
        return batches.size();
    }

    public void execute(boolean async) {
        if(async){
            StackTraceElement[] stackTraceElements = new Exception().getStackTrace();
            this.originalStackTrace = Arrays.copyOfRange(stackTraceElements, 1, stackTraceElements.length);
            Executor.data(() -> execute(false));
            return;
        }

        String errorQuery = query;
        try (PreparedStatement preparedStatement = SQLHelper.buildStatement(query)) {
            if (!batches.isEmpty()) {
                SQLHelper.setAutoCommit(false);
                for (Map<Integer, Object> values : batches) {
                    for (Map.Entry<Integer, Object> entry : values.entrySet()) {
                        preparedStatement.setObject(entry.getKey(), entry.getValue());
                        errorQuery = errorQuery.replaceFirst("\\?", entry.getValue() + "");
                    }
                    preparedStatement.addBatch();
                }
                preparedStatement.executeBatch();
                SQLHelper.commit();
                SQLHelper.setAutoCommit(true);
            } else {
                for (Map.Entry<Integer, Object> entry : values.entrySet()) {
                    preparedStatement.setObject(entry.getKey(), entry.getValue());
                    errorQuery = errorQuery.replaceFirst("\\?", entry.getValue() + "");
                }
                preparedStatement.executeUpdate();
            }
        } catch (SQLException ex) {
            WildStackerPlugin.log("Failed to execute query " + errorQuery);
            if(originalStackTrace.length > 0){
                StackTraceElement[] stackTraceElements = ex.getStackTrace();
                StackTraceElement[] newStackTrace = Arrays.copyOf(stackTraceElements, stackTraceElements.length + originalStackTrace.length - 4);
                //noinspection all
                for(int i = stackTraceElements.length - 4; i < newStackTrace.length; i++)
                    newStackTrace[i] = originalStackTrace[i - stackTraceElements.length + 4];
                ex.setStackTrace(newStackTrace);
            }
            ex.printStackTrace();
        }
    }

}
