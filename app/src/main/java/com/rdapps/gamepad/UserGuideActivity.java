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

import androidx.appcompat.app.AppCompatActivity;

import com.rdapps.gamepad.util.ControllerFunctions;
import com.rdapps.gamepad.util.PreferenceUtils;

import java.util.Locale;
import java.util.Optional;

import static com.rdapps.gamepad.log.JoyConLog.log;

public class UserGuideActivity extends AppCompatActivity {

    private static final String TAG = UserGuideActivity.class.getName();
    private static final String BASE_URL = "https://joycondroid.gitbook.io/joycondroid/";

    public static final String PATH = "PATH";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_user_guide);

        String url = Optional.ofNullable(getIntent())
                .map(intent -> intent.getStringExtra(PATH))
                .map(path -> BASE_URL + path)
                .orElse(BASE_URL);

        WebView webView = findViewById(R.id.webContent);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        Locale aDefault = Locale.getDefault();
        log("Locale", "Locale:" + aDefault.toString());
        webSettings.setMediaPlaybackRequiresUserGesture(false);
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);

        webView.loadUrl(url);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
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
                    webView.loadUrl(url);
                }
        ), "Controller");
    }

    public void closeGuide(View view) {
        PreferenceUtils.setDoNotShow(UserGuideActivity.this, true);
        finish();
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        log(TAG, "Config Changed");
    }
}
