package com.rdapps.gamepad.nintendoswitch;

import static com.rdapps.gamepad.button.ButtonEnum.LEFT;
import static com.rdapps.gamepad.button.ButtonEnum.LEFT_SL;
import static com.rdapps.gamepad.button.ButtonEnum.LEFT_SR;
import static com.rdapps.gamepad.button.ButtonEnum.LEFT_STICK_BUTTON;
import static com.rdapps.gamepad.button.ButtonEnum.MINUS;
import static com.rdapps.gamepad.button.ButtonEnum.RIGHT;
import static com.rdapps.gamepad.button.ButtonState.BUTTON_DOWN;
import static com.rdapps.gamepad.nintendoswitch.SwitchController.ButtonStates.UP;
import static com.rdapps.gamepad.vibrator.VibrationPattern.BUTTON_PRESS;
import static com.rdapps.gamepad.vibrator.VibrationPattern.BUTTON_RELEASE;
import static com.rdapps.gamepad.vibrator.VibrationPattern.STICK_PRESS;
import static com.rdapps.gamepad.vibrator.VibrationPattern.STICK_RELEASE;

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

public class LeftJoyConFragment extends ControllerFragment
        implements JoyStick.JoyStickListener, View.OnClickListener {

    private ImageButton imageButtonSr;
    private ImageButton imageButtonSl;
    private ImageButton imageButtonUp;
    private ImageButton imageButtonDown;
    private ImageButton imageButtonLeft;
    private ImageButton imageButtonRight;
    private ImageButton imageButtonZl;
    private ImageButton imageButtonL;
    private ImageButton imageButtonMinus;
    private ImageButton imageButtonCapture;
    private ImageButton imageButtonSync;

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
        imageButtonSr = view.findViewById(R.id.sr);
        imageButtonSl = view.findViewById(R.id.sl);
        imageButtonUp = view.findViewById(R.id.up);
        imageButtonDown = view.findViewById(R.id.down);
        imageButtonLeft = view.findViewById(R.id.left);
        imageButtonRight = view.findViewById(R.id.right);
        imageButtonL = view.findViewById(R.id.l);
        imageButtonZl = view.findViewById(R.id.zl);
        imageButtonMinus = view.findViewById(R.id.minus);
        imageButtonCapture = view.findViewById(R.id.capture);
        joyStick = view.findViewById(R.id.joy);
        imageButtonSync = view.findViewById(R.id.sync);

        led1 = view.findViewById(R.id.led1);
        led2 = view.findViewById(R.id.led2);
        led3 = view.findViewById(R.id.led3);
        led4 = view.findViewById(R.id.led4);

        View.OnTouchListener buttonTouchListener = new ButtonTouchListener();
        imageButtonSr.setOnTouchListener(buttonTouchListener);
        imageButtonSl.setOnTouchListener(buttonTouchListener);
        imageButtonUp.setOnTouchListener(buttonTouchListener);
        imageButtonDown.setOnTouchListener(buttonTouchListener);
        imageButtonLeft.setOnTouchListener(buttonTouchListener);
        imageButtonRight.setOnTouchListener(buttonTouchListener);
        imageButtonL.setOnTouchListener(buttonTouchListener);
        imageButtonZl.setOnTouchListener(buttonTouchListener);
        imageButtonCapture.setOnTouchListener(buttonTouchListener);
        imageButtonMinus.setOnTouchListener(buttonTouchListener);
        joyStick.setListener(this);
        imageButtonSync.setOnClickListener(this);
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
        joyStick.setPadColor(getContext().getColor(pressed
                ? R.color.custom_pressed : R.color.custom_brand_blue));
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
            if (v == imageButtonSl) {
                buttonEnum = LEFT_SL;
            } else if (v == imageButtonSr) {
                buttonEnum = LEFT_SR;
            } else if (v == imageButtonLeft) {
                buttonEnum = LEFT;
            } else if (v == imageButtonRight) {
                buttonEnum = RIGHT;
            } else if (v == imageButtonDown) {
                buttonEnum = ButtonEnum.DOWN;
            } else if (v == imageButtonUp) {
                buttonEnum = ButtonEnum.UP;
            } else if (v == imageButtonL) {
                buttonEnum = ButtonEnum.L;
            } else if (v == imageButtonZl) {
                buttonEnum = ButtonEnum.ZL;
            } else if (v == imageButtonCapture) {
                buttonEnum = ButtonEnum.CAPTURE;
            } else if (v == imageButtonMinus) {
                buttonEnum = MINUS;
            } else {
                return false;
            }
            device.setButton(buttonEnum, buttonState);
            return true;
        }
    }

    @Override
    public ImageButton getImageButtonSr() {
        return imageButtonSr;
    }

    @Override
    public ImageButton getImageButtonSl() {
        return imageButtonSl;
    }

    @Override
    public ImageButton getImageButtonUp() {
        return imageButtonUp;
    }

    @Override
    public ImageButton getImageButtonDown() {
        return imageButtonDown;
    }

    @Override
    public ImageButton getImageButtonLeft() {
        return imageButtonLeft;
    }

    @Override
    public ImageButton getImageButtonRight() {
        return imageButtonRight;
    }

    @Override
    public ImageButton getImageButtonZl() {
        return imageButtonZl;
    }

    @Override
    public ImageButton getImageButtonZr() {
        return null;
    }

    @Override
    public ImageButton getImageButtonL() {
        return imageButtonL;
    }

    @Override
    public ImageButton getImageButtonR() {
        return null;
    }

    @Override
    public ImageButton getImageButtonMinus() {
        return imageButtonMinus;
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
        return imageButtonCapture;
    }

    @Override
    public ImageButton getImageButtonSync() {
        return imageButtonSync;
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
    public void setPlayerLights(LedState led1, LedState led2, LedState led3, LedState led4) {
        if (Objects.nonNull(getActivity())) {
            if (led1 == LedState.ON) {
                this.led1.setBackground(
                        ContextCompat.getDrawable(getActivity(), R.drawable.ic_ledon));
            }
            if (led2 == LedState.ON) {
                this.led2.setBackground(
                        ContextCompat.getDrawable(getActivity(), R.drawable.ic_ledon));
            }
            if (led3 == LedState.ON) {
                this.led3.setBackground(
                        ContextCompat.getDrawable(getActivity(), R.drawable.ic_ledon));
            }
            if (led4 == LedState.ON) {
                this.led4.setBackground(
                        ContextCompat.getDrawable(getActivity(), R.drawable.ic_ledon));
            }
            if (led1 == LedState.OFF) {
                this.led1.setBackground(
                        ContextCompat.getDrawable(getActivity(), R.drawable.ic_ledofftransparent));
            }
            if (led2 == LedState.OFF) {
                this.led2.setBackground(
                        ContextCompat.getDrawable(getActivity(), R.drawable.ic_ledofftransparent));
            }
            if (led3 == LedState.OFF) {
                this.led3.setBackground(
                        ContextCompat.getDrawable(getActivity(), R.drawable.ic_ledofftransparent));
            }
            if (led4 == LedState.OFF) {
                this.led4.setBackground(
                        ContextCompat.getDrawable(getActivity(), R.drawable.ic_ledofftransparent));
            }
            if (led1 == LedState.BLINK) {
                Drawable drawable =
                        ContextCompat.getDrawable(getActivity(), R.drawable.ic_ledblink);
                this.led1.setBackground(drawable);
                ((Runnable) drawable).run();
            }
            if (led2 == LedState.BLINK) {
                Drawable drawable =
                        ContextCompat.getDrawable(getActivity(), R.drawable.ic_ledblink);
                this.led2.setBackground(drawable);
                ((Runnable) drawable).run();
            }
            if (led3 == LedState.BLINK) {
                Drawable drawable =
                        ContextCompat.getDrawable(getActivity(), R.drawable.ic_ledblink);
                this.led3.setBackground(drawable);
                ((Runnable) drawable).run();
            }
            if (led4 == LedState.BLINK) {
                Drawable drawable =
                        ContextCompat.getDrawable(getActivity(), R.drawable.ic_ledblink);
                this.led4.setBackground(drawable);
                ((Runnable) drawable).run();
            }
        }
    }
}
