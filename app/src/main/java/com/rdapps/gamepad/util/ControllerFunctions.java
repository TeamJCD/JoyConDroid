package com.rdapps.gamepad.util;

import android.webkit.JavascriptInterface;
import android.webkit.WebView;

public class ControllerFunctions {
    private final Runnable runnable;
    private final WebView webView;

    public ControllerFunctions(WebView webView, Runnable runnable) {
        this.runnable = runnable;
        this.webView = webView;
    }

    @JavascriptInterface
    public void retry() {
        webView.post(runnable);
    }
}
