package com.rdapps.gamepad.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.github.angads25.filepicker.view.FilePickerDialog;
import com.rdapps.gamepad.R;
import com.rdapps.gamepad.util.PreferenceUtils;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Objects;

public class FileSelectorFragment extends Fragment implements ResetableSettingFragment, View.OnClickListener {

    private static String TAG = FileSelectorFragment.class.getName();

    private static String TYPE = "TYPE";

    private TextView textView;

    protected FilePickerDialog dialog;

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
            String amiiboFilePath = PreferenceUtils.getAmiiboFilePath(context);
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
        PreferenceUtils.removeAmiiboFilePath(context);
        setAmiiboFilePathText();
    }

    protected void openFileSelectionDialog() {
        Context context = getContext();

        if ((null != dialog && dialog.isShowing()) || context == null) {
            //dialog.dismiss();
            return;
        }

        //Create a DialogProperties object.
        DialogProperties properties = new DialogProperties();
        String amiiboFilePath = PreferenceUtils.getAmiiboFilePath(context);
        if (Objects.nonNull(amiiboFilePath)) {
            File file = new File(amiiboFilePath);
            File folder = file.getParentFile();
            if (folder.exists() && folder.isDirectory()) {
                properties.root = folder;
            }
        } else {
            properties.root = Environment.getExternalStorageDirectory();
        }

        //Instantiate FilePickerDialog with Context and DialogProperties.
        dialog = new FilePickerDialog(context, properties);
        dialog.setTitle("Select a File");
        dialog.setPositiveBtnName("Select");
        dialog.setNegativeBtnName("Cancel");
        //properties.selection_mode = DialogConfigs.MULTI_MODE; // for multiple files
        properties.selection_mode = DialogConfigs.SINGLE_MODE; // for single file
        properties.selection_type = DialogConfigs.FILE_SELECT;

        //Method handle selected files.
        dialog.setDialogSelectionListener(this::onSelectedFilePaths);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setOnCancelListener(this::onFileSelectorCanceled);
        dialog.setOnDismissListener(this::onFileSelectorDismissed);

        dialog.show();
    }


    public void onSelectedFilePaths(String[] files) {
        Context context = getContext();
        if (Objects.isNull(context)) {
            return;
        }
        if (files.length > 0) {
            String file = files[0];
            try {
                byte[] bytes = IOUtils.toByteArray(new FileInputStream(file));
                PreferenceUtils.setAmiiboFilePath(context, file);
                PreferenceUtils.setAmiiboBytes(context, bytes);
                showAmiiboFilePreset();
                setAmiiboFilePathText();
            } catch (IOException e) {
                showAmiiboFileCannotSet();
                e.printStackTrace();
            }
        }
    }

    public void onFileSelectorCanceled(DialogInterface dialog) {
        reset();
        this.dialog = null;
    }

    public void onFileSelectorDismissed(DialogInterface dialog) {
        this.dialog = null;
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
