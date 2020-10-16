package com.bgsoftware.wildstacker.database;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public final class DatabaseQueue {

    private static final ScheduledExecutorService queueService = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder().setNameFormat("WildStacker Database Thread").build());
    private static final long QUEUE_INTERVAL = 60;

    private static final Map<Integer, QueryParameters> queuedCalls = new ConcurrentHashMap<>();
    private static final Map<Query, Map<Object, Integer>> alreadyObjectsCalled = new ConcurrentHashMap<>();
    private static final AtomicInteger currentIndex = new AtomicInteger(0);

    public static void queue(Object caller, QueryParameters parameters){
        Map<Object, Integer> queryCalls = alreadyObjectsCalled.computeIfAbsent(parameters.getQuery(), q -> new ConcurrentHashMap<>());
        Integer existingParametersIndex = queryCalls.get(caller);

        if(existingParametersIndex != null)
            queuedCalls.remove(existingParametersIndex);

        int currentIndex = DatabaseQueue.currentIndex.getAndIncrement();

        queuedCalls.put(currentIndex, parameters);
        queryCalls.put(caller, currentIndex);
    }

    static void start() {
        queueService.scheduleAtFixedRate(DatabaseQueue::processQueue, QUEUE_INTERVAL, QUEUE_INTERVAL, TimeUnit.SECONDS);
    }

    static void stop(){
        // Stopping the queue timer, and calling the process queue manually
        queueService.shutdownNow();
        processQueue();
    }

    private static void processQueue(){
        int currentIndex = DatabaseQueue.currentIndex.getAndSet(0);
        if(currentIndex > 0) {
            Map<Query, PreparedStatement> preparedStatementMap = new EnumMap<>(Query.class);
            Connection connection = Database.getConnection();

            for(int i = 0; i < currentIndex; i++){
                try {
                    QueryParameters parameters = queuedCalls.get(i);

                    if (parameters == null)
                        continue;

                    PreparedStatement preparedStatement = preparedStatementMap.computeIfAbsent(parameters.getQuery(), q -> {
                        try {
                            return connection.prepareStatement(q.getStatement());
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            return null;
                        }
                    });

                    if (preparedStatement != null) {
                        try {
                            if (preparedStatement.isClosed()) {
                                Query query = parameters.getQuery();
                                preparedStatement = connection.prepareStatement(query.getStatement());
                                preparedStatementMap.put(query, preparedStatement);
                            }
                        }catch (Exception ignored){}

                        parameters.executeQuery(preparedStatement);
                        preparedStatement.executeUpdate();
                        preparedStatement.clearParameters();
                    }
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }

            preparedStatementMap.values().forEach(preparedStatement -> {
                try{
                    preparedStatement.close();
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            });
        }
    }

}
