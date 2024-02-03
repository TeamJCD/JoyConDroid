package com.rdapps.gamepad;

import static com.rdapps.gamepad.log.JoyConLog.log;

import android.content.Context;
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;
import androidx.test.platform.app.InstrumentationRegistry;
import com.google.android.gms.common.util.Hex;
import com.rdapps.gamepad.protocol.ControllerType;
import com.rdapps.gamepad.protocol.JoyController;
import com.rdapps.gamepad.protocol.JoyControllerBuilder;
import com.rdapps.gamepad.report.InputReport;
import com.rdapps.gamepad.sensor.AccelerometerEvent;
import com.rdapps.gamepad.sensor.GyroscopeEvent;
import java.util.Arrays;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4ClassRunner.class)
public class SensorTest {
    private static final String TAG = SensorTest.class.getName();

    private static JoyController joyController;

    @BeforeClass
    public static void setup() {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        joyController = JoyControllerBuilder.with(appContext)
                .setType(ControllerType.PRO_CONTROLLER)
                .setLocalMacAddress("C0:17:4D:69:F6:55")
                .build();
    }

    @Test
    public void noEventSensorData() {
        InputReport inputReport = new InputReport(InputReport.Type.STANDARD_FULL_REPORT);
        inputReport.fillSensorData(joyController);
        byte[] buffer = inputReport.getBuffer();

        String bytes = Hex.bytesToStringUppercase(Arrays.copyOfRange(buffer, 12, buffer.length));
        log(TAG, "Bytes: " + bytes);
        Assert.assertEquals(72, bytes.length());
        bytes = bytes.replaceAll("0", "");
        Assert.assertEquals(0, bytes.length());

    }

    @Test
    public void gravityOnlySensorData() throws Exception {
        InputReport inputReport = new InputReport(InputReport.Type.STANDARD_FULL_REPORT);
        AccelerometerEvent accelerometerEvent = new AccelerometerEvent();
        accelerometerEvent.values = new float[]{0, 0, 9.8f};
        joyController.getAccelerometerEvents().add(accelerometerEvent);


        inputReport.fillSensorData(joyController);
        byte[] buffer = inputReport.getBuffer();
        String bytes = Hex.bytesToStringUppercase(Arrays.copyOfRange(buffer, 12, buffer.length));

        log(TAG, "Gravity Bytes: " + bytes);

        Assert.assertEquals(72, bytes.length());

    }

    @Test
    public void valueSensorData() throws Exception {
        InputReport inputReport = new InputReport(InputReport.Type.STANDARD_FULL_REPORT);
        AccelerometerEvent accelerometerEvent = new AccelerometerEvent();
        accelerometerEvent.values = new float[]{0, 0, 4.8f};
        joyController.getAccelerometerEvents().add(accelerometerEvent);


        inputReport.fillSensorData(joyController);
        byte[] buffer = inputReport.getBuffer();
        String bytes = Hex.bytesToStringUppercase(Arrays.copyOfRange(buffer, 12, buffer.length));

        log(TAG, "Value Bytes: " + bytes);

        Assert.assertEquals(72, bytes.length());

    }

    @Test
    public void gyroSensorData() {
        InputReport inputReport = new InputReport(InputReport.Type.STANDARD_FULL_REPORT);
        GyroscopeEvent gyroscopeEvent = new GyroscopeEvent();
        gyroscopeEvent.values = new float[]{0.04f, 0.03f, 0.02f};
        joyController.getGyroscopeEvents().add(gyroscopeEvent);


        inputReport.fillSensorData(joyController);
        byte[] buffer = inputReport.getBuffer();
        String bytes = Hex.bytesToStringUppercase(Arrays.copyOfRange(buffer, 12, buffer.length));

        log(TAG, "Gyro Value Bytes: " + bytes);

        Assert.assertEquals(72, bytes.length());

    }
}
