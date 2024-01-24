package com.rdapps.gamepad;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.rdapps.gamepad.customui.CustomUIClient;
import com.rdapps.gamepad.customui.CustomUIService;
import com.rdapps.gamepad.listview.CustomUIViewAdapter;
import com.rdapps.gamepad.model.CustomUIItem;
import com.rdapps.gamepad.protocol.ControllerType;
import com.rdapps.gamepad.sql.CustomUIDBHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.rdapps.gamepad.ControllerActivity.CONTROLLER_TYPE;
import static com.rdapps.gamepad.ControllerActivity.CUSTOM_UI;
import static com.rdapps.gamepad.ControllerActivity.CUSTOM_UI_URL;
import static com.rdapps.gamepad.log.JoyConLog.log;
import static com.rdapps.gamepad.protocol.ControllerType.LEFT_JOYCON;
import static com.rdapps.gamepad.protocol.ControllerType.PRO_CONTROLLER;
import static com.rdapps.gamepad.protocol.ControllerType.RIGHT_JOYCON;

public class CustomUIActivity extends AppCompatActivity implements Callback<List<CustomUIItem>>, AdapterView.OnItemClickListener {


    private static final String TAG = CustomUIActivity.class.getName();

    private CustomUIViewAdapter customUIViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_ui);

        ListView customUIView = findViewById(R.id.customUIList);
        customUIViewAdapter = new CustomUIViewAdapter(this);
        customUIView.setAdapter(customUIViewAdapter);
        customUIView.setClickable(true);
        customUIView.setOnItemClickListener(this);

        customUIViewAdapter.setItems(new ArrayList<>());

        CustomUIService customUIService = CustomUIClient.getService();
        Call<List<CustomUIItem>> customUIs = customUIService.getCustomUIs();
        customUIs.enqueue(this);
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        log(TAG, "Config Changed");
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onResponse(Call<List<CustomUIItem>> call, Response<List<CustomUIItem>> response) {
        List<CustomUIItem> customUIItems = response.body();
        if (customUIItems != null) {
            ArrayList<CustomUIItem> allUis = new ArrayList<>(customUIItems);
            allUis.addAll(new CustomUIDBHandler(this).getCustomUIs());

            List<CustomUIItem> filtered = allUis.stream()
                    .filter(item -> item.getAppVersion() <= BuildConfig.VERSION_CODE)
                    .collect(Collectors.toList());
            customUIViewAdapter.setItems(filtered);
        }
        hideProgressBar();
    }

    @Override
    public void onFailure(Call<List<CustomUIItem>> call, Throwable t) {
        log(TAG, t.getMessage());
        hideProgressBar();
    }

    public void hideProgressBar() {
        findViewById(R.id.progressBar).setVisibility(View.GONE);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        CustomUIViewAdapter adapter = (CustomUIViewAdapter) parent.getAdapter();
        CustomUIItem item = adapter.getItem(position);
        ControllerType type = item.getType();
        ControllerType controllerType = LEFT_JOYCON;
        if (type == ControllerType.RIGHT_JOYCON) {
            controllerType = RIGHT_JOYCON;
        } else if (type == ControllerType.PRO_CONTROLLER) {
            controllerType = PRO_CONTROLLER;
        }

        Intent intent = new Intent(this, ControllerActivity.class);
        intent.putExtra(CONTROLLER_TYPE, controllerType);
        intent.putExtra(CUSTOM_UI, true);
        intent.putExtra(CUSTOM_UI_URL, item.getUrl() + "?version=" + item.getVersion());
        startActivity(intent);
    }
}
