package com.rdapps.gamepad;

import static com.rdapps.gamepad.ControllerActivity.CONTROLLER_TYPE;
import static com.rdapps.gamepad.ControllerActivity.CUSTOM_UI;
import static com.rdapps.gamepad.ControllerActivity.CUSTOM_UI_URL;
import static com.rdapps.gamepad.log.JoyConLog.log;
import static com.rdapps.gamepad.protocol.ControllerType.LEFT_JOYCON;
import static com.rdapps.gamepad.protocol.ControllerType.PRO_CONTROLLER;
import static com.rdapps.gamepad.protocol.ControllerType.RIGHT_JOYCON;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import com.rdapps.gamepad.customui.CustomUiClient;
import com.rdapps.gamepad.customui.CustomUiService;
import com.rdapps.gamepad.listview.CustomUiViewAdapter;
import com.rdapps.gamepad.model.CustomUiItem;
import com.rdapps.gamepad.protocol.ControllerType;
import com.rdapps.gamepad.sql.CustomUiDbHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomUiActivity extends AppCompatActivity implements Callback<List<CustomUiItem>>,
        AdapterView.OnItemClickListener {


    private static final String TAG = CustomUiActivity.class.getName();

    private CustomUiViewAdapter customUiViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_ui);

        ListView customUiView = findViewById(R.id.customUIList);
        customUiViewAdapter = new CustomUiViewAdapter(this);
        customUiView.setAdapter(customUiViewAdapter);
        customUiView.setClickable(true);
        customUiView.setOnItemClickListener(this);

        customUiViewAdapter.setItems(new ArrayList<>());

        CustomUiService customUiService = CustomUiClient.getService();
        Call<List<CustomUiItem>> customUis = customUiService.getCustomUis();
        customUis.enqueue(this);
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
    public void onResponse(Call<List<CustomUiItem>> call, Response<List<CustomUiItem>> response) {
        List<CustomUiItem> customUiItems = response.body();
        if (customUiItems != null) {
            List<CustomUiItem> allUis = new ArrayList<>(customUiItems);
            try (CustomUiDbHandler customUIDBHandler = new CustomUiDbHandler(this)) {
                allUis.addAll(customUIDBHandler.getCustomUis());
            }

            List<CustomUiItem> filtered = allUis.stream()
                    .filter(item -> item.getAppVersion() <= BuildConfig.VERSION_CODE)
                    .collect(Collectors.toList());
            customUiViewAdapter.setItems(filtered);
        }
        hideProgressBar();
    }

    @Override
    public void onFailure(Call<List<CustomUiItem>> call, Throwable t) {
        log(TAG, t.getMessage());
        hideProgressBar();
    }

    public void hideProgressBar() {
        findViewById(R.id.progressBar).setVisibility(View.GONE);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        CustomUiViewAdapter adapter = (CustomUiViewAdapter) parent.getAdapter();
        CustomUiItem item = adapter.getItem(position);
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
