package com.rdapps.gamepad.nintendoswitch;

import static android.app.Activity.RESULT_OK;
import static com.rdapps.gamepad.ControllerActivity.CUSTOM_UI_URL;
import static com.rdapps.gamepad.button.ButtonState.BUTTON_DOWN;
import static com.rdapps.gamepad.button.ButtonState.BUTTON_UP;
import static com.rdapps.gamepad.log.JoyConLog.log;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageButton;
import androidx.fragment.app.FragmentActivity;
import com.erz.joysticklibrary.JoyStick;
import com.rdapps.gamepad.ControllerActivity;
import com.rdapps.gamepad.R;
import com.rdapps.gamepad.button.AxisEnum;
import com.rdapps.gamepad.button.ButtonEnum;
import com.rdapps.gamepad.led.LedState;
import com.rdapps.gamepad.util.ControllerFunctions;
import com.rdapps.gamepad.web.CachingWebViewClient;
import java.io.Serializable;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.Phaser;

public class CustomFragment extends ControllerFragment {
    private static final String LOG_TAG = CustomFragment.class.getName();

    private String url;

    private WebView webView;

    private ValueCallback<Uri[]> uploadMessage;
    private Uri[] results;

    public static CustomFragment getInstance(Serializable url) {
        CustomFragment customFragment = new CustomFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(CUSTOM_UI_URL, url);
        customFragment.setArguments(bundle);
        return customFragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.custom_joycon_layout, parent, false);
    }

    @Override
    @SuppressLint("SetJavaScriptEnabled")
    public void onViewCreated(View view, Bundle savedInstanceState) {
        this.url = getArguments().getString(CUSTOM_UI_URL);
        super.onViewCreated(view, savedInstanceState);
        webView = view.findViewById(R.id.webContent);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setMediaPlaybackRequiresUserGesture(false);
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);

        webView.loadUrl(url);
        webView.setWebViewClient(new CachingWebViewClient());
        webView.setWebChromeClient(new WebChromeClient() {

            @Override
            public boolean onConsoleMessage(ConsoleMessage cm) {
                log("CONTENT", String.format(Locale.ROOT, "%s @ %d: %s",
                        cm.message(), cm.lineNumber(), cm.sourceId()));
                return true;
            }

            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback,
                                             WebChromeClient.FileChooserParams fileChooserParams) {
                // Double check that we don't have any existing callbacks
                if (uploadMessage != null) {
                    uploadMessage.onReceiveValue(null);
                }
                uploadMessage = filePathCallback;

                openFileSelectionDialog(false);

                return true;
            }
        });

        webView.addJavascriptInterface(new JoyConJsInterface(), "joyconJS");
        webView.addJavascriptInterface(new ControllerFunctions(
                webView,
                () -> webView.loadUrl(url)
        ), "Controller");
    }

    private class JoyConJsInterface {
        private final Phaser phaser = new Phaser(1);

        @JavascriptInterface
        public void onUp(boolean pressed) {
            onButton(ButtonEnum.UP, pressed);
        }

        @JavascriptInterface
        public void onDown(boolean pressed) {
            onButton(ButtonEnum.DOWN, pressed);
        }

        @JavascriptInterface
        public void onLeft(boolean pressed) {
            onButton(ButtonEnum.LEFT, pressed);
        }

        @JavascriptInterface
        public void onRight(boolean pressed) {
            onButton(ButtonEnum.RIGHT, pressed);
        }

        @JavascriptInterface
        public void onLeftSl(boolean pressed) {
            onButton(ButtonEnum.LEFT_SL, pressed);
        }

        @JavascriptInterface
        public void onLeftSr(boolean pressed) {
            onButton(ButtonEnum.LEFT_SR, pressed);
        }

        @JavascriptInterface
        public void onCapture(boolean pressed) {
            onButton(ButtonEnum.CAPTURE, pressed);
        }

        @JavascriptInterface
        public void onMinus(boolean pressed) {
            onButton(ButtonEnum.MINUS, pressed);
        }

        @JavascriptInterface
        public void onSync(boolean pressed) {
            if (pressed) {
                return;
            }

            FragmentActivity activity = getActivity();
            if (activity instanceof ControllerActivity controllerActivity) {
                controllerActivity.sync();
            }
        }

        @JavascriptInterface
        public void onL(boolean pressed) {
            onButton(ButtonEnum.L, pressed);
        }

        @JavascriptInterface
        public void onZl(boolean pressed) {
            onButton(ButtonEnum.ZL, pressed);
        }

        @JavascriptInterface
        public void onLeftJoystick(float power, float angle) {
            if (Objects.isNull(device)) {
                return;
            }

            double x = power * Math.cos(angle);
            double y = power * Math.sin(angle);

            device.setAxis(AxisEnum.LEFT_STICK_X, (int) x);
            device.setAxis(AxisEnum.LEFT_STICK_Y, (int) y);
        }

        @JavascriptInterface
        public void onLeftJoystickPressed(boolean pressed) {
            if (Objects.isNull(device)) {
                return;
            }

            onButton(ButtonEnum.LEFT_STICK_BUTTON, pressed);
        }

        @JavascriptInterface
        public void onB(boolean pressed) {
            onButton(ButtonEnum.B, pressed);
        }

        @JavascriptInterface
        public void onA(boolean pressed) {
            onButton(ButtonEnum.A, pressed);
        }

        @JavascriptInterface
        public void onX(boolean pressed) {
            onButton(ButtonEnum.X, pressed);
        }

        @JavascriptInterface
        public void onY(boolean pressed) {
            onButton(ButtonEnum.Y, pressed);
        }

        @JavascriptInterface
        public void onRightSl(boolean pressed) {
            onButton(ButtonEnum.RIGHT_SL, pressed);
        }

        @JavascriptInterface
        public void onRightSr(boolean pressed) {
            onButton(ButtonEnum.RIGHT_SR, pressed);
        }

        @JavascriptInterface
        public void onHome(boolean pressed) {
            onButton(ButtonEnum.HOME, pressed);
        }

        @JavascriptInterface
        public void onPlus(boolean pressed) {
            onButton(ButtonEnum.PLUS, pressed);
        }

        @JavascriptInterface
        public void onRightJoystick(float power, float angle) {
            if (Objects.isNull(device)) {
                return;
            }

            double x = power * Math.cos(angle);
            double y = power * Math.sin(angle);

            device.setAxis(AxisEnum.RIGHT_STICK_X, (int) x);
            device.setAxis(AxisEnum.RIGHT_STICK_Y, (int) y);
        }

        @JavascriptInterface
        public void onRightJoystickPressed(boolean pressed) {
            if (Objects.isNull(device)) {
                return;
            }

            onButton(ButtonEnum.RIGHT_STICK_BUTTON, pressed);
        }

        @JavascriptInterface
        public void onR(boolean pressed) {
            onButton(ButtonEnum.R, pressed);
        }

        @JavascriptInterface
        public void onZr(boolean pressed) {
            onButton(ButtonEnum.ZR, pressed);
        }


        @JavascriptInterface
        public boolean registerCallback(String callbackFunction) {
            if (Objects.nonNull(device)) {
                device.setCallbackFunction(() -> {
                    log(LOG_TAG, "Notify Callback");
                    phaser.register();
                    getActivity().runOnUiThread(() -> webView.evaluateJavascript(
                            callbackFunction + "();",
                            (value) -> {
                                log(LOG_TAG, "Before Script Completed");
                                phaser.arriveAndDeregister();
                                log(LOG_TAG, "Script Completed");
                            }));
                    log(LOG_TAG, "Waiting Script Completed");
                    phaser.arriveAndAwaitAdvance();
                    log(LOG_TAG, "Callback done");
                });
                return true;
            } else {
                return false;
            }
        }

        @JavascriptInterface
        public void unregisterCallback() {
            if (Objects.nonNull(device)) {
                device.setCallbackFunction(null);
            }
        }

        @JavascriptInterface
        public void unblockCallback() {
            phaser.arriveAndDeregister();
        }

        @JavascriptInterface
        public void setAccelerometerEnabled(boolean enabled) {
            if (Objects.nonNull(device) && device.isAccelerometerEnabled() != enabled) {
                device.setAccelerometerEnabled(enabled);
                if (enabled) {
                    registerAccelerometerListener();
                } else {
                    unregisterAccelerometerListener();
                }
            }
        }

        @JavascriptInterface
        public void setGyroscopeEnabled(boolean enabled) {
            if (Objects.nonNull(device) && device.isGyroscopeEnabled() != enabled) {
                device.setGyroscopeEnabled(enabled);
                if (enabled) {
                    registerGyroscopeListener();
                } else {
                    unregisterGyroscopeListener();
                }
            }
        }

        @JavascriptInterface
        public void setMotionControlsEnabled(boolean enabled) {
            setAccelerometerEnabled(enabled);
            setGyroscopeEnabled(enabled);
        }

        @JavascriptInterface
        public boolean isAccelerometerEnabled() {
            return device.isAccelerometerEnabled();
        }

        @JavascriptInterface
        public boolean isGyroscopeEnabled() {
            return device.isGyroscopeEnabled();
        }

        @JavascriptInterface
        public boolean isMotionControlsEnabled() {
            return device.isMotionControlsEnabled();
        }
    }

    public void onButton(ButtonEnum buttonEnum, boolean pressed) {
        if (Objects.isNull(device)) {
            return;
        }

        int buttonState;
        if (pressed) {
            buttonState = BUTTON_DOWN;
        } else {
            buttonState = BUTTON_UP;
        }

        device.setButton(buttonEnum, buttonState);
    }

    @Override
    public boolean setLeftStickPress(boolean pressed) {
        return true;
    }

    @Override
    public boolean setRightStickPress(boolean pressed) {
        return false;
    }

    @Override
    public ImageButton getImageButtonSr() {
        return null;
    }

    @Override
    public ImageButton getImageButtonSl() {
        return null;
    }

    @Override
    public ImageButton getImageButtonUp() {
        return null;
    }

    @Override
    public ImageButton getImageButtonDown() {
        return null;
    }

    @Override
    public ImageButton getImageButtonLeft() {
        return null;
    }

    @Override
    public ImageButton getImageButtonRight() {
        return null;
    }

    @Override
    public ImageButton getImageButtonZl() {
        return null;
    }

    @Override
    public ImageButton getImageButtonZr() {
        return null;
    }

    @Override
    public ImageButton getImageButtonL() {
        return null;
    }

    @Override
    public ImageButton getImageButtonR() {
        return null;
    }

    @Override
    public ImageButton getImageButtonMinus() {
        return null;
    }

    @Override
    public ImageButton getImageButtonPlus() {
        return null;
    }

    @Override
    public ImageButton getImageButtonHome() {
        return null;
    }

    @Override
    public ImageButton getImageButtonCapture() {
        return null;
    }

    @Override
    public ImageButton getImageButtonSync() {
        return null;
    }

    @Override
    public JoyStick getLeftJoyStick() {
        return null;
    }

    @Override
    public JoyStick getRightJoyStick() {
        return null;
    }

    @Override
    public ImageButton getImageButtonA() {
        return null;
    }

    @Override
    public ImageButton getImageButtonB() {
        return null;
    }

    @Override
    public ImageButton getImageButtonX() {
        return null;
    }

    @Override
    public ImageButton getImageButtonY() {
        return null;
    }

    @Override
    public boolean reverseJoystickXy() {
        return false;
    }

    @Override
    protected void onFileSelected(Intent data) {
        Uri[] results = new Uri[1];
        if (data != null) {
            Context context = getContext();
            results[0] = data.getData();
            log(LOG_TAG, "file uri: " + results[0]);
        }
        uploadMessage.onReceiveValue(results);
        uploadMessage = null;
    }

    @Override
    public void setPlayerLights(LedState led1, LedState led2, LedState led3, LedState led4) {

    }
}
