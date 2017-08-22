package com.marssoft.skeletonlib.ui.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.Map;
import java.util.Stack;

/**
 * This class serves as a WebView to be used in conjunction with a VideoEnabledWebChromeClient.
 * It makes possible:
 * - To detect the HTML5 video ended event so that the VideoEnabledWebChromeClient can exit full-screen.
 * <p>
 * Important notes:
 * - Javascript is enabled by default and must not be disabled with getSettings().setJavaScriptEnabled(false).
 * - setWebChromeClient() must be called before any loadData(), loadDataWithBaseURL() or loadUrl() method.
 *
 * @author Cristian Perez (http://cpr.name)
 */
public class VideoEnabledWebView extends WebView {


    public interface WebViewInterface {
        boolean shouldOverrideUrlLoading(WebView view, String url);

        void onPageFinished(WebView view, String url);

        void onStartLinkOpen(String url);

        void onFinishLinkOpen(String url);

        void onError(int errorCode, String description, String failingUrl);
    }

    private static final String TAG = VideoEnabledWebView.class.getSimpleName();
    private OnLoadPage mListener;
    private VideoEnabledWebChromeClient videoEnabledWebChromeClient;
    private boolean addedJavascriptInterface;
    private Stack<String> mHistory = new Stack<>();
    //    private String mLastLoadedUrl;
    private String mRequestedUrl;
    private boolean mWasTouch;
    private WebViewInterface mWebViewInterface;

    @SuppressWarnings("unused")
    public VideoEnabledWebView(Context context) {
        super(context);
        addedJavascriptInterface = false;
        initWebView();
    }

    private void initWebView() {
        setWebViewClient(new MyWebViewClient());
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                WebView.HitTestResult hitTestResult = getHitTestResult();
                if (hitTestResult != null && hitTestResult.getType() == HitTestResult.SRC_ANCHOR_TYPE &&
                        hitTestResult.getExtra().contains("loginlink")){
                    mWasTouch = false; // login link
                } else {
                    mWasTouch = true;
                }
                return false;
            }
        });
    }

    @SuppressWarnings("unused")
    public VideoEnabledWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        addedJavascriptInterface = false;
        initWebView();
    }

    @SuppressWarnings("unused")
    public VideoEnabledWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        addedJavascriptInterface = false;
        initWebView();
    }

    /**
     * Indicates if the video is being displayed using a custom view (typically full-screen)
     *
     * @return true it the video is being displayed using a custom view (typically full-screen)
     */
    @SuppressWarnings("unused")
    public boolean isVideoFullscreen() {
        return videoEnabledWebChromeClient != null && videoEnabledWebChromeClient.isVideoFullscreen();
    }

    /**
     * Pass only a VideoEnabledWebChromeClient instance.
     */
    @Override
    @SuppressLint("SetJavaScriptEnabled")
    public void setWebChromeClient(WebChromeClient client) {
        getSettings().setJavaScriptEnabled(true);

        if (client instanceof VideoEnabledWebChromeClient) {
            this.videoEnabledWebChromeClient = (VideoEnabledWebChromeClient) client;
        }

        super.setWebChromeClient(client);
    }

    @Override
    public void loadData(String data, String mimeType, String encoding) {
        addJavascriptInterface();
        super.loadData(data, mimeType, encoding);
    }

    @Override
    public void loadDataWithBaseURL(String baseUrl, String data, String mimeType, String encoding, String historyUrl) {
        addJavascriptInterface();
        super.loadDataWithBaseURL(baseUrl, data, mimeType, encoding, historyUrl);
    }

    public void addToHistory(String url) {
        if (url == null) return;
        if (mHistory.isEmpty() || !url.equals(mHistory.peek())) {
            Log.i(TAG, "add url " + url);
            mHistory.add(url);
        }
    }

    @Override
    public void loadUrl(String url) {
        if (url.startsWith("http")) {
            mRequestedUrl = url;
        }
        addJavascriptInterface();
        super.loadUrl(url);
    }

    public void loadUrlWithHistory(String url) {
        mWasTouch = true;
        addJavascriptInterface();
        super.loadUrl(url);
    }

    @Override
    public void loadUrl(String url, Map<String, String> additionalHttpHeaders) {
        if (url.startsWith("http")) {
            mRequestedUrl = url;
        }
        addJavascriptInterface();
        super.loadUrl(url, additionalHttpHeaders);
    }

    private void addJavascriptInterface() {
        if (!addedJavascriptInterface) {
            // Add javascript interface to be called when the video ends (must be done before page load)
            //noinspection all
            addJavascriptInterface(new JavascriptInterface(), "_VideoEnabledWebView"); // Must match Javascript interface name of VideoEnabledWebChromeClient

            addedJavascriptInterface = true;
        }
    }

    public boolean onBackPressed() {
        if (isVideoFullscreen()) {
            videoEnabledWebChromeClient.onBackPressed();
            return true;
        } else if (canGoBack()) {
            goBack();
            mListener.loadPage(getUrl());
            return true;
        }
        return false;
    }

    @Override
    public boolean canGoBack() {
        if (mHistory.size() > 0) {
            String url = mHistory.peek();
            return !(mHistory.size() == 1 && url.equals(getUrl()));
        } else {
            return false;
        }
    }

    public boolean canGoBackOrig() {
        return super.canGoBack();
    }

    public void goBackOrig() {
        super.goBack();
    }

    @Override
    public void goBack() {
        if (!mHistory.isEmpty()) {
            String url = mHistory.pop();
            Log.v(TAG,"history pop " + url);
            if (mWebViewInterface != null) {
                mWebViewInterface.onStartLinkOpen(url);
            }
            loadUrl(url);
        }
    }

    public OnLoadPage getListener() {
        return mListener;
    }

    public void setListener(OnLoadPage listener) {
        mListener = listener;
    }

    public interface OnLoadPage {
        void loadPage(String url);
    }

    public class JavascriptInterface {
        @android.webkit.JavascriptInterface
        @SuppressWarnings("unused")
        public void notifyVideoEnd() // Must match Javascript interface method of VideoEnabledWebChromeClient
        {
            Log.d("___", "GOT IT");
            // This code is not executed in the UI thread, so we must force that to happen
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    if (videoEnabledWebChromeClient != null) {
                        videoEnabledWebChromeClient.onHideCustomView();
                    }
                }
            });
        }
    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            Log.i(TAG, "start opening " + url);
            if (mWasTouch && url.startsWith("http")) {
                addToHistory(mRequestedUrl);
                if (mWebViewInterface != null) {
                    // any hit
                    mWebViewInterface.onStartLinkOpen(url);
                }
                mRequestedUrl = url;
                mWasTouch = false;
            }
            if (url.startsWith("http")) {
                if (mWebViewInterface != null) {
                    mWebViewInterface.onFinishLinkOpen(url);
                }
            }
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (mWebViewInterface != null) {
                return mWebViewInterface.shouldOverrideUrlLoading(view, url);
            } else {
                return false;
            }
        }


        @Override
        public void onPageFinished(WebView view, String url) {
            Log.i(TAG, "finish opening " + url);
            mWebViewInterface.onPageFinished(view, url);
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            mWebViewInterface.onError(errorCode, description, failingUrl);
            super.onReceivedError(view, errorCode, description, failingUrl);
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mWebViewInterface.onError(error.getErrorCode(), error.getDescription().toString(), request.getUrl().toString());
            }
            super.onReceivedError(view, request, error);
        }

        @Override
        public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && request.getUrl().toString().equals(mRequestedUrl)) {
                Log.w(TAG, "requested url " + mRequestedUrl + "\nerror url "  + request.getUrl() + "\nerror " + errorResponse.getReasonPhrase() + " code " + errorResponse.getStatusCode());
                mWebViewInterface.onError(errorResponse.getStatusCode(), errorResponse.getReasonPhrase(), request.getUrl().toString());
            }
            super.onReceivedHttpError(view, request, errorResponse);
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            if (error.getUrl().equals(mRequestedUrl)) {
                mWebViewInterface.onError(error.getPrimaryError(), toString(), error.getUrl());
            }
            super.onReceivedSslError(view, handler, error);
        }
    }

    public void setWebViewInterface(WebViewInterface webViewInterface) {
        mWebViewInterface = webViewInterface;
    }

    public void setToggledFullscreenCallback(VideoEnabledWebChromeClient.ToggledFullscreenCallback toggledFullscreenCallback) {
        videoEnabledWebChromeClient.setOnToggledFullscreen(toggledFullscreenCallback);
    }

    public int getHistorySize(){
        return mHistory.size();
    }
}

