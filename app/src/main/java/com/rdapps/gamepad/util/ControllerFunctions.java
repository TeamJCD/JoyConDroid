package com.rdapps.gamepad.util;

import android.webkit.JavascriptInterface;
import android.webkit.WebView;

public class ControllerFunctions {
    private Runnable runnable;
    private WebView webView;

    public ControllerFunctions(WebView webView, Runnable runnable) {
        this.runnable = runnable;
        this.webView = webView;
    }

    @JavascriptInterface
    public void retry() {
        webView.post(runnable);
    }
}
