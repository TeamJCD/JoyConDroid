package com.rdapps.gamepad.command.handler.subcommand;

import com.rdapps.gamepad.protocol.JoyController;
import com.rdapps.gamepad.report.InputReport;
import com.rdapps.gamepad.report.OutputReport;

public interface SubCommandHandler {
    InputReport handleRumbleAndSubCommand(JoyController joyController, OutputReport outputReport);
}
