package com.rdapps.gamepad;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Animatable2;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.rdapps.gamepad.device.JoystickType;
import com.rdapps.gamepad.model.ControllerAction;
import com.rdapps.gamepad.util.EventUtils;

import java.util.Map;
import java.util.Objects;

import static com.rdapps.gamepad.device.JoystickType.RIGHT_JOYSTICK;
import static com.rdapps.gamepad.log.JoyConLog.log;
import static com.rdapps.gamepad.util.ControllerActionUtils.AXIS_NAMES;

public class JoystickMappingActivity extends AppCompatActivity {

    public static final String TYPE = "TYPE";
    public static final String RESULT = "RESULT";
    public static final int MAPPING = 1;

    private JoystickType joystickType;
    private State state;
    private ImageView imageView;
    private TextView instructionView;
    private TextView resultView;
    private ControllerAction resultAction;

    private enum State {
        X,
        Y,
        NONE
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_joystick_mapping);
        Toolbar mainToolbar = findViewById(R.id.mainMenuToolbar);
        setSupportActionBar(mainToolbar);

        Intent intent = getIntent();
        joystickType = (JoystickType) intent.getSerializableExtra(TYPE);

        if (Objects.isNull(joystickType)) {
            finish();
        }

        imageView = findViewById(R.id.to_do_action);
        instructionView = findViewById(R.id.instruction);
        resultView = findViewById(R.id.result);

        final int instructionText = joystickType == RIGHT_JOYSTICK ?
                R.string.push_right_joystick_up :
                R.string.push_left_joystick_up;

        instructionView.setText(instructionText);
        imageView.setContentDescription(getString(instructionText));

        state = State.Y;
        resultAction = new ControllerAction(joystickType, -1, 0, -1, 0);

        AnimatedVectorDrawable avd = (AnimatedVectorDrawable) imageView.getDrawable();
        avd.start();
        avd.registerAnimationCallback(new Animatable2.AnimationCallback() {
            @Override
            public void onAnimationEnd(Drawable drawable) {
                avd.start();
            }
        });
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent motionEvent) {
        InputDevice device = motionEvent.getDevice();
        for (Map.Entry<Integer, String> axisEntry : AXIS_NAMES.entrySet()) {
            Integer axis = axisEntry.getKey();
            int maxedAxis = EventUtils.getMaxedAxis(motionEvent, device, axis);
            if (maxedAxis != 0) {
                log("AXIS_NAME", axisEntry.getValue());
                if (state == State.Y) {
                    resultView.setText(this.getString(R.string.axis_registered, "Y", axisEntry.getValue()));
                    imageView.setRotation(90);
                    instructionView.setText(
                            joystickType == RIGHT_JOYSTICK ?
                                    R.string.push_right_joystick_right :
                                    R.string.push_left_joystick_right);
                    state = State.X;
                    resultAction.setAxisY(axis);
                    resultAction.setDirectionY(maxedAxis);
                } else if (state == State.X && axis != resultAction.getAxisY()) {
                    resultView.setText(this.getString(R.string.axis_registered, "X", axisEntry.getValue()));
                    resultAction.setAxisX(axis);
                    resultAction.setDirectionX(maxedAxis);
                    new AlertDialog.Builder(this)
                            .setMessage(R.string.joystick_mapping_completed)
                            .setPositiveButton(R.string.save, this::onSave)
                            .setNegativeButton(R.string.redo, this::onCancel)
                            .setCancelable(true)
                            .setOnCancelListener(this::onCancel)
                            .create()
                            .show();
                    state = State.NONE;
                }
                break;
            }
        }
        return true;
    }

    private void onCancel(DialogInterface dialogInterface, int i) {
        this.onCancel(dialogInterface);
    }

    private void onCancel(DialogInterface dialogInterface) {
        imageView.setRotation(0);
        instructionView.setText(
                joystickType == RIGHT_JOYSTICK ?
                        R.string.push_right_joystick_up :
                        R.string.push_left_joystick_up);

        state = State.Y;
        resultView.setText("");
        resultAction = new ControllerAction(joystickType, -1, 0, -1, 0);
    }

    private void onSave(DialogInterface dialogInterface, int i) {
        Intent result = new Intent();
        result.putExtra(RESULT, resultAction);
        setResult(RESULT_OK, result);
        finish();
    }
}
