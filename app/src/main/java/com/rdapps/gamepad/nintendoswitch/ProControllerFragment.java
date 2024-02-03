package com.rdapps.gamepad.nintendoswitch;

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

public class ProControllerFragment extends ControllerFragment implements View.OnClickListener {

    private ImageButton imageButtonUp;
    private ImageButton imageButtonDown;
    private ImageButton imageButtonLeft;
    private ImageButton imageButtonRight;
    private ImageButton imageButtonZl;
    private ImageButton imageButtonL;
    private ImageButton imageButtonMinus;
    private ImageButton imageButtonCapture;
    private ImageButton imageButtonX;
    private ImageButton imageButtonY;
    private ImageButton imageButtonA;
    private ImageButton imageButtonB;
    private ImageButton imageButtonZr;
    private ImageButton imageButtonR;
    private ImageButton imageButtonPlus;
    private ImageButton imageButtonHome;
    private ImageButton imageButtonSync;

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
        imageButtonUp = view.findViewById(R.id.up);
        imageButtonDown = view.findViewById(R.id.down);
        imageButtonLeft = view.findViewById(R.id.left);
        imageButtonRight = view.findViewById(R.id.right);
        imageButtonL = view.findViewById(R.id.l);
        imageButtonZl = view.findViewById(R.id.zl);
        imageButtonMinus = view.findViewById(R.id.minus);
        imageButtonCapture = view.findViewById(R.id.capture);
        leftJoyStick = view.findViewById(R.id.left_joy);
        imageButtonA = view.findViewById(R.id.a);
        imageButtonB = view.findViewById(R.id.b);
        imageButtonX = view.findViewById(R.id.x);
        imageButtonY = view.findViewById(R.id.y);
        imageButtonR = view.findViewById(R.id.r);
        imageButtonZr = view.findViewById(R.id.zr);
        imageButtonPlus = view.findViewById(R.id.plus);
        imageButtonHome = view.findViewById(R.id.home);
        rightJoyStick = view.findViewById(R.id.right_joy);
        imageButtonSync = view.findViewById(R.id.sync);

        led1 = view.findViewById(R.id.led1);
        led2 = view.findViewById(R.id.led2);
        led3 = view.findViewById(R.id.led3);
        led4 = view.findViewById(R.id.led4);

        View.OnTouchListener buttonTouchListener = new ButtonTouchListener();
        imageButtonUp.setOnTouchListener(buttonTouchListener);
        imageButtonDown.setOnTouchListener(buttonTouchListener);
        imageButtonLeft.setOnTouchListener(buttonTouchListener);
        imageButtonRight.setOnTouchListener(buttonTouchListener);
        imageButtonL.setOnTouchListener(buttonTouchListener);
        imageButtonZl.setOnTouchListener(buttonTouchListener);
        imageButtonCapture.setOnTouchListener(buttonTouchListener);
        imageButtonMinus.setOnTouchListener(buttonTouchListener);
        leftJoyStick.setListener(new LeftStickListener());
        imageButtonA.setOnTouchListener(buttonTouchListener);
        imageButtonB.setOnTouchListener(buttonTouchListener);
        imageButtonX.setOnTouchListener(buttonTouchListener);
        imageButtonY.setOnTouchListener(buttonTouchListener);
        imageButtonR.setOnTouchListener(buttonTouchListener);
        imageButtonZr.setOnTouchListener(buttonTouchListener);
        imageButtonHome.setOnTouchListener(buttonTouchListener);
        imageButtonPlus.setOnTouchListener(buttonTouchListener);
        rightJoyStick.setListener(new RightStickListener());
        imageButtonSync.setOnClickListener(this);
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
        leftJoyStick.setPadColor(getContext().getColor(pressed
                ? R.color.custom_pressed : R.color.custom_brand_blue));
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
        rightJoyStick.setPadColor(getContext().getColor(pressed
                ? R.color.custom_pressed : R.color.custom_brand_red));
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
            if (v == imageButtonLeft) {
                buttonEnum = LEFT;
            } else if (v == imageButtonRight) {
                buttonEnum = RIGHT;
            } else if (v == imageButtonDown) {
                buttonEnum = DOWN;
            } else if (v == imageButtonUp) {
                buttonEnum = UP;
            } else if (v == imageButtonL) {
                buttonEnum = L;
            } else if (v == imageButtonZl) {
                buttonEnum = ZL;
            } else if (v == imageButtonCapture) {
                buttonEnum = CAPTURE;
            } else if (v == imageButtonMinus) {
                buttonEnum = MINUS;
            } else if (v == imageButtonY) {
                buttonEnum = Y;
            } else if (v == imageButtonA) {
                buttonEnum = A;
            } else if (v == imageButtonB) {
                buttonEnum = B;
            } else if (v == imageButtonX) {
                buttonEnum = X;
            } else if (v == imageButtonR) {
                buttonEnum = ButtonEnum.R;
            } else if (v == imageButtonZr) {
                buttonEnum = ZR;
            } else if (v == imageButtonHome) {
                buttonEnum = HOME;
            } else if (v == imageButtonPlus) {
                buttonEnum = PLUS;
            } else {
                return false;
            }
            device.setButton(buttonEnum, buttonState);
            return true;
        }
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
    public ImageButton getImageButtonL() {
        return imageButtonL;
    }

    @Override
    public ImageButton getImageButtonMinus() {
        return imageButtonMinus;
    }

    @Override
    public ImageButton getImageButtonCapture() {
        return imageButtonCapture;
    }

    @Override
    public ImageButton getImageButtonX() {
        return imageButtonX;
    }

    @Override
    public ImageButton getImageButtonY() {
        return imageButtonY;
    }

    @Override
    public ImageButton getImageButtonSl() {
        return null;
    }

    @Override
    public ImageButton getImageButtonSr() {
        return null;
    }

    @Override
    public ImageButton getImageButtonA() {
        return imageButtonA;
    }

    @Override
    public ImageButton getImageButtonB() {
        return imageButtonB;
    }

    @Override
    public ImageButton getImageButtonZr() {
        return imageButtonZr;
    }

    @Override
    public ImageButton getImageButtonR() {
        return imageButtonR;
    }

    @Override
    public ImageButton getImageButtonPlus() {
        return imageButtonPlus;
    }

    @Override
    public ImageButton getImageButtonHome() {
        return imageButtonHome;
    }

    @Override
    public ImageButton getImageButtonSync() {
        return imageButtonSync;
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
    public boolean reverseJoystickXy() {
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
