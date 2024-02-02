package com.rdapps.gamepad.protocol;

import android.content.Context;
import android.os.Process;

import com.rdapps.gamepad.amiibo.AmiiboConfig;
import com.rdapps.gamepad.button.ButtonState;
import com.rdapps.gamepad.command.handler.InputHandler;
import com.rdapps.gamepad.command.handler.OutputHandler;
import com.rdapps.gamepad.log.JoyConLog;
import com.rdapps.gamepad.memory.ControllerMemory;
import com.rdapps.gamepad.memory.DummySPIMemory;
import com.rdapps.gamepad.memory.FileSPIMemory;
import com.rdapps.gamepad.memory.RAFSPIMemory;
import com.rdapps.gamepad.memory.SPIMemory;
import com.rdapps.gamepad.util.MacUtils;
import com.rdapps.gamepad.util.PriorityThreadFactory;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class JoyControllerBuilder {

    private static final String TAG = JoyControllerBuilder.class.getName();

    public static JoyControllerBuilder with(Context context) {
        return new JoyControllerBuilder(context);
    }

    private final Context context;
    private ControllerType type;
    private ControllerMemory memory;
    private ButtonState buttonsState;
    private JoyControllerListener listener;
    private String localMacAddress;
    private ScheduledExecutorService executorService;

    private JoyControllerBuilder(Context context) {
        this.context = context;
    }

    public JoyController build() {
        if (Objects.isNull(type)) {
            RuntimeException rex = new RuntimeException("Controller Type is Not Set");
            JoyConLog.log(TAG, "Controller Type is Not Set", rex);
            throw rex;
        }

        if (Objects.isNull(localMacAddress)) {
            RuntimeException rex = new RuntimeException("Local MAC Address is Not Set");
            JoyConLog.log(TAG, "Local MAC Address is Not Set", rex);
            throw rex;
        }

        createExecutorService();
        JoyControllerState joyControllerState = new JoyControllerState(getMacBytes());
        joyControllerState.calculateCoeffs(memory);
        JoyController joyController = new JoyController(
                context,
                type,
                memory,
                buttonsState,
                new AmiiboConfig(context),
                executorService,
                new JoyControllerConfig(context),
                joyControllerState,
                listener
        );

        joyController.setInputHandler(new InputHandler(joyController));
        joyController.setOutputHandler(new OutputHandler(joyController));
        return joyController;
    }

    private byte[] getMacBytes() {
        return MacUtils.parseMacAddress(localMacAddress);
    }

    private void createExecutorService() {
        this.executorService = Executors.newSingleThreadScheduledExecutor(
                new PriorityThreadFactory(Process.THREAD_PRIORITY_URGENT_AUDIO,
                        true,
                        "BT Thread",
                        false)
        );
    }

    public JoyControllerBuilder setType(ControllerType type) {
        this.type = type;
        createMemory();
        createButtonsState();
        return this;
    }

    public JoyControllerBuilder setLocalMacAddress(String localMacAddress) {
        this.localMacAddress = localMacAddress;
        return this;
    }

    public JoyControllerBuilder setListener(JoyControllerListener listener) {
        this.listener = listener;
        return this;
    }

    private void createMemory() {
        SPIMemory memory = null;
        try {
            memory = createRAFSPIMemory();
        } catch (IOException e) {
            JoyConLog.log(TAG, "RAFSPIMemory Failed.", e);
        }

        if (Objects.isNull(memory)) {
            try {
                memory = createFileMemory();
            } catch (IOException e) {
                JoyConLog.log(TAG, "FileSPIMemory Failed.", e);
            }
        }

        if (Objects.isNull(memory)) {
            memory = new DummySPIMemory();
        }

        this.memory = new ControllerMemory(memory);
    }

    private RAFSPIMemory createRAFSPIMemory() throws IOException {
        String bluetoothName = type.getBTName();
        int memoryResource = type.getMemoryResource();
        return new RAFSPIMemory(context, bluetoothName, memoryResource);
    }

    private FileSPIMemory createFileMemory() throws IOException {
        int memoryResource = type.getMemoryResource();
        return new FileSPIMemory(context, memoryResource);
    }

    private void createButtonsState() {
        this.buttonsState = new ButtonState(type);
    }
}
