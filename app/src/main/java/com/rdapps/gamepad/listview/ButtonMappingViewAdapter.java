package com.rdapps.gamepad.listview;

import static com.rdapps.gamepad.model.ControllerAction.Type.AXIS;
import static com.rdapps.gamepad.model.ControllerAction.Type.BUTTON;
import static com.rdapps.gamepad.util.ControllerActionUtils.AXIS_NAMES;
import static com.rdapps.gamepad.util.ControllerActionUtils.BUTTON_NAMES;
import static com.rdapps.gamepad.util.ControllerActionUtils.getJoystickMapping;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.rdapps.gamepad.R;
import com.rdapps.gamepad.device.ButtonType;
import com.rdapps.gamepad.device.JoystickType;
import com.rdapps.gamepad.model.ControllerAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class ButtonMappingViewAdapter extends BaseAdapter {

    private final LayoutInflater layoutInflater;
    private List<ControllerAction> controllerActionList;
    private Map<Enum<?>, ControllerAction> actionMap;

    private Map<JoystickType, ControllerAction> joysticks;

    public ButtonMappingViewAdapter(Context context, List<ControllerAction> controllerActions) {
        this.refresh(controllerActions);
        this.layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return controllerActionList.size();
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

    @Override
    public int getItemViewType(int position) {
        ControllerAction.Type type = getItem(position).getType();
        return type.ordinal();
    }

    @Override
    public ControllerAction getItem(int position) {
        return controllerActionList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        ControllerAction action = getItem(position);
        if (Objects.isNull(view)) {
            int layout = R.layout.stick_mapping;
            if (action.getType() == BUTTON) {
                layout = R.layout.button_mapping;
            }
            view = layoutInflater.inflate(layout, parent, false);
        }

        if (Objects.isNull(action)) {
            return view;
        }


        if (action.getType() == BUTTON) {
            TextView nameView = view.findViewById(R.id.buttonName);
            TextView valueView = view.findViewById(R.id.buttonValue);

            ButtonType buttonType = action.getButton();
            Integer keyValue = actionMap.get(buttonType).getKey();
            String keyName = Optional.ofNullable(keyValue)
                    .map(BUTTON_NAMES::get)
                    .orElse(null);

            nameView.setText(buttonType.name());
            if (Objects.nonNull(keyName)) {
                valueView.setText(keyName);
            } else {
                valueView.setText(R.string.unknown);
            }
        } else if (action.getType() == AXIS) {
            TextView nameView = view.findViewById(R.id.stickName);
            TextView valueViewX = view.findViewById(R.id.stickXValue);
            TextView valueViewY = view.findViewById(R.id.stickYValue);

            ButtonType buttonType = action.getButton();
            Integer axisValue = actionMap.get(buttonType).getAxisX();
            String axisName = Optional.ofNullable(axisValue)
                    .map(AXIS_NAMES::get)
                    .orElse(null);

            nameView.setText(buttonType.name());
            valueViewY.setText(String.valueOf(action.getDirectionX()));
            if (Objects.nonNull(axisName)) {
                valueViewX.setText(axisName);
            } else {
                valueViewX.setText(R.string.unknown);
            }
        } else {
            TextView nameView = view.findViewById(R.id.stickName);
            TextView valueViewX = view.findViewById(R.id.stickXValue);
            TextView valueViewY = view.findViewById(R.id.stickYValue);

            JoystickType joystick = action.getJoystick();
            nameView.setText(joystick.name());
            ControllerAction ca = joysticks.get(joystick);

            if (Objects.nonNull(ca)) {
                String axisNameX = AXIS_NAMES.get(ca.getAxisX());
                if (Objects.nonNull(axisNameX)) {
                    valueViewX.setText(axisNameX);
                } else {
                    valueViewX.setText(R.string.unknown);
                }
                String axisNameY = AXIS_NAMES.get(ca.getAxisY());
                if (Objects.nonNull(axisNameY)) {
                    valueViewY.setText(axisNameY);
                } else {
                    valueViewY.setText(R.string.unknown);
                }
            } else {
                valueViewX.setText(R.string.unknown);
                valueViewY.setText(R.string.unknown);
            }
        }

        return view;
    }

    public void refresh(List<ControllerAction> controllerActions) {
        controllerActionList = new ArrayList<>();
        actionMap = new HashMap<>();
        Arrays.stream(ButtonType.values())
                .map(type -> new ControllerAction(type, 0))
                .forEach(ca -> {
                    controllerActionList.add(ca);
                    actionMap.put(ca.getButton(), ca);
                });
        Arrays.stream(JoystickType.values())
                .map(type -> new ControllerAction(type, 0, 0, 0, 0))
                .forEach(ca -> {
                    controllerActionList.add(ca);
                    actionMap.put(ca.getJoystick(), ca);
                });

        this.joysticks = getJoystickMapping(controllerActions);
        controllerActions.forEach(
                ca -> {
                    ButtonType button = ca.getButton();
                    if (Objects.nonNull(button)) {
                        actionMap.get(button).from(ca);
                    } else {
                        actionMap.get(ca.getJoystick()).from(ca);
                    }
                }
        );
        notifyDataSetChanged();
    }
}
