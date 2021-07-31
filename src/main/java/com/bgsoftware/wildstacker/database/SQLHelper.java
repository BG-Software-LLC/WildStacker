package com.bgsoftware.wildstacker.database;

import com.bgsoftware.wildstacker.WildStackerPlugin;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public final class SQLHelper {

    private static final CompletableFuture<Void> ready = new CompletableFuture<>();
    private static final Object mutex = new Object();

    private static Connection conn;

    private SQLHelper() {

    }

    public static void waitForConnection() {
        try {
            ready.get();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static Object getMutex() {
        return mutex;
    }

    public static void createConnection(WildStackerPlugin plugin) throws SQLException {
        File file = new File(plugin.getDataFolder(), "database.db");
        conn = DriverManager.getConnection("jdbc:sqlite:" + file.getAbsolutePath().replace("\\", "/"));

        ready.complete(null);
    }

    public static void executeUpdate(String statement) {
        executeUpdate(statement, ex -> {
            System.out.println(statement);
            ex.printStackTrace();
        });
    }

    public static void executeUpdate(String statement, Consumer<SQLException> onError) {
        if (conn == null)
            return;

        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = conn.prepareStatement(statement);
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            onError.accept(ex);
        } finally {
            close(preparedStatement);
        }
    }

    public static boolean doesConditionExist(String statement) {
        if (conn == null)
            return false;

        boolean ret = false;

        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            preparedStatement = conn.prepareStatement(statement);
            resultSet = preparedStatement.executeQuery();
            ret = resultSet.next();
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            close(resultSet);
            close(preparedStatement);
        }

        return ret;
    }

    public static void executeQuery(String statement, QueryConsumer<ResultSet> callback) {
        executeQuery(statement, callback, Throwable::printStackTrace);
    }

    public static void executeQuery(String statement, QueryConsumer<ResultSet> callback, Consumer<SQLException> onError) {
        if (conn == null)
            return;

        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            preparedStatement = conn.prepareStatement(statement);
            resultSet = preparedStatement.executeQuery();
            callback.accept(resultSet);
        } catch (SQLException ex) {
            onError.accept(ex);
        } finally {
            close(resultSet);
            close(preparedStatement);
        }
    }

    public static void close() {
        try {
            conn.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static void buildStatement(String query, QueryConsumer<PreparedStatement> consumer, Consumer<SQLException> failure) {
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = conn.prepareStatement(query);
            consumer.accept(preparedStatement);
        } catch (SQLException ex) {
            failure.accept(ex);
        } finally {
            close(preparedStatement);
        }
    }

    private static void close(AutoCloseable closeable) {
        if (closeable != null) {
            try {
                if (!(closeable instanceof Connection))
                    closeable.close();
            } catch (Exception ignored) {
            }
        }
    }

    public static void setAutoCommit(boolean autoCommit) {
        try {
            conn.setAutoCommit(autoCommit);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static void commit() throws SQLException {
        conn.commit();
    }

    public interface QueryConsumer<T> {

        void accept(T value) throws SQLException;

    }

}

