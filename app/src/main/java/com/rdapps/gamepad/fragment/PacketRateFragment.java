package com.rdapps.gamepad.fragment;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import com.rdapps.gamepad.R;
import com.rdapps.gamepad.util.PreferenceUtils;
import java.util.Objects;

public class PacketRateFragment extends Fragment
        implements ResettableSettingFragment, SeekBar.OnSeekBarChangeListener {

    private static String TAG = PacketRateFragment.class.getName();


    private TextView textView;
    protected SeekBar seekBar;

    public static PacketRateFragment getInstance() {
        PacketRateFragment packetRateFragment = new PacketRateFragment();
        Bundle bundle = new Bundle();
        packetRateFragment.setArguments(bundle);
        return packetRateFragment;
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.component_pps_setting, container, false);
        textView = view.findViewById(R.id.ppsTitle);
        seekBar = view.findViewById(R.id.ppsBar);
        seekBar.setOnSeekBarChangeListener(this);

        Context context = getContext();
        if (Objects.nonNull(context)) {
            int packetRate = PreferenceUtils.getPacketRate(context);
            textView.setText(context.getString(R.string.packet_rate, packetRate));
            seekBar.setProgress(rateToProgress(packetRate));
        }

        return view;
    }

    @Override
    public void onInflate(Context context, AttributeSet attrs, Bundle savedInstanceState) {
        super.onInflate(context, attrs, savedInstanceState);
    }

    @Override
    public void reset() {
        Context context = getContext();
        if (Objects.nonNull(context)) {
            PreferenceUtils.removePacketRate(context);
            int packetRate = PreferenceUtils.getPacketRate(context);
            textView.setText(context.getString(R.string.packet_rate, packetRate));
            seekBar.setProgress(rateToProgress(packetRate));
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        int packageRate = progressToRate(progress);
        Context context = getContext();
        if (Objects.nonNull(context)) {
            PreferenceUtils.setPacketRate(context, packageRate);
            textView.setText(context.getString(R.string.packet_rate, packageRate));
        }
    }

    private int progressToRate(int progress) {
        return progress + 1;
    }

    private int rateToProgress(int rate) {
        return rate - 1;
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }
}
