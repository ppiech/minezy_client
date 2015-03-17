package org.minezy.android.rx;

import android.webkit.WebView;

public class WebViewLoadingEvent
{
    public enum Type {
        STARTED,
        FINISHED,
        UNKNOWN
    }

    private Type mType;
    private WebView mWebView;
    private String mUrl;

    private WebViewLoadingEvent(Type type, WebView webView, String url) {
        mType = type;
        mWebView = webView;
        mUrl = url;
    }

    public Type type() {
        return mType;
    }

    public WebView webView() {
        return mWebView;
    }

    public String url() {
        return mUrl;
    }

    public static WebViewLoadingEvent create(Type type, WebView webView, String url) {
        return new WebViewLoadingEvent(type, webView, url);
    }
}
