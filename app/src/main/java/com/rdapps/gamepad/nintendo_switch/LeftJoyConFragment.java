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

import static com.rdapps.gamepad.button.ButtonEnum.LEFT;
import static com.rdapps.gamepad.button.ButtonEnum.LEFT_SL;
import static com.rdapps.gamepad.button.ButtonEnum.LEFT_SR;
import static com.rdapps.gamepad.button.ButtonEnum.LEFT_STICK_BUTTON;
import static com.rdapps.gamepad.button.ButtonEnum.MINUS;
import static com.rdapps.gamepad.button.ButtonEnum.RIGHT;
import static com.rdapps.gamepad.button.ButtonState.BUTTON_DOWN;
import static com.rdapps.gamepad.nintendo_switch.SwitchController.ButtonStates.UP;
import static com.rdapps.gamepad.vibrator.VibrationPattern.BUTTON_PRESS;
import static com.rdapps.gamepad.vibrator.VibrationPattern.BUTTON_RELEASE;
import static com.rdapps.gamepad.vibrator.VibrationPattern.STICK_PRESS;
import static com.rdapps.gamepad.vibrator.VibrationPattern.STICK_RELEASE;

public class LeftJoyConFragment extends ControllerFragment implements JoyStick.JoyStickListener, View.OnClickListener {

    private Button sr;
    private Button sl;
    private Button up;
    private Button down;
    private Button left;
    private Button right;
    private Button zl;
    private Button l;
    private Button minus;
    private Button capture;
    private Button sync;

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
        return inflater.inflate(R.layout.left_joycon_layout, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButtonTouchListener buttonTouchListener = new ButtonTouchListener();
        sr = view.findViewById(R.id.sr);
        sl = view.findViewById(R.id.sl);
        up = view.findViewById(R.id.up);
        down = view.findViewById(R.id.down);
        left = view.findViewById(R.id.left);
        right = view.findViewById(R.id.right);
        l = view.findViewById(R.id.l);
        zl = view.findViewById(R.id.zl);
        minus = view.findViewById(R.id.minus);
        capture = view.findViewById(R.id.capture);
        joyStick = view.findViewById(R.id.joy);
        sync = view.findViewById(R.id.sync);

        led1 = view.findViewById(R.id.led1);
        led2 = view.findViewById(R.id.led2);
        led3 = view.findViewById(R.id.led3);
        led4 = view.findViewById(R.id.led4);

        sr.setOnTouchListener(buttonTouchListener);
        sl.setOnTouchListener(buttonTouchListener);
        up.setOnTouchListener(buttonTouchListener);
        down.setOnTouchListener(buttonTouchListener);
        left.setOnTouchListener(buttonTouchListener);
        right.setOnTouchListener(buttonTouchListener);
        l.setOnTouchListener(buttonTouchListener);
        zl.setOnTouchListener(buttonTouchListener);
        capture.setOnTouchListener(buttonTouchListener);
        minus.setOnTouchListener(buttonTouchListener);
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
        device.setAxis(AxisEnum.LEFT_STICK_X, (int) x);
        device.setAxis(AxisEnum.LEFT_STICK_Y, (int) y);
    }

    @Override
    public void onTap() {
        if (Objects.isNull(device)) {
            return;
        }

        setLeftStickPress(!stickPressed);
        stickPressed = !stickPressed;

        vibrate(stickPressed ? STICK_PRESS : STICK_RELEASE);
    }

    @Override
    public boolean setLeftStickPress(boolean pressed) {
        if (Objects.isNull(device)) {
            return false;
        }

        device.setButton(LEFT_STICK_BUTTON, pressed ? SwitchController.ButtonStates.DOWN : UP);
        joyStick.setPadColor(getContext().getColor(pressed ? R.color.pressed : R.color.dark_blue));
        joyStick.invalidate();
        return true;
    }

    @Override
    public boolean setRightStickPress(boolean pressed) {
        return false;
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
                buttonState = SwitchController.ButtonStates.DOWN;
            } else if (action == MotionEvent.ACTION_UP) {
                v.setPressed(false);
                buttonState = UP;
            } else {
                return false;
            }

            vibrate(buttonState == BUTTON_DOWN ? BUTTON_PRESS : BUTTON_RELEASE);

            ButtonEnum buttonEnum;
            if (v == sl) {
                buttonEnum = LEFT_SL;
            } else if (v == sr) {
                buttonEnum = LEFT_SR;
            } else if (v == left) {
                buttonEnum = LEFT;
            } else if (v == right) {
                buttonEnum = RIGHT;
            } else if (v == down) {
                buttonEnum = ButtonEnum.DOWN;
            } else if (v == up) {
                buttonEnum = ButtonEnum.UP;
            } else if (v == l) {
                buttonEnum = ButtonEnum.L;
            } else if (v == zl) {
                buttonEnum = ButtonEnum.ZL;
            } else if (v == capture) {
                buttonEnum = ButtonEnum.CAPTURE;
            } else if (v == minus) {
                buttonEnum = MINUS;
            } else {
                return false;
            }
            device.setButton(buttonEnum, buttonState);
            return true;
        }
    }

    @Override
    public Button getSR() {
        return sr;
    }

    @Override
    public Button getSL() {
        return sl;
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
    public Button getZR() {
        return null;
    }

    @Override
    public Button getL() {
        return l;
    }

    @Override
    public Button getR() {
        return null;
    }

    @Override
    public Button getMinus() {
        return minus;
    }

    @Override
    public Button getPlus() {
        return null;
    }

    @Override
    public Button getHome() {
        return null;
    }

    @Override
    public Button getCapture() {
        return capture;
    }

    @Override
    public Button getSync() {
        return sync;
    }

    @Override
    public JoyStick getLeftJoyStick() {
        return joyStick;
    }

    @Override
    public JoyStick getRightJoyStick() {
        return null;
    }

    @Override
    public Button getA() {
        return null;
    }

    @Override
    public Button getB() {
        return null;
    }

    @Override
    public Button getX() {
        return null;
    }

    @Override
    public Button getY() {
        return null;
    }

    @Override
    public boolean reverseJoyStickXY() {
        return false;
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
