package com.bgsoftware.wildstacker.database;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.utils.Executor;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class StatementHolder {

    private static WildStackerPlugin plugin = WildStackerPlugin.getPlugin();

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

    public StatementHolder setLocation(Location loc){
        values.put(currentIndex++, loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ());
        return this;
    }

    public StatementHolder setItemStack(ItemStack itemStack){
        values.put(currentIndex++, plugin.getNMSAdapter().serialize(itemStack));
        return this;
    }

    public void execute(boolean async) {
        if(async){
            Executor.async(() -> execute(false));
            return;
        }

        String errorQuery = query;
        try(PreparedStatement preparedStatement = SQLHelper.buildStatement(query)){
            for(Map.Entry<Integer, Object> entry : values.entrySet()) {
                preparedStatement.setObject(entry.getKey(), entry.getValue());
                errorQuery = errorQuery.replaceFirst("\\?", entry.getValue() + "");
            }

            preparedStatement.executeUpdate();
        }catch(SQLException ex){
            WildStackerPlugin.log("Failed to execute query " + errorQuery);
            ex.printStackTrace();
        }
    }

}
