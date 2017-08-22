package com.marssoft.skeletonlib.core;

/**
 * Created by alexey on 25-Mar-16.
 */
public interface BackStackInterface {
    boolean onTryBackPress();
    boolean hasBackStack();
    void onBackStackChanged();
}
