package com.rdapps.gamepad.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.rdapps.gamepad.R;
import com.rdapps.gamepad.util.PreferenceUtils;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;

public class FileSelectorFragment extends Fragment implements ResettableSettingFragment, View.OnClickListener {

    private static String TAG = FileSelectorFragment.class.getName();

    private static String TYPE = "TYPE";

    private static final int REQUEST_SELECT_FILE = 1;

    private TextView textView;

    public static FileSelectorFragment getInstance() {
        FileSelectorFragment fileSelectorFragment = new FileSelectorFragment();
        Bundle bundle = new Bundle();
        fileSelectorFragment.setArguments(bundle);
        return fileSelectorFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.component_file_selector, container, false);
        ((TextView) view.findViewById(R.id.title)).setText(R.string.amiibo_bin_path);
        textView = view.findViewById(R.id.selected_file_path);
        setAmiiboFilePathText();

        view.findViewById(R.id.select_button).setOnClickListener(this);
        return view;
    }

    @Override
    public void onInflate(Context context, AttributeSet attrs, Bundle savedInstanceState) {
        super.onInflate(context, attrs, savedInstanceState);
    }

    private void setAmiiboFilePathText() {
        Context context = getContext();
        if (Objects.isNull(context) || Objects.isNull(textView)) {
            return;
        }
        byte[] amiiboBytes = PreferenceUtils.getAmiiboBytes(context);
        if (Objects.nonNull(amiiboBytes)) {
            String amiiboFilePath = PreferenceUtils.getAmiiboFileName(context);
            textView.setText(amiiboFilePath);
        } else {
            textView.setText("");
        }
    }

    @Override
    public void reset() {
        Context context = getContext();
        if (Objects.isNull(context)) {
            return;
        }
        PreferenceUtils.removeAmiiboBytes(context);
        PreferenceUtils.removeAmiiboFileName(context);
        setAmiiboFilePathText();
    }

    protected void openFileSelectionDialog() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/octet-stream");

        startActivityForResult(intent, REQUEST_SELECT_FILE);
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SELECT_FILE && resultCode == RESULT_OK) {
            if (data != null) {
                Context context = getContext();
                Uri uri = data.getData();
                try (InputStream is = context.getContentResolver().openInputStream(uri)) {
                    byte[] bytes = IOUtils.toByteArray(is);
                    PreferenceUtils.setAmiiboFileName(context, uri);
                    PreferenceUtils.setAmiiboBytes(context, bytes);
                    showAmiiboFilePreset();
                    setAmiiboFilePathText();
                } catch (IOException e) {
                    showAmiiboFileCannotSet();
                    e.printStackTrace();
                }
            }
        }
    }

    public void showAmiiboFilePreset() {
        Context context = getContext();
        if (Objects.isNull(context)) {
            return;
        }
        Toast.makeText(context, R.string.amiibo_file_preset, Toast.LENGTH_LONG).show();
    }

    public void showAmiiboFileCannotSet() {
        Context context = getContext();
        if (Objects.isNull(context)) {
            return;
        }
        Toast.makeText(context, R.string.amiibo_file_cannot_be, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onClick(View v) {
        openFileSelectionDialog();
    }
}
