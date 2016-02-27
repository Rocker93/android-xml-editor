package com.pjv.koranlu2.documenteditor.adapter;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.BaseSwipeAdapter;
import com.pjv.koranlu2.documenteditor.R;
import com.pjv.koranlu2.documenteditor.activity.AttributeDialog;
import com.pjv.koranlu2.documenteditor.format.AttributeNode;

import java.util.List;

/**
 * Created by Luky on 17. 1. 2015.
 * Adapter for displaying attributes
 */
public class AttributeViewAdapter extends BaseSwipeAdapter {
    private static final String TAG = "AttributeViewAdapter";
    private final List<AttributeNode> mAttributeNodeList;
    private final Context mContext;

    public AttributeViewAdapter( Context mContext, List<AttributeNode> attributeNodeList) {
        this.mAttributeNodeList = attributeNodeList;
        this.mContext = mContext;
    }

    @Override
    public int getCount() {
        return mAttributeNodeList.size();
    }

    @Override
    public Object getItem(int position) {
        return mAttributeNodeList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getSwipeLayoutResourceId(int i) {
        return R.id.swipe_row;
    }

    @Override
    public View generateView(int position, ViewGroup viewGroup) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View v = inflater.inflate(R.layout.swipe_row, null);
        return v;
    }

    @Override
    public void fillValues(final int position, View convertView) {
        TextView textView= (TextView) convertView.findViewById(R.id.row_swipe_text);

        AttributeNode currentNode = mAttributeNodeList.get(position);
        convertView.findViewById(R.id.row_swipe_text).setOnClickListener(new AttributeChangeListener(position));
        convertView.findViewById(R.id.trash).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAttributeNodeList.remove(position);
                notifyDataSetChanged();
            }
        });
        ((SwipeLayout)convertView.findViewById(getSwipeLayoutResourceId(position))).close();

        SpannableStringBuilder spannableString = styleAttribute(currentNode);

        textView.setText(spannableString, TextView.BufferType.SPANNABLE);
    }

    private SpannableStringBuilder styleAttribute(AttributeNode currentNode) {
        SpannableStringBuilder attrName = new SpannableStringBuilder(currentNode.getNodeName());
        attrName.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(R.color.attribute_name)),0,attrName.length(),0);
        SpannableStringBuilder attrValue = new SpannableStringBuilder("\"" + currentNode.getNodeValue() + "\"");
        attrValue.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(R.color.attribute_value)),0,attrValue.length(),0);
        return new SpannableStringBuilder(TextUtils.concat(attrName, "=", attrValue));
    }

    /**
     * Listener For TreeElement Text Click
     */
    private class AttributeChangeListener implements SwipeLayout.OnClickListener {
        private int position;

        public AttributeChangeListener(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            AttributeDialog dialog = new AttributeDialog();
            Bundle bundle = new Bundle();
            bundle.putInt("position", position);
            dialog.setArguments(bundle);
            dialog.show(((Activity) mContext).getFragmentManager(), TAG);
            ((Activity) mContext).getFragmentManager().executePendingTransactions();

            EditText attrName = ((EditText) dialog.getDialog().findViewById(R.id.attr_name));
            EditText attrVal = ((EditText) dialog.getDialog().findViewById(R.id.attr_value));

            attrName.setText(mAttributeNodeList.get(position).getNodeName());
            attrName.setTextColor(mContext.getResources().getColor(R.color.attribute_name));
            attrName.setSelection(attrName.getText().length());

            attrVal.setText(mAttributeNodeList.get(position).getNodeValue());
            attrVal.setTextColor(mContext.getResources().getColor(R.color.attribute_value));

            InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        }

    }

}
