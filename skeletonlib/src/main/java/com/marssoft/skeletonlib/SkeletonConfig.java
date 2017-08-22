package com.marssoft.skeletonlib;

/**
 * Created by alexey on 25-Mar-16.
 */
public class SkeletonConfig {
    private static SkeletonConfig instance;
    private String mAppName;

    private SkeletonConfig() {
        mAppName = "Skeleton";// set name in host application
    }

    public static SkeletonConfig getInstance(){
        if (instance == null){
            instance = new SkeletonConfig();
        }
        return instance;
    }

    public String getAppName() {
        return mAppName;
    }

    public void setAppName(String appName) {
        mAppName = appName;
    }
}
