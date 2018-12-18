package com.rdapps.gamepad.vibrator;

import android.os.VibrationEffect;

import lombok.Getter;

import static android.os.VibrationEffect.DEFAULT_AMPLITUDE;

public enum VibrationPattern {
    BUTTON_PRESS(VibrationEffect.createOneShot(100, DEFAULT_AMPLITUDE)),
    BUTTON_RELEASE(VibrationEffect.createOneShot(50, DEFAULT_AMPLITUDE)),
    STICK_PRESS(VibrationEffect.createWaveform(new long[]{30, 20, 10}, -1)),
    STICK_RELEASE(VibrationEffect.createWaveform(new long[]{20, 10, 10}, -1)),
    STICK_MOVE(VibrationEffect.createWaveform(
            new long[]{20, 10, 10},
            new int[]{50, 30, 25},
            -1));

    @Getter
    VibrationEffect vibrationEffect;

    VibrationPattern(VibrationEffect vibrationEffect) {
        this.vibrationEffect = vibrationEffect;
    }
}
