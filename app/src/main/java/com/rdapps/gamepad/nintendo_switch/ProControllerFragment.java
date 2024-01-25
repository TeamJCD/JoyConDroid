package com.rdapps.gamepad.nintendo_switch;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

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
import static com.rdapps.gamepad.button.ButtonEnum.CAPTURE;
import static com.rdapps.gamepad.button.ButtonEnum.DOWN;
import static com.rdapps.gamepad.button.ButtonEnum.HOME;
import static com.rdapps.gamepad.button.ButtonEnum.L;
import static com.rdapps.gamepad.button.ButtonEnum.LEFT;
import static com.rdapps.gamepad.button.ButtonEnum.LEFT_STICK_BUTTON;
import static com.rdapps.gamepad.button.ButtonEnum.MINUS;
import static com.rdapps.gamepad.button.ButtonEnum.PLUS;
import static com.rdapps.gamepad.button.ButtonEnum.RIGHT;
import static com.rdapps.gamepad.button.ButtonEnum.UP;
import static com.rdapps.gamepad.button.ButtonEnum.X;
import static com.rdapps.gamepad.button.ButtonEnum.Y;
import static com.rdapps.gamepad.button.ButtonEnum.ZL;
import static com.rdapps.gamepad.button.ButtonEnum.ZR;
import static com.rdapps.gamepad.button.ButtonState.BUTTON_DOWN;
import static com.rdapps.gamepad.button.ButtonState.BUTTON_UP;
import static com.rdapps.gamepad.vibrator.VibrationPattern.BUTTON_PRESS;
import static com.rdapps.gamepad.vibrator.VibrationPattern.BUTTON_RELEASE;
import static com.rdapps.gamepad.vibrator.VibrationPattern.STICK_PRESS;
import static com.rdapps.gamepad.vibrator.VibrationPattern.STICK_RELEASE;

public class ProControllerFragment extends ControllerFragment implements View.OnClickListener {

    private Button up;
    private Button down;
    private Button left;
    private Button right;
    private Button zl;
    private Button l;
    private Button minus;
    private Button capture;
    private Button x;
    private Button y;
    private Button a;
    private Button b;
    private Button zr;
    private Button r;
    private Button plus;
    private Button home;
    private Button sync;

    private JoyStick leftJoyStick;
    private JoyStick rightJoyStick;

    private View led1;
    private View led2;
    private View led3;
    private View led4;

    private boolean rightStickPressed = false;
    private boolean leftStickPressed = false;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.pro_controller_layout, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        View.OnTouchListener buttonTouchListener = new ButtonTouchListener();
        up = view.findViewById(R.id.up);
        down = view.findViewById(R.id.down);
        left = view.findViewById(R.id.left);
        right = view.findViewById(R.id.right);
        l = view.findViewById(R.id.l);
        zl = view.findViewById(R.id.zl);
        minus = view.findViewById(R.id.minus);
        capture = view.findViewById(R.id.capture);
        leftJoyStick = view.findViewById(R.id.left_joy);
        a = view.findViewById(R.id.a);
        b = view.findViewById(R.id.b);
        x = view.findViewById(R.id.x);
        y = view.findViewById(R.id.y);
        r = view.findViewById(R.id.r);
        zr = view.findViewById(R.id.zr);
        plus = view.findViewById(R.id.plus);
        home = view.findViewById(R.id.home);
        rightJoyStick = view.findViewById(R.id.right_joy);
        sync = view.findViewById(R.id.sync);

        led1 = view.findViewById(R.id.led1);
        led2 = view.findViewById(R.id.led2);
        led3 = view.findViewById(R.id.led3);
        led4 = view.findViewById(R.id.led4);

        up.setOnTouchListener(buttonTouchListener);
        down.setOnTouchListener(buttonTouchListener);
        left.setOnTouchListener(buttonTouchListener);
        right.setOnTouchListener(buttonTouchListener);
        l.setOnTouchListener(buttonTouchListener);
        zl.setOnTouchListener(buttonTouchListener);
        capture.setOnTouchListener(buttonTouchListener);
        minus.setOnTouchListener(buttonTouchListener);
        leftJoyStick.setListener(new LeftStickListener());
        a.setOnTouchListener(buttonTouchListener);
        b.setOnTouchListener(buttonTouchListener);
        x.setOnTouchListener(buttonTouchListener);
        y.setOnTouchListener(buttonTouchListener);
        r.setOnTouchListener(buttonTouchListener);
        zr.setOnTouchListener(buttonTouchListener);
        home.setOnTouchListener(buttonTouchListener);
        plus.setOnTouchListener(buttonTouchListener);
        rightJoyStick.setListener(new RightStickListener());
        sync.setOnClickListener(this);
    }

    private class LeftStickListener implements JoyStick.JoyStickListener {
        @Override
        public void onMove(JoyStick joyStick, double angle, double power, int direction) {
            if (Objects.isNull(device)) {
                return;
            }

            double x = power * Math.cos(angle) * -1;
            double y = power * Math.sin(angle) * -1;

            device.setAxis(AxisEnum.LEFT_STICK_X, (int) y);
            device.setAxis(AxisEnum.LEFT_STICK_Y, (int) x);
        }

        @Override
        public void onTap() {
            if (Objects.isNull(device)) {
                return;
            }

            setLeftStickPress(!leftStickPressed);
            leftStickPressed = !leftStickPressed;

            vibrate(leftStickPressed ? STICK_PRESS : STICK_RELEASE);
        }

        @Override
        public void onDoubleTap() {

        }
    }

    @Override
    public boolean setLeftStickPress(boolean pressed) {
        if (Objects.isNull(device)) {
            return false;
        }

        device.setButton(LEFT_STICK_BUTTON, pressed ? BUTTON_DOWN : BUTTON_UP);
        leftJoyStick.setPadColor(getContext().getColor(pressed ? R.color.pressed : R.color.dark_grey));
        leftJoyStick.invalidate();
        return true;
    }

    private class RightStickListener implements JoyStick.JoyStickListener {
        @Override
        public void onMove(JoyStick joyStick, double angle, double power, int direction) {
            if (Objects.isNull(device)) {
                return;
            }

            double x = power * Math.cos(angle) * -1;
            double y = power * Math.sin(angle) * -1;

            device.setAxis(AxisEnum.RIGHT_STICK_X, (int) y);
            device.setAxis(AxisEnum.RIGHT_STICK_Y, (int) x);
        }

        @Override
        public void onTap() {
            if (Objects.isNull(device)) {
                return;
            }

            setRightStickPress(!rightStickPressed);
            rightStickPressed = !rightStickPressed;

            vibrate(rightStickPressed ? STICK_PRESS : STICK_RELEASE);
        }

        @Override
        public void onDoubleTap() {

        }
    }

    @Override
    public boolean setRightStickPress(boolean pressed) {
        if (Objects.isNull(device)) {
            return false;
        }

        device.setButton(ButtonEnum.RIGHT_STICK_BUTTON, pressed ? BUTTON_DOWN : BUTTON_UP);
        rightJoyStick.setPadColor(getContext().getColor(pressed ? R.color.pressed : R.color.dark_grey));
        rightJoyStick.invalidate();
        return true;
    }

    @Override
    public void onClick(View v) {
        FragmentActivity activity = getActivity();
        if (activity instanceof ControllerActivity controllerActivity) {
            controllerActivity.sync();
        }
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
                buttonState = BUTTON_DOWN;
            } else if (action == MotionEvent.ACTION_UP) {
                v.setPressed(false);
                buttonState = BUTTON_UP;
            } else {
                return false;
            }

            vibrate(buttonState == BUTTON_DOWN ? BUTTON_PRESS : BUTTON_RELEASE);

            ButtonEnum buttonEnum;
            if (v == left) {
                buttonEnum = LEFT;
            } else if (v == right) {
                buttonEnum = RIGHT;
            } else if (v == down) {
                buttonEnum = DOWN;
            } else if (v == up) {
                buttonEnum = UP;
            } else if (v == l) {
                buttonEnum = L;
            } else if (v == zl) {
                buttonEnum = ZL;
            } else if (v == capture) {
                buttonEnum = CAPTURE;
            } else if (v == minus) {
                buttonEnum = MINUS;
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
    public Button getUp() {
        return up;
    }

    @Override
    public Button getDown() {
        return down;
    }

    @Override
    public Button getLeft() {
        return left;
    }

    @Override
    public Button getRight() {
        return right;
    }

    @Override
    public Button getZL() {
        return zl;
    }

    @Override
    public Button getL() {
        return l;
    }

    @Override
    public Button getMinus() {
        return minus;
    }

    @Override
    public Button getCapture() {
        return capture;
    }

    @Override
    public Button getX() {
        return x;
    }

    @Override
    public Button getY() {
        return y;
    }

    @Override
    public Button getSL() {
        return null;
    }

    @Override
    public Button getSR() {
        return null;
    }

    @Override
    public Button getA() {
        return a;
    }

    @Override
    public Button getB() {
        return b;
    }

    @Override
    public Button getZR() {
        return zr;
    }

    @Override
    public Button getR() {
        return r;
    }

    @Override
    public Button getPlus() {
        return plus;
    }

    @Override
    public Button getHome() {
        return home;
    }

    @Override
    public Button getSync() {
        return sync;
    }

    @Override
    public JoyStick getLeftJoyStick() {
        return leftJoyStick;
    }

    @Override
    public JoyStick getRightJoyStick() {
        return rightJoyStick;
    }

    @Override
    public boolean reverseJoyStickXY() {
        return true;
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
