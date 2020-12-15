package com.bgsoftware.wildstacker.database;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Consumer;

public final class Database {

    private static final int CONNECTION_TIMEOUT = 900;

    private static String dbFilePath = "";

    private static Connection connection = null;
    private static long lastConnectionCreation = 0;

    private Database(){}

    public static void start(File databaseFile){
        Database.dbFilePath = databaseFile.getAbsolutePath().replace("\\", "/");
        DatabaseQueue.start();
    }

    public static void stop(){
        try {
            DatabaseQueue.stop();
            if(connection != null)
                connection.close();
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public static void executeQuery(String statement, DatabaseConsumer<ResultSet> callback){
        executeQuery(statement, callback, Throwable::printStackTrace);
    }

    public static void executeQuery(String statement, DatabaseConsumer<ResultSet> callback, Consumer<Throwable> onError){
        initializeConnection();
        try(PreparedStatement preparedStatement = connection.prepareStatement(statement); ResultSet resultSet = preparedStatement.executeQuery()){
            callback.accept(resultSet);
        }catch(SQLException ex){
            onError.accept(ex);
        }
    }

    public static void executeUpdate(String statement){
        executeUpdate(statement, Throwable::printStackTrace);
    }

    public static void executeUpdate(String statement, Consumer<Throwable> onError){
        initializeConnection();
        try(PreparedStatement preparedStatement = connection.prepareStatement(statement)){
            preparedStatement.executeUpdate();
        }catch(SQLException ex){
            onError.accept(ex);
        }
    }

    public static Connection getConnection() {
        initializeConnection();
        return connection;
    }

    private static void initializeConnection(){
        try {
            long currentTime = System.currentTimeMillis() / 1000;
            if(connection == null || currentTime - lastConnectionCreation > CONNECTION_TIMEOUT) {
                if(connection != null)
                    connection.close();
                Class.forName("org.sqlite.JDBC");
                String sqlURL = "jdbc:sqlite:" + dbFilePath;
                connection = DriverManager.getConnection(sqlURL);
                lastConnectionCreation = currentTime;
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public interface DatabaseConsumer<T>{

        void accept(T t) throws SQLException;

    }


}
