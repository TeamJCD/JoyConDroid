package com.rdapps.gamepad.fragment;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import com.jaredrummler.android.device.DeviceName;
import com.rdapps.gamepad.R;
import com.rdapps.gamepad.util.PreferenceUtils;
import java.io.Serializable;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;

public class FeatureSwitchFragment extends Fragment
        implements CompoundButton.OnCheckedChangeListener, ResettableSettingFragment {

    private static String TAG = FeatureSwitchFragment.class.getName();

    private static final String TYPE = "TYPE";

    private FeatureType type;
    @Setter
    @Getter
    private FeatureSwitchListener featureSwitchListener;
    private SwitchCompat switchController;
    private boolean enabled;

    public static FeatureSwitchFragment getInstance(Serializable type) {
        FeatureSwitchFragment colorPickerFragment = new FeatureSwitchFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(TYPE, type);
        colorPickerFragment.setArguments(bundle);
        return colorPickerFragment;
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.component_feature_switch, container, false);
        Bundle arguments = getArguments();
        type = (FeatureType) arguments.getSerializable(TYPE);

        switchController = view.findViewById(R.id.featureSwitch);
        switchController.setOnCheckedChangeListener(this);
        enabled = false;

        TextView textView = view.findViewById(R.id.controllerTextView);

        switch (type) {
            case ACCELEROMETER:
                textView.setText(R.string.enable_accelerometer);
                enabled = PreferenceUtils.getAccelerometerEnabled(getContext());
                break;
            case GYROSCOPE:
                textView.setText(R.string.enable_gyroscope);
                enabled = PreferenceUtils.getGyroscopeEnabled(getContext());
                break;
            case AMIIBO:
                textView.setText(R.string.enable_amiibo);
                enabled = PreferenceUtils.getAmiiboEnabled(getContext());
                break;
            case HAPTIC_FEEDBACK:
                textView.setText(R.string.enable_haptic_feedback);
                enabled = PreferenceUtils.getHapticFeedBackEnabled(getContext());
                break;
            default:
                break;
        }
        switchController.setChecked(enabled);
        return view;
    }

    @Override
    public void onInflate(Context context, AttributeSet attrs, Bundle savedInstanceState) {
        super.onInflate(context, attrs, savedInstanceState);
        TypedArray typedArray =
                context.obtainStyledAttributes(attrs, R.styleable.FeatureSwitchFragment);
        if (typedArray != null) {
            String type = typedArray.getString(R.styleable.FeatureSwitchFragment_feature_type);
            FeatureType featureType = Optional.ofNullable(type)
                    .map(t -> {
                        try {
                            return FeatureType.valueOf(t.toUpperCase(Locale.ROOT));
                        } catch (Exception e) {
                            return null;
                        }
                    })
                    .orElse(FeatureType.AMIIBO);
            Bundle bundle = new Bundle();
            bundle.putSerializable(TYPE, featureType);
            setArguments(bundle);

            typedArray.recycle();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (type) {
            case ACCELEROMETER:
                PreferenceUtils.setAccelerometerEnabled(getContext(), isChecked);
                break;
            case GYROSCOPE:
                PreferenceUtils.setGyroscopeEnabled(getContext(), isChecked);
                break;
            case HAPTIC_FEEDBACK:
                PreferenceUtils.setHapticFeedBackEnabled(getContext(), isChecked);
                break;
            case AMIIBO:
                if (!isChecked) {
                    PreferenceUtils.setAmiiboEnabled(getContext(), isChecked);
                    PreferenceUtils.removeAmiiboBytes(getContext());
                    break;
                } else if (!enabled) {
                    DeviceName.with(getContext()).request((info, error) -> {
                        if (Objects.nonNull(info)) {
                            String manufacturer = info.manufacturer;  // "Samsung"
                            // String name = info.marketName;            // "Galaxy S8+"
                            // String model = info.model;                // "SM-G955W"
                            // String codename = info.codename;          // "dream2qltecan"
                            // String deviceName = info.getName();       // "Galaxy S8+"

                            if (!"Samsung".equalsIgnoreCase(manufacturer)) {
                                showMtuSizeWarning();
                            } else {
                                showAmiiboExperimentalWarning();
                            }
                        } else {
                            showMtuSizeWarning();
                        }
                    });
                }
                break;
            default:
                return;
        }

        enabled = isChecked;
        if (Objects.nonNull(featureSwitchListener)) {
            featureSwitchListener.onChanged(enabled);
        }
    }

    public void showMtuSizeWarning() {
        Context context = getContext();
        if (Objects.isNull(context)) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.mtu_warning_title);
        builder.setMessage(R.string.mtu_warning_text);
        builder.setPositiveButton(R.string.continue_option, (dialog, i) ->
                showAmiiboExperimentalWarning());
        builder.setNegativeButton(android.R.string.cancel, (dialog, i) ->
                switchController.setChecked(false));
        builder.setOnCancelListener((dialogInterface -> switchController.setChecked(false)));
        builder.create().show();
    }

    public void showAmiiboExperimentalWarning() {
        Context context = getContext();
        if (Objects.isNull(context)) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.amiibo_experimental_title);
        builder.setMessage(R.string.amiibo_experimental_text);
        builder.setPositiveButton(R.string.continue_option, (dialog, i) -> {
            PreferenceUtils.setAmiiboEnabled(context, true);
            if (Objects.nonNull(featureSwitchListener)) {
                featureSwitchListener.onChanged(true);
            }
        });
        builder.setNegativeButton(android.R.string.cancel, (dialog, i) -> {
            switchController.setChecked(false);
            if (Objects.nonNull(featureSwitchListener)) {
                featureSwitchListener.onChanged(false);
            }
        });
        builder.setOnCancelListener((dialogInterface -> switchController.setChecked(false)));
        builder.create().show();
    }

    @Override
    public void reset() {
        switch (type) {
            case ACCELEROMETER:
                PreferenceUtils.removeAccelerometerEnabled(getContext());
                enabled = PreferenceUtils.getAccelerometerEnabled(getContext());
                break;
            case GYROSCOPE:
                PreferenceUtils.removeGyroscopeEnabled(getContext());
                enabled = PreferenceUtils.getGyroscopeEnabled(getContext());
                break;
            case HAPTIC_FEEDBACK:
                PreferenceUtils.removeHapticFeedbackEnabled(getContext());
                enabled = PreferenceUtils.getHapticFeedBackEnabled(getContext());
                break;
            case AMIIBO:
                PreferenceUtils.removeAmiiboEnabled(getContext());
                enabled = PreferenceUtils.getAmiiboEnabled(getContext());
                break;
            default:
                break;
        }
        if (Objects.nonNull(featureSwitchListener)) {
            featureSwitchListener.onChanged(enabled);
        }
        switchController.setChecked(enabled);
    }

    public interface FeatureSwitchListener {
        void onChanged(boolean set);
    }
}
