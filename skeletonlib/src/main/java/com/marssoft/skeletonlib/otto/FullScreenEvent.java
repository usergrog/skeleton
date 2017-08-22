package com.marssoft.skeletonlib.otto;

import android.widget.FrameLayout;

/**
 * Created by Alexey Sidorenko on 05-Jul-16.
 */
public class FullScreenEvent {
    private final boolean mFullScreen;
    private final FrameLayout mVideoViewContainer;

    public FullScreenEvent(boolean fullScreen, FrameLayout videoViewContainer) {
        mFullScreen = fullScreen;
        mVideoViewContainer = videoViewContainer;
    }

    public boolean isFullScreen() {
        return mFullScreen;
    }

    public FrameLayout getVideoViewContainer() {
        return mVideoViewContainer;
    }
}
