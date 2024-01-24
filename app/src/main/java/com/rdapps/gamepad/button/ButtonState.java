package com.rdapps.gamepad.button;

import com.rdapps.gamepad.protocol.ControllerType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.Data;

@Data
public class ButtonState {
    public static final int BUTTON_DOWN = 100;
    public static final int BUTTON_UP = 0;
    public static final int STICK_CENTER = 0;
    public static final int STICK_NEGATIVE = -100;
    public static final int STICK_POSITIVE = 100;

    private ControllerType type;
    private Map<ButtonEnum, Integer> buttons;
    private Map<AxisEnum, Integer> axes;

    public ButtonState(ControllerType type) {
        this.type = type;
        this.buttons = new HashMap<>(
                Arrays.asList(ButtonEnum.values())
                        .stream()
                        .collect(Collectors.toMap(Function.identity(), (buttonEnum) -> BUTTON_UP)));
        this.axes = new HashMap<>(
                Arrays.asList(AxisEnum.values())
                        .stream()
                        .collect(Collectors.toMap(Function.identity(), (axisEnum -> STICK_CENTER))));
    }

    public void setButton(ButtonEnum button, int value) {
        buttons.put(button, value);
    }

    public int getButton(ButtonEnum button) {
        return buttons.getOrDefault(button, BUTTON_UP);
    }

    public void setAxis(AxisEnum axis, int value) {
        axes.put(axis, value);
    }

    public int getAxis(AxisEnum axis) {
        return axes.getOrDefault(axis, STICK_CENTER);
    }
}
