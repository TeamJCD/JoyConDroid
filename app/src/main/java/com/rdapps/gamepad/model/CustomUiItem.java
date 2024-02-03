package com.rdapps.gamepad.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.rdapps.gamepad.protocol.ControllerType;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomUiItem {
    private String name;
    private String url;
    private ControllerType type;
    private int version;
    private int appVersion;
}
