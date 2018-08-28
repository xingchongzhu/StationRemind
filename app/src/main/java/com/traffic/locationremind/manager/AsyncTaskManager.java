package com.traffic.locationremind.manager;

import com.traffic.locationremind.baidu.location.utils.SearchPath;

import java.util.ArrayList;
import java.util.List;

public class AsyncTaskManager {
    public static AsyncTaskManager mAsyncTaskManager;
    private List<SearchPath> geekRunnableMap = new ArrayList<>();

    public void addGeekRunnable(SearchPath geekRunnable){
        geekRunnableMap.add(geekRunnable);
    }

    public void removeGeekRunnable(SearchPath geekRunnable){
        geekRunnableMap.remove(geekRunnable);
    }

    public void stopAllGeekRunable(){
        for(SearchPath geekRunnable:geekRunnableMap){
            geekRunnable.setStopRunState(true);
        }
        geekRunnableMap.clear();
    }

    public boolean isSearch(){
        boolean isSearch = false;
        for(SearchPath geekRunnable:geekRunnableMap){
            if(!geekRunnable.getRunState()){
                isSearch = true;
            }
        }
        return isSearch;
    }

    public static AsyncTaskManager getInstance(){
        if(mAsyncTaskManager == null){
            mAsyncTaskManager = new AsyncTaskManager();
        }
        return mAsyncTaskManager;
    }
}
