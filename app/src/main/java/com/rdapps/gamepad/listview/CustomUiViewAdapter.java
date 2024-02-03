package com.rdapps.gamepad.listview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.rdapps.gamepad.R;
import com.rdapps.gamepad.model.CustomUiItem;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CustomUiViewAdapter extends BaseAdapter {

    private final LayoutInflater layoutInflater;
    private List<CustomUiItem> customUiItems;


    public CustomUiViewAdapter(Context context) {
        this.customUiItems = new ArrayList<>();
        this.layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return customUiItems.size();
    }

    @Override
    public CustomUiItem getItem(int position) {
        return customUiItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (Objects.isNull(view)) {
            view = layoutInflater.inflate(R.layout.custom_ui_option_layout, parent, false);
        }

        CustomUiItem customUiItem = customUiItems.get(position);

        if (Objects.isNull(customUiItem)) {
            return view;
        }

        TextView nameView = view.findViewById(R.id.customUIName);
        nameView.setText(customUiItem.getName());

        int icon = R.drawable.ic_left_joycon_icon;
        if (Objects.nonNull(customUiItem.getType())) {
            icon = switch (customUiItem.getType()) {
                case RIGHT_JOYCON -> R.drawable.ic_right_joycon_icon;
                case LEFT_JOYCON -> R.drawable.ic_left_joycon_icon;
                case PRO_CONTROLLER -> R.drawable.ic_procontroller_icon;
            };
        }
        nameView.setCompoundDrawablesRelativeWithIntrinsicBounds(icon, 0, 0, 0);

        return view;
    }

    public void setItems(List<CustomUiItem> customUiItems) {
        this.customUiItems = customUiItems;
        notifyDataSetChanged();
    }
}
