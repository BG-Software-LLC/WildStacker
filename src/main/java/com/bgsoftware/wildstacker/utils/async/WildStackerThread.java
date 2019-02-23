package com.bgsoftware.wildstacker.utils.async;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public final class WildStackerThread {

    private static final Executor executor = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat("WildStacker Thread").build());

    private Runnable runnable;

    public WildStackerThread(Runnable runnable){
        this.runnable = runnable;
    }

    public void start(){
        executor.execute(runnable);
    }

}
