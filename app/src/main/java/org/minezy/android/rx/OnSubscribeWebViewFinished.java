package org.minezy.android.rx;

import android.graphics.Bitmap;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.AndroidSubscriptions;
import rx.android.internal.Assertions;
import rx.functions.Action0;

final class OnSubscribeWebViewFinished implements Observable.OnSubscribe<WebViewLoadingEvent> {
    private final boolean emitInitialValue;
    private final WebView view;

    public OnSubscribeWebViewFinished(WebView view, boolean emitInitialValue) {
        this.view = view;
        this.emitInitialValue = emitInitialValue;
    }

    @Override
    public void call(final Subscriber<? super WebViewLoadingEvent> observer) {
        Assertions.assertUiThread();
        final CompositeWebViewListener composite = CachedListeners.getFromViewOrCreate(view);

        final Observer<WebViewLoadingEvent> listener = new Observer<WebViewLoadingEvent>() {
            @Override
            public void onNext(WebViewLoadingEvent webViewLoadingEvent) {
                observer.onNext(webViewLoadingEvent);
            }

            @Override
            public void onCompleted() {}

            @Override
            public void onError(Throwable e) {}
        };

        final Subscription subscription = AndroidSubscriptions.unsubscribeInUiThread(new Action0() {
            @Override
            public void call() {
                composite.remove(listener);
            }
        });

        if (emitInitialValue) {
            observer.onNext(composite.getLast());
        }

        composite.add(listener);
        observer.add(subscription);
    }

    private static class CompositeWebViewListener extends WebViewClient {
        CompositeWebViewListener(WebView webView) {
            last = WebViewLoadingEvent.create(WebViewLoadingEvent.Type.UNKNOWN, webView, webView.getUrl());
        }

        private final List<Observer<WebViewLoadingEvent>> listeners = new ArrayList<Observer<WebViewLoadingEvent>>();
        private WebViewLoadingEvent last;

        public WebViewLoadingEvent getLast() {
            return last;
        }

        public boolean add(Observer<WebViewLoadingEvent> listener) {
            return listeners.add(listener);
        }

        public boolean remove(Observer<WebViewLoadingEvent> listener) {
            return listeners.remove(listener);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            last = WebViewLoadingEvent.create(WebViewLoadingEvent.Type.STARTED, view, url);
            callListeners(last);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            last = WebViewLoadingEvent.create(WebViewLoadingEvent.Type.FINISHED, view, url);
            callListeners(last);
        }

        private void callListeners( WebViewLoadingEvent event) {
            for (Observer<WebViewLoadingEvent> listener : listeners) {
                listener.onNext(event);
            }
        }
    }

    private static class CachedListeners {
        private static final Map<WebView, CompositeWebViewListener> sCachedListeners = new WeakHashMap<WebView, CompositeWebViewListener>();

        public static CompositeWebViewListener getFromViewOrCreate(WebView webView) {
            final CompositeWebViewListener cached = sCachedListeners.get(webView);

            if (cached != null) {
                return cached;
            }

            final CompositeWebViewListener listener = new CompositeWebViewListener(webView);

            sCachedListeners.put(webView, listener);
            webView.setWebViewClient(listener);

            return listener;
        }
    }
}
