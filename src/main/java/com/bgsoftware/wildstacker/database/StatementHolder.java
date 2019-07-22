package com.bgsoftware.wildstacker.database;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.utils.Executor;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatementHolder {

    private static WildStackerPlugin plugin = WildStackerPlugin.getPlugin();

    private final List<Map<Integer, Object>> batches = new ArrayList<>();

    private final String query;
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
        if(batches.isEmpty())
            SQLHelper.setAutoCommit(false);
        batches.add(new HashMap<>(values));
        values.clear();
        currentIndex = 1;
    }

    public void execute(boolean async) {
        if(async){
            Executor.data(() -> execute(false));
            return;
        }

        String errorQuery = query;
        try(PreparedStatement preparedStatement = SQLHelper.buildStatement(query)){
            if(!batches.isEmpty()){
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
            }
            else {
                for (Map.Entry<Integer, Object> entry : values.entrySet()) {
                    preparedStatement.setObject(entry.getKey(), entry.getValue());
                    errorQuery = errorQuery.replaceFirst("\\?", entry.getValue() + "");
                }
                preparedStatement.executeUpdate();
            }
        }catch(SQLException ex){
            WildStackerPlugin.log("Failed to execute query " + errorQuery);
            ex.printStackTrace();
        }
    }

}
