package com.rdapps.gamepad;

import static com.rdapps.gamepad.log.JoyConLog.log;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import androidx.appcompat.app.AppCompatActivity;
import com.rdapps.gamepad.util.ControllerFunctions;
import com.rdapps.gamepad.util.PreferenceUtils;
import com.rdapps.gamepad.web.CachingWebViewClient;
import java.util.Locale;
import java.util.Optional;

public class UserGuideActivity extends AppCompatActivity {

    private static final String TAG = UserGuideActivity.class.getName();
    private static final String BASE_URL = "https://joycondroid.gitbook.io/joycondroid/";

    public static final String PATH = "PATH";

    @Override
    @SuppressLint("SetJavaScriptEnabled")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_user_guide);

        WebView webView = findViewById(R.id.webContent);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        Locale defaultLocale = Locale.getDefault();
        log("Locale", "Locale:" + defaultLocale);
        webSettings.setMediaPlaybackRequiresUserGesture(false);
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);

        String url = Optional.ofNullable(getIntent())
                .map(intent -> intent.getStringExtra(PATH))
                .map(path -> BASE_URL + path)
                .orElse(BASE_URL);

        webView.loadUrl(url);
        webView.setWebViewClient(new CachingWebViewClient());
        webView.setWebChromeClient(new WebChromeClient() {

            @Override
            public boolean onConsoleMessage(ConsoleMessage cm) {
                log("CONTENT", String.format(Locale.ROOT, "%s @ %d: %s",
                        cm.message(), cm.lineNumber(), cm.sourceId()));
                return true;
            }
        });


        webView.addJavascriptInterface(new ControllerFunctions(
                webView,
                () -> webView.loadUrl(url)
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
