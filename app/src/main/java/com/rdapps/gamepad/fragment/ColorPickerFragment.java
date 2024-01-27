package com.rdapps.gamepad.fragment;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.azeesoft.lib.colorpicker.ColorPickerDialog;
import com.rdapps.gamepad.R;
import com.rdapps.gamepad.memory.ControllerMemory;
import com.rdapps.gamepad.memory.RAFSPIMemory;
import com.rdapps.gamepad.protocol.ControllerType;
import com.rdapps.gamepad.util.ByteUtils;

import java.io.IOException;
import java.io.Serializable;
import java.util.Locale;
import java.util.Optional;

import static com.rdapps.gamepad.log.JoyConLog.log;

public class ColorPickerFragment extends Fragment implements View.OnClickListener, ResettableSettingFragment {

    private static final String TAG = ColorPickerFragment.class.getName();

    private static final String TYPE = "TYPE";
    private static final String SECTION = "SECTION";

    private ControllerType type;
    private ColorSection section;
    private ControllerMemory eeprom;
    private View colorView;
    private TextView textView;

    public static ColorPickerFragment getInstance(Serializable type, Serializable section) {
        ColorPickerFragment colorPickerFragment = new ColorPickerFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(TYPE, type);
        bundle.putSerializable(SECTION, section);
        colorPickerFragment.setArguments(bundle);
        return colorPickerFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.component_color_setting, container, false);
        Bundle arguments = getArguments();
        type = (ControllerType) arguments.getSerializable(TYPE);
        section = (ColorSection) arguments.getSerializable(SECTION);
        try {
            eeprom = new ControllerMemory(new RAFSPIMemory(getContext(), type.getBTName(), type.getMemoryResource()));
        } catch (IOException e) {
            log(TAG, "EEPROM could not load.", e);
        }

        textView = view.findViewById(R.id.controllerTextView);
        colorView = view.findViewById(R.id.colorView);
        view.findViewById(R.id.cardView).setOnClickListener(this);
        if (type == ControllerType.PRO_CONTROLLER) {
            if (section == ColorSection.BODY) {
                textView.setText(R.string.pro_controller_body_color);
            } else {
                textView.setText(R.string.pro_controller_button_color);
            }
        } else if (type == ControllerType.LEFT_JOYCON) {
            if (section == ColorSection.BODY) {
                textView.setText(R.string.left_joycon_body_color);
            } else {
                textView.setText(R.string.left_joycon_button_color);
            }
        } else {
            if (section == ColorSection.BODY) {
                textView.setText(R.string.right_joycon_body_color);
            } else {
                textView.setText(R.string.right_joycon_button_color);
            }
        }

        if (eeprom != null) {
            if (section == ColorSection.BODY) {
                int bodyColor = eeprom.getBodyColor();
                colorView.setBackgroundColor(bodyColor);
            } else {
                int buttonColor = eeprom.getButtonColor();
                colorView.setBackgroundColor(buttonColor);
            }
        }
        return view;
    }

    @Override
    public void onInflate(Context context, AttributeSet attrs, Bundle savedInstanceState) {
        super.onInflate(context, attrs, savedInstanceState);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ColorFragment);
        if (typedArray != null) {
            String type = typedArray.getString(R.styleable.ColorFragment_type);
            ControllerType controllerType = Optional.ofNullable(type)
                    .map(t -> {
                        try {
                            return ControllerType.valueOf(t.toUpperCase(Locale.ROOT));
                        } catch (Exception e) {
                            return null;
                        }
                    })
                    .orElse(ControllerType.LEFT_JOYCON);
            String section = typedArray.getString(R.styleable.ColorFragment_section);
            ColorSection sectionEnum = Optional.ofNullable(section)
                    .map(s -> {
                        try {
                            return ColorSection.valueOf(s.toUpperCase(Locale.ROOT));
                        } catch (Exception e) {
                            return null;
                        }
                    })
                    .orElse(ColorSection.BODY);
            Bundle bundle = new Bundle();
            bundle.putSerializable(TYPE, controllerType);
            bundle.putSerializable(SECTION, sectionEnum);
            setArguments(bundle);
        }
    }

    @Override
    public void onClick(View v) {
        ColorPickerDialog colorPickerDialog = ColorPickerDialog.createColorPickerDialog(getContext());
        colorPickerDialog.setTitle(textView.getText());
        colorPickerDialog.setOnColorPickedListener((color, hexVal) -> {
            if (eeprom != null) {
                if (section == ColorSection.BODY) {
                    eeprom.setBodyColor(color);
                } else {
                    eeprom.setButtonColor(color);
                }
            }
            colorView.setBackgroundColor(color);
        });
        if (eeprom != null) {
            int color = 0;
            if (section == ColorSection.BODY) {
                color = eeprom.getBodyColor();
            } else {
                color = eeprom.getButtonColor();
            }
            colorPickerDialog.setInitialColor(color);
        }
        colorPickerDialog.setNegativeActionText(getString(android.R.string.cancel));
        colorPickerDialog.setPositiveActionText(getString(android.R.string.ok));
        colorPickerDialog.show();
    }

    @Override
    public void reset() {
        int color = 0;
        if (type == ControllerType.PRO_CONTROLLER) {
            if (section == ColorSection.BODY) {
                eeprom.setBodyColor(color);
            } else {
                eeprom.setButtonColor(color);
            }
        } else if (type == ControllerType.LEFT_JOYCON) {
            if (section == ColorSection.BODY) {
                color = ByteUtils.byteArrayToColor(new byte[]{0x0A, (byte) 0xB9, (byte) 0xE6});
                eeprom.setBodyColor(color);
            } else {
                color = ByteUtils.byteArrayToColor(new byte[]{0x00, 0x1E, 0x1E});
                eeprom.setButtonColor(color);
            }
        } else {
            if (section == ColorSection.BODY) {
                color = ByteUtils.byteArrayToColor(new byte[]{(byte) 0xFF, 0x3C, 0x28});
                eeprom.setBodyColor(color);
            } else {
                color = ByteUtils.byteArrayToColor(new byte[]{0x1E, 0x0A, 0x0A});
                eeprom.setButtonColor(color);
            }
        }
        colorView.setBackgroundColor(color);
    }
}
