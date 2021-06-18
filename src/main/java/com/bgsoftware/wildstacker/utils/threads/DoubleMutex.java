package com.bgsoftware.wildstacker.utils.threads;

import com.bgsoftware.wildstacker.objects.WAsyncStackedObject;

public final class DoubleMutex {

    public static void hold(WAsyncStackedObject<?> stackedObject, WAsyncStackedObject<?>  otherStackedObject,
                            Runnable runnable){
        int firstId = stackedObject.getId(), secondId = otherStackedObject.getId();
        WAsyncStackedObject<?> firstEntity = firstId > secondId ? stackedObject : otherStackedObject;
        WAsyncStackedObject<?> secondEntity = firstId > secondId ? otherStackedObject : stackedObject;
        if(firstId > secondId){
            synchronized (firstEntity.getMutex()){
                synchronized (secondEntity.getMutex()){
                    runnable.run();
                }
            }
        }
    }

}
