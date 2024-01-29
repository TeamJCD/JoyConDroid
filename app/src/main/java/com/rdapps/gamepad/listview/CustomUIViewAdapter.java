package com.rdapps.gamepad.listview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.rdapps.gamepad.R;
import com.rdapps.gamepad.model.CustomUIItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CustomUIViewAdapter extends BaseAdapter {

    private final LayoutInflater layoutInflater;
    private List<CustomUIItem> customUIItems;


    public CustomUIViewAdapter(Context aContext) {
        this.customUIItems = new ArrayList<>();
        this.layoutInflater = LayoutInflater.from(aContext);
    }

    @Override
    public int getCount() {
        return customUIItems.size();
    }

    @Override
    public CustomUIItem getItem(int position) {
        return customUIItems.get(position);
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

        CustomUIItem customUIItem = customUIItems.get(position);

        if (Objects.isNull(customUIItem)) {
            return view;
        }

        TextView nameView = view.findViewById(R.id.customUIName);
        nameView.setText(customUIItem.getName());

        int icon = R.drawable.ic_left_joycon_icon;
        if (Objects.nonNull(customUIItem.getType())) {
            icon = switch (customUIItem.getType()) {
                case RIGHT_JOYCON -> R.drawable.ic_right_joycon_icon;
                case LEFT_JOYCON -> R.drawable.ic_left_joycon_icon;
                case PRO_CONTROLLER -> R.drawable.ic_procontroller_icon;
            };
        }
        nameView.setCompoundDrawablesRelativeWithIntrinsicBounds(icon, 0, 0, 0);

        return view;
    }

    public void setItems(List<CustomUIItem> customUIItems) {
        this.customUIItems = customUIItems;
        notifyDataSetChanged();
    }
}
