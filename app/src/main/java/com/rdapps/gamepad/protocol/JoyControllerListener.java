package com.rdapps.gamepad.protocol;

import com.rdapps.gamepad.led.LedState;

public interface JoyControllerListener {
    void showAmiiboPicker();

    void setPlayerLights(LedState led1, LedState led2, LedState led3, LedState led4);
}
