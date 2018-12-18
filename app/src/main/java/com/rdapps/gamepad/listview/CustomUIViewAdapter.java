package com.rdapps.gamepad.listview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.rdapps.gamepad.R;
import com.rdapps.gamepad.model.CustomUIItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CustomUIViewAdapter extends BaseAdapter {

    private LayoutInflater layoutInflater;
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
        if (Objects.isNull(convertView)) {
            convertView = layoutInflater.inflate(R.layout.custom_ui_option_layout, parent, false);
        }

        CustomUIItem customUIItem = customUIItems.get(position);

        if (Objects.isNull(customUIItem)) {
            return convertView;
        }

        TextView nameView = convertView.findViewById(R.id.customUIName);
        nameView.setText(customUIItem.getName());

        ImageView imageView = convertView.findViewById(R.id.customUIIcon);

        int icon = R.drawable.ic_left_joycon_icon_black;
        if (Objects.nonNull(customUIItem.getType())) {
            switch (customUIItem.getType()) {
                case RIGHT_JOYCON:
                    icon = R.drawable.ic_right_joycon_icon_black;
                    break;
                case LEFT_JOYCON:
                    icon = R.drawable.ic_left_joycon_icon_black;
                    break;
                case PRO_CONTROLLER:
                    icon = R.drawable.ic_procontroller_icon_black;
                    break;
            }
        }
        imageView.setImageResource(icon);

        return convertView;
    }

    public void setItems(List<CustomUIItem> customUIItems) {
        this.customUIItems = customUIItems;
        notifyDataSetChanged();
    }
}
