package com.marssoft.skeletonlib.ui.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.marssoft.skeletonlib.core.BackStackInterface;
import com.marssoft.skeletonlib.helpers.ServiceUrlsHelper;
import com.marssoft.skeletonlib.ui.BaseActivity;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import no.innocode.skeletonlib.R;

import com.marssoft.skeletonlib.core.Constants;
import com.marssoft.skeletonlib.ui.WebActivity;

/**
 * Created by alexey on 25-Mar-16.
 */
public class WebFragment extends WebViewFragment {

    protected RelativeLayout mRlErrMessage;
    protected SwipeRefreshLayout mSwipeRefreshLayout;
    private boolean mWasError;
    private static final String TAG = WebFragment.class.getSimpleName();


    public WebFragment() {
        setArguments(new Bundle());
    }

    @SuppressLint("JavascriptInterface")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //View view = super.onCreateView(inflater, container, savedInstanceState);
        View view = super.onCreateView(inflater, container, savedInstanceState);
        mRlErrMessage = (RelativeLayout) view.findViewById(R.id.rlErrMessage);
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setOnRefreshListener(getOnRefreshListener());
        }
//        initFullScreenListener();
        mWebView.addJavascriptInterface(this, WebFragment.class.getSimpleName());

        Button ibRefresh = (Button)view.findViewById(R.id.ibRefresh);
        if (ibRefresh != null) {
            ibRefresh.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mRlErrMessage.setVisibility(View.GONE);
                    updateModel(true);
                }
            });
        }
        updateModel(true);
        return view;
    }

    @Override
    protected void onError(int errorCode, String description, String failingUrl) {
        if (mWebView.getUrl().equals(failingUrl)) {
            mWebView.loadUrl("about:blank");
            mRlErrMessage.setVisibility(View.VISIBLE);
            setWasError(true);
        }
    }

    private SwipeRefreshLayout.OnRefreshListener getOnRefreshListener() {
        return new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateModel(true);
            }
        };
    }

    @Override
    protected void onStartLinkOpen(String url) {
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    protected void onFinishLinkOpen(String url) {
    }

    @Override
    protected void onPageFinished(WebView view, String url) {
        if (!isWasError()) {
            setDataWasLoaded(true);
        }
        setCustomTitle(view.getTitle());
    }

    /*private void initFullScreenListener() {
        setToggledFullscreenCallback(new VideoEnabledWebChromeClient.ToggledFullscreenCallback() {
            @Override
            public void toggledFullscreen(boolean fullscreen) {
                ((BaseActivity) getActivity()).hideToolbar(fullscreen);
                if (fullscreen) {
//                    getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                    WindowManager.LayoutParams attrs = getActivity().getWindow().getAttributes();
                    attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
                    attrs.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
                    getActivity().getWindow().setAttributes(attrs);
                    if (android.os.Build.VERSION.SDK_INT >= 14) {
                        //noinspection all
                        getActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
                    }
                } else {
//                    getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    WindowManager.LayoutParams attrs = getActivity().getWindow().getAttributes();
                    attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
                    attrs.flags &= ~WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
                    getActivity().getWindow().setAttributes(attrs);
                    if (android.os.Build.VERSION.SDK_INT >= 14) {
                        //noinspection all
                        getActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                    }
                }
            }
        });
    }*/

    public void updateArgs(Bundle args) {
        if (args != null) {
            if (getArguments() != null) {
                getArguments().clear();
                getArguments().putAll(args);
            } else {
                setArguments(args);
            }
            updateModel(true);
        }
    }

    @Override
    public void updateModel(boolean forceUpdate) {
        if (isAdded()) {
            setCustomTitle(R.string.loading);
            mProgressBar.setVisibility(View.VISIBLE);
            String url = getArguments().getString(Constants.URL);
            mWebView.stopLoading();
            try {
                url = URLDecoder.decode(url, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            mWebView.loadUrl(url);
        }
    }

    @Override
    public void setCustomTitle(String title) {
        if (getActivity() != null
                && getActivity() instanceof BaseActivity) {
            //noinspection ConstantConditions
            ((BaseActivity) getActivity()).setCustomTitle(title);
        }
    }

    public void setCustomTitle(int titleRes) {
        if (getActivity() != null
                && getActivity() instanceof BaseActivity) {
            //noinspection ConstantConditions
            ((BaseActivity) getActivity()).setCustomTitle(titleRes);
        }
    }

    @Override
    public void onDestroyView() {
        mWebView.loadUrl("about:blank");
        super.onDestroyView();
    }

    @Override
    public void onHistoryChanged() {
        if (getActivity() != null && getActivity() instanceof BackStackInterface) {
            //((BaseActivity) getActivity()).setHomeIcon(mWebView.canGoBack() ? R.drawable.ic_arrow_back_black_24dp : R.drawable.ic_menu_black_24dp);
            ((BackStackInterface) getActivity()).onBackStackChanged();
        }
    }

    @Override
    public void onBackStackChanged() {
        onHistoryChanged();
    }

    @Override
    public boolean onTryBackPress() {
        return mWebView.onBackPressed();
    }

    @Override
    public boolean hasBackStack() {
        return mWebView.canGoBack();
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        Log.d(TAG, url);
        WebView.HitTestResult hitTestResult = mWebView.getHitTestResult();
        if (!ServiceUrlsHelper.handleServiceUrl(getContext(), url)) {
            if (hitTestResult == null
                    || hitTestResult.getType() == WebView.HitTestResult.UNKNOWN_TYPE
                    || getActivity() instanceof WebActivity) {
                setDataWasLoaded(false);
                setWasError(false);
                return false;
            } else {
                startWebActivity(url);
            }
        }
        return true;
    }

    @Override
    protected void onReceivedTitle(WebView view, String title) {
        setCustomTitle(title);
    }

    protected void startWebActivity(String url) {
        Log.i(TAG, "load url " + url);
        Intent intent = new Intent(getActivity(), WebActivity.class);
        intent.putExtra(Constants.URL, url);
        startActivity(intent);
    }


/*
    @Subscribe
    public void answerAvailable(NetworkChangeEvent event) {
        no.innocode.utils.lib.logs.Log.v(TAG, event.getTypeName());
        refreshClick();
    }
*/
    public void openUrlWithHistory(String url) {
        mWebView.loadUrlWithHistory(url);
    }

    public boolean isWasError() {
        return mWasError;
    }

    public void setWasError(boolean wasError) {
        mWasError = wasError;
    }
}
