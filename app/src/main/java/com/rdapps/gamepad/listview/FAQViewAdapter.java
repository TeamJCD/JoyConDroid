package com.rdapps.gamepad.listview;


import android.content.Context;
import android.graphics.Typeface;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.rdapps.gamepad.R;

import static androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY;
import static androidx.core.text.HtmlCompat.fromHtml;

public class FAQViewAdapter extends BaseExpandableListAdapter {

    private Context context;
    private String[] questionList;
    private String[] answerList;

    public FAQViewAdapter(
            Context context,
            String[] questionList,
            String[] answerList) {
        this.context = context;
        this.questionList = questionList;
        this.answerList = answerList;
    }

    @Override
    public String getChild(int listPosition, int expandedListPosition) {
        return this.answerList[listPosition];
    }

    @Override
    public long getChildId(int listPosition, int expandedListPosition) {
        return expandedListPosition;
    }

    @Override
    public View getChildView(int listPosition, final int expandedListPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        final String expandedListText = getChild(listPosition, expandedListPosition);
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.list_answer, null);
        }
        TextView expandedListTextView = convertView
                .findViewById(R.id.expandedListItem);

        expandedListTextView.setText(fromHtml(expandedListText, FROM_HTML_MODE_LEGACY));
        expandedListTextView.setMovementMethod(LinkMovementMethod.getInstance());
        return convertView;
    }

    @Override
    public int getChildrenCount(int listPosition) {
        return 1;
    }

    @Override
    public String getGroup(int listPosition) {
        return this.questionList[listPosition];
    }

    @Override
    public int getGroupCount() {
        return this.questionList.length;
    }

    @Override
    public long getGroupId(int listPosition) {
        return listPosition;
    }

    @Override
    public View getGroupView(int listPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        String listTitle = getGroup(listPosition);
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.list_question, null);
        }
        TextView listTitleTextView = convertView
                .findViewById(R.id.listQuestion);
        listTitleTextView.setTypeface(null, Typeface.BOLD);
        listTitleTextView.setText(listTitle);
        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int listPosition, int expandedListPosition) {
        return true;
    }
}
