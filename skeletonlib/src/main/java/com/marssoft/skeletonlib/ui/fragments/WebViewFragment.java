package com.marssoft.skeletonlib.ui.fragments;

import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.ConsoleMessage;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ProgressBar;

import com.marssoft.skeletonlib.ui.BaseActivity;
import com.marssoft.skeletonlib.ui.views.VideoEnabledWebChromeClient;
import com.marssoft.skeletonlib.ui.views.VideoEnabledWebView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import no.innocode.skeletonlib.R;
import com.marssoft.skeletonlib.SkeletonConfig;

/**
 * Created by alexey on 25-Mar-16.
 */
public abstract class WebViewFragment extends BaseFragment {
    private static final String TAG = WebViewFragment.class.getSimpleName();
    protected VideoEnabledWebView mWebView;
    protected ProgressBar mProgressBar;
    protected Map<String, String> mExtraHeaders = new HashMap<String, String>(1);
    private MyVideoEnabledWebChromeClient webChromeClient;
    private boolean mCanRotate;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        mExtraHeaders.put(SkeletonConfig.getInstance().getAppName(), "yes");// fixme get app name from configuration
        View view = inflateLayout(inflater, container);
        // Save the web view
        mWebView = (VideoEnabledWebView) view.findViewById(R.id.webView);

        mWebView.setWebViewInterface(new VideoEnabledWebView.WebViewInterface() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return WebViewFragment.this.shouldOverrideUrlLoading(view, url);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                WebViewFragment.this.onPageFinished(view, url);
            }

            @Override
            public void onStartLinkOpen(String url) {
                WebViewFragment.this.onStartLinkOpen(url);
            }

            @Override
            public void onFinishLinkOpen(String url) {
                WebViewFragment.this.onFinishLinkOpen(url);
            }

            @Override
            public void onError(int errorCode, String description, String failingUrl) {
                if (!TextUtils.isEmpty(failingUrl) &&
                        !TextUtils.isEmpty(mWebView.getUrl()) &&
                        mWebView.getUrl().equals(failingUrl)) {
                    WebViewFragment.this.onError(errorCode, description, failingUrl);
                }
            }
        });

        setWebViewSettings();
        mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        // Initialize the VideoEnabledWebChromeClient and set event handlers
        View nonVideoLayout = view.findViewById(R.id.nonVideoLayout); // Your own view, read class comments
        ViewGroup videoLayout = (ViewGroup) view.findViewById(R.id.videoLayout); // Your own view, read class comments
        //noinspection all
        View loadingView = inflater.inflate(R.layout.view_loading_video, null); // Your own view, read class comments
        webChromeClient = new MyVideoEnabledWebChromeClient(nonVideoLayout, videoLayout, loadingView, mWebView); // See all available constructors...


        mWebView.setWebChromeClient(webChromeClient);
        initFullScreenListener();
        webChromeClient.setProgressCallback(new VideoEnabledWebChromeClient.ProgressCallback() {
            @Override
            public void onProgress(int progress) {
                if (progress > 0 && progress < 100) {
                    mProgressBar.setVisibility(View.VISIBLE);
                    mProgressBar.setProgress(progress);
                } else {
                    mProgressBar.setVisibility(View.GONE);
                }
            }
        });

        return view;
    }

    protected View inflateLayout(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.web_fragment, container, false);
    }

    private void initFullScreenListener() {
        setToggledFullscreenCallback(new VideoEnabledWebChromeClient.ToggledFullscreenCallback() {
            @Override
            public void toggledFullscreen(boolean fullscreen) {
                // Your code to handle the full-screen change, for example showing and hiding the title bar. Example:
                if (fullscreen) {
                    WindowManager.LayoutParams attrs = getActivity().getWindow().getAttributes();
                    attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
                    attrs.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
                    getActivity().getWindow().setAttributes(attrs);
                    if (mCanRotate) {
                        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                    }
                    if (getActivity() instanceof BaseActivity) {
                        ((BaseActivity) getActivity()).hideToolbar(true);
                    }
                    if (android.os.Build.VERSION.SDK_INT >= 14) {
                        //noinspection all
                        getActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
                    }
                } else {
                    WindowManager.LayoutParams attrs = getActivity().getWindow().getAttributes();
                    attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
                    attrs.flags &= ~WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
                    getActivity().getWindow().setAttributes(attrs);
                    if (mCanRotate) {
                        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    }
                    if (getActivity() instanceof BaseActivity) {
                        ((BaseActivity) getActivity()).hideToolbar(false);
                    }
                    if (android.os.Build.VERSION.SDK_INT >= 14) {
                        //noinspection all
                        getActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                    }
                }
            }
        });
    }

    protected abstract void onError(int errorCode, String description, String failingUrl);

    protected abstract void onStartLinkOpen(String url);

    protected abstract void onFinishLinkOpen(String url);

    protected abstract void onPageFinished(WebView view, String url);

    private void setWebViewSettings() {
        // Configure the webview
        WebSettings s = mWebView.getSettings();
        s.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        s.setUseWideViewPort(true);
        s.setLoadWithOverviewMode(true);
        s.setJavaScriptCanOpenWindowsAutomatically(true);
        s.setSavePassword(true);
        s.setSaveFormData(true);
        s.setJavaScriptEnabled(true);
        CookieManager.getInstance().setAcceptCookie(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(mWebView, true);
            s.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            s.setMediaPlaybackRequiresUserGesture(false);
        }

        // enable navigator.geolocation
        s.setGeolocationEnabled(true);
        s.setGeolocationDatabasePath("/data/data/org.itri.html5webview/databases/");

        // enable Web Storage: localStorage, sessionStorage
        s.setDomStorageEnabled(true);

        mWebView.setListener(new VideoEnabledWebView.OnLoadPage() {
            @Override
            public void loadPage(String url) {
                setCustomTitle(mWebView.getTitle());
            }
        });
    }

    public abstract void setCustomTitle(String title);

    public void setCanRotate(boolean canRotate) {
        mCanRotate = canRotate;
    }

    public abstract void onHistoryChanged();

    public abstract boolean shouldOverrideUrlLoading(WebView view, String url);

    private class MyVideoEnabledWebChromeClient extends VideoEnabledWebChromeClient {
        public MyVideoEnabledWebChromeClient() {
        }

        public MyVideoEnabledWebChromeClient(View activityNonVideoView, ViewGroup activityVideoView) {
            super(activityNonVideoView, activityVideoView);
        }

        public MyVideoEnabledWebChromeClient(View activityNonVideoView, ViewGroup activityVideoView, View loadingView) {
            super(activityNonVideoView, activityVideoView, loadingView);
        }

        public MyVideoEnabledWebChromeClient(View activityNonVideoView, ViewGroup activityVideoView, View loadingView, VideoEnabledWebView webView) {
            super(activityNonVideoView, activityVideoView, loadingView, webView);
        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
            WebViewFragment.this.onReceivedTitle(view, title);
        }

        @Override
        public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
            Log.d(TAG, consoleMessage.message() + " -- From line "
                    + consoleMessage.lineNumber() + " of "
                    + consoleMessage.sourceId());
            return super.onConsoleMessage(consoleMessage);
        }

        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {
            super.onShowCustomView(view, callback);
        }

        @Override
        public void onShowCustomView(View view, int requestedOrientation, CustomViewCallback callback) {
            super.onShowCustomView(view, requestedOrientation, callback);
        }

        @Override
        public void onHideCustomView() {
            super.onHideCustomView();
        }
    }

    public VideoEnabledWebView getWebView() {
        return mWebView;
    }

    public void setToggledFullscreenCallback(VideoEnabledWebChromeClient.ToggledFullscreenCallback toggledFullscreenCallback) {
        mWebView.setToggledFullscreenCallback(toggledFullscreenCallback);
    }

    protected abstract void onReceivedTitle(WebView view, String title);

    public void runJsFromAssets(String jsName) {
        String jsScript = "javascript:try{ " +
                readJsFromAssetsHtml(jsName) +
                "}catch(error){console.log(error.message);}";
        Log.v(TAG, jsScript);
        mWebView.loadUrl(jsScript);
    }

    private String readJsFromAssetsHtml(String jsName) {
        if (getActivity() == null) return "";
        StringBuilder out = new StringBuilder();
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(getActivity().getAssets().open(jsName)));
            String str;
            while ((str = in.readLine()) != null) {
                out.append(str).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return out.toString();
    }

}
