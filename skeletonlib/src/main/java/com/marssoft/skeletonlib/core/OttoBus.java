package com.marssoft.skeletonlib.core;

import android.os.Handler;
import android.os.Looper;

import com.squareup.otto.Bus;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alexey Sidorenko on 05-Jul-16.
 */
public class OttoBus extends Bus {

    private static OttoBus instance;
    private List mRegisteredObjects;

    public static OttoBus getInstance(){
        if (instance == null){
            instance = new OttoBus();
        }
        return instance;
    }

    public OttoBus() {
        mRegisteredObjects = new ArrayList();
    }

    private final Handler mHandler = new Handler(Looper.getMainLooper());

    @Override
    public void post(final Object event) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            super.post(event);
        } else {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    OttoBus.super.post(event);
                }
            });
        }
    }

    @Override
    public void register(final Object object) {
        if (mRegisteredObjects.contains(object)) return;
        mRegisteredObjects.add(object);
        if (Looper.myLooper() == Looper.getMainLooper()) {
            super.register(object);
        } else {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    OttoBus.super.register(object);
                }
            });
        }
    }

    @Override
    public void unregister(final Object object) {
        if (!mRegisteredObjects.contains(object)) return;
        mRegisteredObjects.remove(object);
        if (Looper.myLooper() == Looper.getMainLooper()) {
            super.unregister(object);
        } else {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    OttoBus.super.unregister(object);
                }
            });
        }
    }
}

