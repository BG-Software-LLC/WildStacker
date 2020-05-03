package com.bgsoftware.wildstacker.hooks;

import com.bgsoftware.wildstacker.utils.ServerVersion;

public final class PaperHook {

    private static final boolean asyncChunkSupport = checkAsyncChunkSupport();

    public static boolean hasAsyncChunkSupport(){
        return asyncChunkSupport;
    }

    private static boolean checkAsyncChunkSupport(){
        try{
            Class.forName("com.destroystokyo.paper.VersionHistoryManager$VersionData");
            return ServerVersion.isAtLeast(ServerVersion.v1_13);
        }catch(Exception ignored){
            return false;
        }
    }

}
