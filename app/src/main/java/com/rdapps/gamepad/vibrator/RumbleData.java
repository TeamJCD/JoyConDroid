package com.rdapps.gamepad.vibrator;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RumbleData {
    private byte[] rumbleData;

    /**
     * Decodes the combined LF amplitude of both rumble channels as an Android amplitude
     * in [0, 255]. Returns 0 if both channels are silent.
     *
     * <p>
     * LF amplitude encoding (byte[3] of each channel):
     * {@code lf_amp_byte = (encoded_hex_amp / 2) + 0x40}, neutral = 0x40.
     * Inverse: {@code encoded_hex_amp = (lf_amp_byte - 0x40) * 2}.
     * Amplitude: {@code 2^(idx/32) / 8.7} for idx >= 32, {@code 2^(idx/16) / 17} for idx >= 16.
     */
    public int getAndroidAmplitude() {
        float left = decodeChannelAmplitude(rumbleData, 0);
        float right = decodeChannelAmplitude(rumbleData, 4);
        float amplitude = Math.max(left, right);
        if (amplitude <= 0f) {
            return 0;
        }
        return Math.max(1, Math.round(amplitude * 255));
    }

    private static float decodeChannelAmplitude(byte[] data, int offset) {
        int lfAmpByte = data[offset + 3] & 0xFF;
        int idx = (lfAmpByte - 0x40) * 2;
        if (idx <= 0) {
            return 0f;
        }
        double amp;
        if (idx >= 32) {
            amp = Math.pow(2.0, idx / 32.0) / 8.7;
        } else if (idx >= 16) {
            amp = Math.pow(2.0, idx / 16.0) / 17.0;
        } else {
            return 0f;
        }
        return (float) Math.min(1.0, amp);
    }
}
