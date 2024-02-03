package com.rdapps.gamepad.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.rdapps.gamepad.device.ButtonType;
import com.rdapps.gamepad.device.JoystickType;
import java.io.Serial;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ControllerAction implements Serializable {
    @Serial
    private static final long serialVersionUID = -7980767209480839283L;

    private Type type;

    private int key;
    private ButtonType button;

    private JoystickType joystick;
    private int axisX;
    private int directionX;
    private int axisY;
    private int directionY;

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

    public ControllerAction(ButtonType button, int axisX, int directionX) {
        this.type = Type.AXIS;
        this.button = button;
        this.axisX = axisX;
        this.directionX = directionX;
    }

    public ControllerAction(
            JoystickType joystick, int axisX, int directionX, int axisY, int directionY) {
        this.type = Type.JOYSTICK;
        this.joystick = joystick;
        this.axisX = axisX;
        this.directionX = directionX;
        this.axisY = axisY;
        this.directionY = directionY;
    }


    public void from(ControllerAction ca) {
        this.type = ca.type;
        this.key = ca.key;
        this.button = ca.button;

        this.joystick = ca.joystick;
        this.axisX = ca.axisX;
        this.directionX = ca.directionX;
        this.axisY = ca.axisY;
        this.directionY = ca.directionY;
    }
}
