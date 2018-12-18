package com.rdapps.gamepad.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.rdapps.gamepad.device.ButtonType;
import com.rdapps.gamepad.device.JoystickType;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ControllerAction implements Serializable {

    private Type type;

    private int key;
    private ButtonType button;

    private JoystickType joystick;
    private int xAxis;
    private int xDirection;
    private int yAxis;
    private int yDirection;

    public enum Type {
        BUTTON,
        AXIS,
        JOYSTICK
    }

    public ControllerAction(ButtonType button, int key) {
        this.type = Type.BUTTON;
        this.key = key;
        this.button = button;
    }

    public ControllerAction(ButtonType button, int xAxis, int xDirection) {
        this.type = Type.AXIS;
        this.button = button;
        this.xAxis = xAxis;
        this.xDirection = xDirection;
    }

    public ControllerAction(JoystickType joystick, int xAxis, int xDirection, int yAxis, int yDirection) {
        this.type = Type.JOYSTICK;
        this.joystick = joystick;
        this.xAxis = xAxis;
        this.xDirection = xDirection;
        this.yAxis = yAxis;
        this.yDirection = yDirection;
    }


    public void from(ControllerAction ca) {
        this.type = ca.type;
        this.key = ca.key;
        this.button = ca.button;

        this.joystick = ca.joystick;
        this.xAxis = ca.xAxis;
        this.xDirection = ca.xDirection;
        this.yAxis = ca.yAxis;
        this.yDirection = ca.yDirection;
    }
}
