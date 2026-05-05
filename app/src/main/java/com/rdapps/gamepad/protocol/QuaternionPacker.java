package com.rdapps.gamepad.protocol;

/**
 * Maintains a running orientation quaternion and packs it into the Switch
 * packing_mode_2 motion data format (36 bytes), used when subcommand 0x40 enables
 * quaternion mode (values 0x02-0x05).
 *
 * <p>
 * Quaternion components [x, y, z, w]. The max-abs component is omitted (reconstructed
 * by the Switch from the unit-norm constraint). The remaining 3 are stored as 21-bit
 * signed fixed-point values where 2^20 = 1.0.
 */
public class QuaternionPacker {

    // Running orientation, identity = {0, 0, 0, 1}: indices [x, y, z, w]
    private final double[] rotation = {0.0, 0.0, 0.0, 1.0};
    private long lastTimestampNs = 0;

    public void reset() {
        rotation[0] = 0.0;
        rotation[1] = 0.0;
        rotation[2] = 0.0;
        rotation[3] = 1.0;
        lastTimestampNs = 0;
    }

    /**
     * Integrate one gyroscope sample into the running quaternion.
     *
     * <p>
     * Uses a constrained Euler approximation (Taylor series of sin/cos to 4th order)
     * to avoid transcendental functions. Input must be in rad/s (Android TYPE_GYROSCOPE unit).
     *
     * @param timestampNs SensorEvent.timestamp (nanoseconds from boot)
     */
    public void integrateGyro(double gyroX, double gyroY, double gyroZ, long timestampNs) {
        long dtNs;
        if (lastTimestampNs == 0) {
            dtNs = 16_666_667L; // fallback: one frame at 60 Hz
        } else {
            dtNs = timestampNs - lastTimestampNs;
            if (dtNs <= 0 || dtNs > 500_000_000L) {
                dtNs = 16_666_667L;
            }
        }
        lastTimestampNs = timestampNs;

        // Angle change in radians for this time step
        double ax = gyroX * dtNs * 1e-9;
        double ay = gyroY * dtNs * 1e-9;
        double az = gyroZ * dtNs * 1e-9;

        double normSq = ax * ax + ay * ay + az * az;

        // Taylor approximations: sin(|angle|/2)/|angle| and cos(|angle|/2)
        double vecScale   = normSq * normSq / 3840.0 - normSq / 48.0 + 0.5;
        double scalarComp = normSq * normSq /  384.0 - normSq /  8.0 + 1.0;

        double dqx = ax * vecScale;
        double dqy = ay * vecScale;
        double dqz = az * vecScale;
        double dqw = scalarComp;

        // Hamilton product: rotation * dq
        double qx = rotation[0];
        double qy = rotation[1];
        double qz = rotation[2];
        double qw = rotation[3];
        rotation[0] = qw * dqx + qx * dqw + qy * dqz - qz * dqy;
        rotation[1] = qw * dqy + qy * dqw + qz * dqx - qx * dqz;
        rotation[2] = qw * dqz + qz * dqw + qx * dqy - qy * dqx;
        rotation[3] = qw * dqw - qx * dqx - qy * dqy - qz * dqz;

        double len = Math.sqrt(rotation[0] * rotation[0] + rotation[1] * rotation[1]
                + rotation[2] * rotation[2] + rotation[3] * rotation[3]);
        if (len > 1e-10) {
            rotation[0] /= len;
            rotation[1] /= len;
            rotation[2] /= len;
            rotation[3] /= len;
        }
    }

    /**
     * Pack the current quaternion and 3 accelerometer samples into a 36-byte
     * packing_mode_2 motion data block starting at buf[offset].
     *
     * <p>
     * Layout (all little-endian): [0-5] accel_0; [6-9] u32 with packing_mode/max_index/c0/c1_low;
     * [10-11] u16 with c1_high/c2_low; [12-17] accel_1; [18-21] u32 with c2_high/deltas=0;
     * [22-23] u16 deltas=0; [24-29] accel_2; [30-33] u32 deltas+timestamp_l; [34-35] u16
     * timestamp_h/count=3.
     *
     * @param accelSamples 3 raw accelerometer samples, each short[3] = {x, y, z}
     * @param timestampMs  SystemClock.elapsedRealtime() — low 11 bits used
     */
    public void pack(byte[] buf, int offset, short[][] accelSamples, long timestampMs) {
        // Find the largest absolute-value component (omitted from the packet)
        int maxIdx = 3; // default w
        double maxAbs = Math.abs(rotation[3]);
        for (int i = 0; i < 3; i++) {
            double abs = Math.abs(rotation[i]);
            if (abs > maxAbs) {
                maxAbs = abs;
                maxIdx = i;
            }
        }

        // Sign factor: ensure the reconstructed max component is positive
        double sign = rotation[maxIdx] >= 0 ? 1.0 : -1.0;

        // Cyclic order of the 3 transmitted components
        int i0 = (maxIdx + 1) & 3;
        int i1 = (maxIdx + 2) & 3;
        int i2 = (maxIdx + 3) & 3;

        // Encode to 21-bit signed fixed-point: 2^20 = 1.0, range [-2^20, 2^20-1]
        int c0 = clamp21((int) Math.round(rotation[i0] * sign * 1048576.0));
        int c1 = clamp21((int) Math.round(rotation[i1] * sign * 1048576.0));
        int c2 = clamp21((int) Math.round(rotation[i2] * sign * 1048576.0));

        writeAccel(buf, offset, accelSamples[0]);

        // u32: packing_mode(2) | max_index(2) | c0[20:0](21) | c1[6:0](7)
        int word0 = 2 | (maxIdx << 2) | ((c0 & 0x1FFFFF) << 4) | ((c1 & 0x7F) << 25);
        putLe32(buf, offset + 6, word0);

        // u16: c1[20:7](14) | c2[1:0](2)
        int word1 = ((c1 >> 7) & 0x3FFF) | ((c2 & 0x3) << 14);
        putLe16(buf, offset + 10, word1);

        writeAccel(buf, offset + 12, accelSamples[1]);

        // u32: c2[20:2](19) | delta_last_first_0(13)=0
        int word2 = (c2 >> 2) & 0x7FFFF;
        putLe32(buf, offset + 18, word2);

        // u16: remaining deltas = 0
        putLe16(buf, offset + 22, 0);

        writeAccel(buf, offset + 24, accelSamples[2]);

        // u32: delta fields(31)=0 | timestamp_start_l(1)
        int ts = (int) (timestampMs & 0x7FF); // 11-bit counter
        putLe32(buf, offset + 30, (ts & 1) << 31);

        // u16: timestamp_start_h(10) | timestamp_count(6)=3
        putLe16(buf, offset + 34, ((ts >> 1) & 0x3FF) | (3 << 10));
    }

    // 21-bit signed clamp: range is -2^20 to 2^20-1
    private static int clamp21(int v) {
        return Math.max(-1048576, Math.min(1048575, v));
    }

    private static void writeAccel(byte[] buf, int offset, short[] accel) {
        buf[offset]     = (byte)  (accel[0]       & 0xFF);
        buf[offset + 1] = (byte) ((accel[0] >> 8) & 0xFF);
        buf[offset + 2] = (byte)  (accel[1]       & 0xFF);
        buf[offset + 3] = (byte) ((accel[1] >> 8) & 0xFF);
        buf[offset + 4] = (byte)  (accel[2]       & 0xFF);
        buf[offset + 5] = (byte) ((accel[2] >> 8) & 0xFF);
    }

    private static void putLe32(byte[] buf, int offset, int v) {
        buf[offset]     = (byte)  (v         & 0xFF);
        buf[offset + 1] = (byte) ((v >>  8)  & 0xFF);
        buf[offset + 2] = (byte) ((v >> 16)  & 0xFF);
        buf[offset + 3] = (byte) ((v >> 24)  & 0xFF);
    }

    private static void putLe16(byte[] buf, int offset, int v) {
        buf[offset]     = (byte)  (v       & 0xFF);
        buf[offset + 1] = (byte) ((v >> 8) & 0xFF);
    }
}
