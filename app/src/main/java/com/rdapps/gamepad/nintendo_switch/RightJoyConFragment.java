package com.rdapps.gamepad.nintendo_switch;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageButton;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.erz.joysticklibrary.JoyStick;
import com.rdapps.gamepad.ControllerActivity;
import com.rdapps.gamepad.R;
import com.rdapps.gamepad.button.AxisEnum;
import com.rdapps.gamepad.button.ButtonEnum;
import com.rdapps.gamepad.led.LedState;

import java.util.Objects;

import static com.rdapps.gamepad.button.ButtonEnum.A;
import static com.rdapps.gamepad.button.ButtonEnum.B;
import static com.rdapps.gamepad.button.ButtonEnum.HOME;
import static com.rdapps.gamepad.button.ButtonEnum.PLUS;
import static com.rdapps.gamepad.button.ButtonEnum.RIGHT_SL;
import static com.rdapps.gamepad.button.ButtonEnum.RIGHT_SR;
import static com.rdapps.gamepad.button.ButtonEnum.RIGHT_STICK_BUTTON;
import static com.rdapps.gamepad.button.ButtonEnum.X;
import static com.rdapps.gamepad.button.ButtonEnum.Y;
import static com.rdapps.gamepad.button.ButtonEnum.ZR;
import static com.rdapps.gamepad.button.ButtonState.BUTTON_DOWN;
import static com.rdapps.gamepad.nintendo_switch.SwitchController.ButtonStates.DOWN;
import static com.rdapps.gamepad.nintendo_switch.SwitchController.ButtonStates.UP;
import static com.rdapps.gamepad.vibrator.VibrationPattern.BUTTON_PRESS;
import static com.rdapps.gamepad.vibrator.VibrationPattern.BUTTON_RELEASE;
import static com.rdapps.gamepad.vibrator.VibrationPattern.STICK_PRESS;
import static com.rdapps.gamepad.vibrator.VibrationPattern.STICK_RELEASE;

public class RightJoyConFragment extends ControllerFragment implements JoyStick.JoyStickListener, View.OnClickListener {

    private ImageButton sr;
    private ImageButton sl;
    private ImageButton x;
    private ImageButton y;
    private ImageButton a;
    private ImageButton b;
    private ImageButton zr;
    private ImageButton r;
    private ImageButton plus;
    private ImageButton home;
    private ImageButton sync;

    private View led1;
    private View led2;
    private View led3;
    private View led4;

    private JoyStick joyStick;

    private boolean stickPressed = false;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.right_joycon_layout, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        View.OnTouchListener buttonTouchListener = new ButtonTouchListener();
        sr = view.findViewById(R.id.sr);
        sl = view.findViewById(R.id.sl);
        a = view.findViewById(R.id.a);
        b = view.findViewById(R.id.b);
        x = view.findViewById(R.id.x);
        y = view.findViewById(R.id.y);
        r = view.findViewById(R.id.r);
        zr = view.findViewById(R.id.zr);
        plus = view.findViewById(R.id.plus);
        home = view.findViewById(R.id.home);
        joyStick = view.findViewById(R.id.joy);
        sync = view.findViewById(R.id.sync);

        led1 = view.findViewById(R.id.led1);
        led2 = view.findViewById(R.id.led2);
        led3 = view.findViewById(R.id.led3);
        led4 = view.findViewById(R.id.led4);

        sr.setOnTouchListener(buttonTouchListener);
        sl.setOnTouchListener(buttonTouchListener);
        a.setOnTouchListener(buttonTouchListener);
        b.setOnTouchListener(buttonTouchListener);
        x.setOnTouchListener(buttonTouchListener);
        y.setOnTouchListener(buttonTouchListener);
        r.setOnTouchListener(buttonTouchListener);
        zr.setOnTouchListener(buttonTouchListener);
        home.setOnTouchListener(buttonTouchListener);
        plus.setOnTouchListener(buttonTouchListener);
        joyStick.setListener(this);
        sync.setOnClickListener(this);
    }

    @Override
    public void onMove(JoyStick joyStick, double angle, double power, int direction) {
        if (Objects.isNull(device)) {
            return;
        }

        double x = power * Math.cos(angle) * -1;
        double y = power * Math.sin(angle);
        device.setAxis(AxisEnum.RIGHT_STICK_X, (int) x);
        device.setAxis(AxisEnum.RIGHT_STICK_Y, (int) y);
    }

    @Override
    public void onTap() {
        if (Objects.isNull(device)) {
            return;
        }

        setRightStickPress(!stickPressed);
        stickPressed = !stickPressed;

        vibrate(stickPressed ? STICK_PRESS : STICK_RELEASE);
    }

    @Override
    public void onDoubleTap() {

    }

    @Override
    public void onClick(View v) {
        FragmentActivity activity = getActivity();
        if (activity instanceof ControllerActivity controllerActivity) {
            controllerActivity.sync();
        }
    }


    @Override
    public boolean setLeftStickPress(boolean pressed) {
        return false;
    }

    @Override
    public boolean setRightStickPress(boolean pressed) {
        if (Objects.isNull(device)) {
            return false;
        }

        device.setButton(RIGHT_STICK_BUTTON, pressed ? DOWN : UP);
        joyStick.setPadColor(getContext().getColor(pressed ? R.color.custom_pressed : R.color.custom_brand_red));
        joyStick.invalidate();
        return true;
    }

    private class ButtonTouchListener implements View.OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (Objects.isNull(device)) {
                return false;
            }

            int action = event.getAction();
            int buttonState;
            if (action == MotionEvent.ACTION_DOWN) {
                v.setPressed(true);
                buttonState = DOWN;
            } else if (action == MotionEvent.ACTION_UP) {
                v.setPressed(false);
                buttonState = UP;
            } else {
                return false;
            }

            vibrate(buttonState == BUTTON_DOWN ? BUTTON_PRESS : BUTTON_RELEASE);

            ButtonEnum buttonEnum;
            if (v == sl) {
                buttonEnum = RIGHT_SL;
            } else if (v == sr) {
                buttonEnum = RIGHT_SR;
            } else if (v == y) {
                buttonEnum = Y;
            } else if (v == a) {
                buttonEnum = A;
            } else if (v == b) {
                buttonEnum = B;
            } else if (v == x) {
                buttonEnum = X;
            } else if (v == r) {
                buttonEnum = ButtonEnum.R;
            } else if (v == zr) {
                buttonEnum = ZR;
            } else if (v == home) {
                buttonEnum = HOME;
            } else if (v == plus) {
                buttonEnum = PLUS;
            } else {
                return false;
            }
            device.setButton(buttonEnum, buttonState);
            return true;
        }
    }

    @Override
    public ImageButton getSR() {
        return sr;
    }

    @Override
    public ImageButton getL() {
        return null;
    }

    @Override
    public ImageButton getSL() {
        return sl;
    }

    @Override
    public ImageButton getX() {
        return x;
    }

    @Override
    public ImageButton getY() {
        return y;
    }

    @Override
    public ImageButton getA() {
        return a;
    }

    @Override
    public ImageButton getB() {
        return b;
    }

    @Override
    public ImageButton getZR() {
        return zr;
    }

    @Override
    public ImageButton getMinus() {
        return null;
    }

    @Override
    public ImageButton getR() {
        return r;
    }

    @Override
    public ImageButton getZL() {
        return null;
    }

    @Override
    public ImageButton getPlus() {
        return plus;
    }

    @Override
    public ImageButton getHome() {
        return home;
    }

    @Override
    public ImageButton getCapture() {
        return null;
    }

    @Override
    public ImageButton getLeft() {
        return null;
    }

    @Override
    public ImageButton getRight() {
        return null;
    }

    @Override
    public ImageButton getUp() {
        return null;
    }

    @Override
    public ImageButton getDown() {
        return null;
    }

    @Override
    public ImageButton getSync() {
        return sync;
    }

    @Override
    public JoyStick getLeftJoyStick() {
        return null;
    }

    @Override
    public JoyStick getRightJoyStick() {
        return joyStick;
    }

    @Override
    public boolean reverseJoyStickXY() {
        return false;
    }

    @Override
    public void showAmiiboPicker() {
        openFileSelectionDialog();
    }

    @Override
    public void setPlayerLights(LedState led1, LedState led2, LedState led3, LedState led4) {
        if (Objects.nonNull(getActivity())) {
            if (led1 == LedState.ON) {
                this.led1.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.ic_ledon));
            }
            if (led2 == LedState.ON) {
                this.led2.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.ic_ledon));
            }
            if (led3 == LedState.ON) {
                this.led3.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.ic_ledon));
            }
            if (led4 == LedState.ON) {
                this.led4.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.ic_ledon));
            }
            if (led1 == LedState.OFF) {
                this.led1.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.ic_ledofftransparent));
            }
            if (led2 == LedState.OFF) {
                this.led2.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.ic_ledofftransparent));
            }
            if (led3 == LedState.OFF) {
                this.led3.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.ic_ledofftransparent));
            }
            if (led4 == LedState.OFF) {
                this.led4.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.ic_ledofftransparent));
            }
            if (led1 == LedState.BLINK) {
                Drawable drawable = ContextCompat.getDrawable(getActivity(), R.drawable.ic_ledblink);
                this.led1.setBackground(drawable);
                ((Runnable) drawable).run();
            }
            if (led2 == LedState.BLINK) {
                Drawable drawable = ContextCompat.getDrawable(getActivity(), R.drawable.ic_ledblink);
                this.led2.setBackground(drawable);
                ((Runnable) drawable).run();
            }
            if (led3 == LedState.BLINK) {
                Drawable drawable = ContextCompat.getDrawable(getActivity(), R.drawable.ic_ledblink);
                this.led3.setBackground(drawable);
                ((Runnable) drawable).run();
            }
            if (led4 == LedState.BLINK) {
                Drawable drawable = ContextCompat.getDrawable(getActivity(), R.drawable.ic_ledblink);
                this.led4.setBackground(drawable);
                ((Runnable) drawable).run();
            }
        }
    }
}
