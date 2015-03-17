package org.minezy.android.rx;

import android.webkit.WebView;

import rx.Observable;

public class WebViewObservable {
    private WebViewObservable() {
    }

    public static Observable<WebViewLoadingEvent> pageLoading(final WebView view, boolean emitInitialValue) {
        return Observable.create(new OnSubscribeWebViewFinished(view, emitInitialValue));
    }


}
