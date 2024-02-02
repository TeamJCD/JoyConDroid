package com.rdapps.gamepad;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.ButtonMappingAlertDialog;
import androidx.appcompat.widget.Toolbar;

import com.rdapps.gamepad.device.ButtonType;
import com.rdapps.gamepad.device.JoystickType;
import com.rdapps.gamepad.listview.ButtonMappingViewAdapter;
import com.rdapps.gamepad.model.ControllerAction;
import com.rdapps.gamepad.util.ControllerActionUtils;
import com.rdapps.gamepad.util.EventUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.rdapps.gamepad.log.JoyConLog.log;
import static com.rdapps.gamepad.model.ControllerAction.Type.AXIS;
import static com.rdapps.gamepad.util.ControllerActionUtils.AXIS_NAMES;
import static com.rdapps.gamepad.util.ControllerActionUtils.BUTTON_NAMES;
import static com.rdapps.gamepad.util.ControllerActionUtils.CONTROLLER_ACTIONS;
import static com.rdapps.gamepad.util.ControllerActionUtils.getControllerActions;

public class ButtonMappingActivity extends AppCompatActivity implements AdapterView.OnItemClickListener,
        ButtonMappingAlertDialog.DialogEventListener {

    private ButtonMappingViewAdapter adapter;
    private ButtonMappingAlertDialog alertDialog;
    private ButtonType buttonType;

    private int keyValue;
    private int axisValue;
    private int axisDirection;

    private List<ControllerAction> controllerActions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_button_mapping);
        Toolbar mainToolbar = findViewById(R.id.mainMenuToolbar);
        setSupportActionBar(mainToolbar);

        controllerActions = getControllerActions(this);

        ListView listView = findViewById(R.id.button_mappings);
        adapter = new ButtonMappingViewAdapter(this, controllerActions);
        listView.setAdapter(adapter);
        listView.setClickable(true);
        listView.setOnItemClickListener(this);
        keyValue = -1;
        axisValue = -1;
        axisDirection = 0;

        Button saveButton = findViewById(R.id.save_button);
        saveButton.setText(R.string.save_button);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mapping_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_reset) {
            //ControllerActionUtils.setControllerActions(this, null);
            controllerActions = CONTROLLER_ACTIONS;
            Optional.ofNullable(adapter).ifPresent(adapter -> adapter.refresh(controllerActions));
            return true;
        }

        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ButtonMappingViewAdapter adapter = (ButtonMappingViewAdapter) parent.getAdapter();
        ControllerAction controllerAction = adapter.getItem(position);

        if (Objects.nonNull(controllerAction.getButton())) {
            buttonType = controllerAction.getButton();
            alertDialog = new ButtonMappingAlertDialog.Builder(this)
                    .setCancelable(true)
                    .setMessage(this.getString(R.string.press_a_button_to_remap, buttonType.name()))
                    .setOnCancelListener(this::alertCancel)
                    .create();
            alertDialog.setEventListener(this);
            alertDialog.show();
        } else {
            JoystickType joystick = controllerAction.getJoystick();
            Intent intent = new Intent(this, JoystickMappingActivity.class);
            intent.putExtra(JoystickMappingActivity.TYPE, joystick);
            startActivityForResult(intent, JoystickMappingActivity.MAPPING);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == JoystickMappingActivity.MAPPING
                && resultCode == JoystickMappingActivity.RESULT_OK) {
            Optional<ControllerAction> controllerAction = Optional.ofNullable(data)
                    .map(d -> (ControllerAction) d.getSerializableExtra(JoystickMappingActivity.RESULT));
            if (controllerAction.isPresent()) {
                remapJoystick(controllerAction.get());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void remapJoystick(ControllerAction controllerAction) {
        List<ControllerAction> newActions = new ArrayList<>(controllerActions);
        for (ControllerAction ca : controllerActions) {
            if (ca.getJoystick() == controllerAction.getJoystick()) {
                newActions.remove(ca);
            }
        }
        newActions.add(controllerAction);
        //ControllerActionUtils.setControllerActions(this, newActions);
        this.controllerActions = newActions;
        this.adapter.refresh(controllerActions);
    }

    private void alertCancel(DialogInterface dialogInterface) {
        alertDialog = null;
        buttonType = null;
        keyValue = -1;
        axisValue = -1;
        axisDirection = 0;
    }

    private void remap(ButtonType buttonType, int keyValue) {
        List<ControllerAction> newActions = new ArrayList<>(controllerActions);
        for (ControllerAction controllerAction : controllerActions) {
            if (controllerAction.getKey() == keyValue) {
                newActions.remove(controllerAction);
            }
            if (controllerAction.getButton() == buttonType) {
                newActions.remove(controllerAction);
            }
        }
        newActions.add(new ControllerAction(buttonType, keyValue));
        //ControllerActionUtils.setControllerActions(this, newActions);
        this.controllerActions = newActions;
        this.adapter.refresh(controllerActions);
    }

    private void remap(ButtonType buttonType, int axisValue, int axisDirection) {
        List<ControllerAction> newActions = new ArrayList<>(controllerActions);
        for (ControllerAction controllerAction : controllerActions) {
            if (controllerAction.getType() == AXIS &&
                    controllerAction.getXAxis() == axisValue &&
                    controllerAction.getXDirection() == axisDirection) {
                newActions.remove(controllerAction);
            }
            if (controllerAction.getButton() == buttonType) {
                newActions.remove(controllerAction);
            }
        }
        newActions.add(new ControllerAction(buttonType, axisValue, axisDirection));
        this.controllerActions = newActions;
        this.adapter.refresh(controllerActions);
    }

    @Override
    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
        log("Key", event.toString());
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if (Objects.nonNull(buttonType)) {
                if (keyCode != keyValue) {
                    String keyName = Optional.ofNullable(BUTTON_NAMES.get(keyCode))
                            .orElse(getString(R.string.unknown));
                    keyValue = keyCode;
                    alertDialog.setMessage(
                            this.getString(R.string.repress_to_map, buttonType.name(), keyName));
                } else {
                    remap(buttonType, keyValue);
                    alertDialog.cancel();
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onMotionEvent(MotionEvent motionEvent) {
        InputDevice device = motionEvent.getDevice();
        for (Map.Entry<Integer, String> axisEntry : AXIS_NAMES.entrySet()) {
            Integer axis = axisEntry.getKey();
            int maxedAxis = EventUtils.getMaxedAxis(motionEvent, device, axis);
            if (maxedAxis != 0) {
                log("AXIS_NAME", axisEntry.getValue());
                if (Objects.nonNull(buttonType)) {
                    if (axisValue != axis || axisDirection != maxedAxis) {
                        alertDialog.setMessage(
                                this.getString(
                                        R.string.push_axis_same_direction_to_map_to_button,
                                        axisEntry.getValue(),
                                        buttonType.name()));
                        axisValue = axis;
                        axisDirection = maxedAxis;
                    } else {
                        remap(buttonType, axisValue, axisDirection);
                        alertDialog.cancel();
                    }
                    return true;
                }
            }
        }
        return false;
    }

    public void saveClicked(View view) {
        saveControllerActionsAndFinish();
    }

    private void saveControllerActionsAndFinish() {
        ControllerActionUtils.setControllerActions(this, controllerActions);
        Toast.makeText(this, R.string.button_mappings_saves, Toast.LENGTH_LONG).show();
    }

}
