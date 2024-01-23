package com.rdapps.gamepad;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.rdapps.gamepad.util.ControllerFunctions;
import com.rdapps.gamepad.util.PreferenceUtils;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.rdapps.gamepad.log.JoyConLog.log;

public class InfoAndLegalActivity extends AppCompatActivity {

    public static final int ACCEPT = 1;
    public static final int REJECT = 2;

    private static final String TAG = InfoAndLegalActivity.class.getName();

    private AtomicBoolean errored = new AtomicBoolean(false);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_info_and_legal);
        Button acceptButton = findViewById(R.id.accept_button);
        acceptButton.setEnabled(false);

        WebView webView = findViewById(R.id.webContent);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        Locale aDefault = Locale.getDefault();
        log("Locale", "Locale:" + aDefault.toString());
        webSettings.setMediaPlaybackRequiresUserGesture(false);
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);

        webView.loadUrl("https://youtubeplays.github.io/JoyConDroidJS/TutorialUI/?page=legal");
        webView.setWebViewClient(new WebViewClient() {
            public void onPageFinished(WebView view, String url) {
                if (!errored.get()) {
                    acceptButton.setEnabled(true);
                }
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                errored.set(true);
                webView.loadUrl("file:///android_asset/error.html");
            }
        });
        webView.setWebChromeClient(new WebChromeClient() {

            @Override
            public boolean onConsoleMessage(ConsoleMessage cm) {
                log("CONTENT", String.format("%s @ %d: %s",
                        cm.message(), cm.lineNumber(), cm.sourceId()));
                return true;
            }
        });

        webView.addJavascriptInterface(new ControllerFunctions(
                webView,
                () -> {
                    webView.loadUrl("https://youtubeplays.github.io/JoyConDroidJS/TutorialUI/?page=legal");
                }
        ), "Controller");
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        log(TAG, "Config Changed");
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void setAccepted(View view) {
        PreferenceUtils.setLegalAccepted(this, true);
        setResult(ACCEPT);
        finish();
    }
}
