package com.bgsoftware.wildstacker.database;

import com.bgsoftware.wildstacker.utils.Executor;
import org.bukkit.Location;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SQLHelper {

    private static Connection conn;

    private SQLHelper(){}

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void init(File file) throws ClassNotFoundException, SQLException {
        if(!file.exists()){
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }catch(Exception ex){
                ex.printStackTrace();
                return;
            }
        }

        Class.forName("org.sqlite.JDBC");
        String sqlURL = "jdbc:sqlite:" + file.getAbsolutePath().replace("\\", "/");
        conn = DriverManager.getConnection(sqlURL);
    }

    public static void executeUpdate(String statement){
        try(PreparedStatement preparedStatement = conn.prepareStatement(statement)){
            preparedStatement.executeUpdate();
        }catch(SQLException ex){
            System.out.println(statement);
            ex.printStackTrace();
        }
    }

    public static boolean doesConditionExist(String statement){
        boolean ret = false;

        try(PreparedStatement preparedStatement = conn.prepareStatement(statement); ResultSet resultSet = preparedStatement.executeQuery()){
            ret = resultSet.next();
        }catch(SQLException ex){
            ex.printStackTrace();
        }

        return ret;
    }

    public static void runIfConditionNotExist(String statement, Runnable runnable){
        Executor.data(() -> {
            if(!doesConditionExist(statement))
                runnable.run();
        });
    }

    public static String getLocation(Location loc){
        return loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();
    }

    public static void setAutoCommit(boolean autoCommit){
        try {
            conn.setAutoCommit(autoCommit);
        }catch(SQLException ex){
            ex.printStackTrace();
        }
    }

    public static void commit(){
        try {
            conn.commit();
        }catch(SQLException ex){
            ex.printStackTrace();
        }
    }

    public static void executeQuery(String statement, QueryCallback callback){
        try(PreparedStatement preparedStatement = conn.prepareStatement(statement); ResultSet resultSet = preparedStatement.executeQuery()){
            callback.run(resultSet);
        }catch(SQLException ex){
            ex.printStackTrace();
        }
    }

    public interface QueryCallback{

        void run(ResultSet resultSet) throws SQLException;

    }

    public static void close(){
        try{
            conn.close();
        }catch(SQLException ex){
            ex.printStackTrace();
        }
    }

    public static PreparedStatement buildStatement(String query) throws SQLException{
        return conn.prepareStatement(query);
    }

}

