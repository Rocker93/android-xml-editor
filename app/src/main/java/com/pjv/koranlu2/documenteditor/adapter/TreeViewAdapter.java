package com.pjv.koranlu2.documenteditor.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.pjv.koranlu2.documenteditor.R;
import com.pjv.koranlu2.documenteditor.format.AttributeNode;
import com.pjv.koranlu2.documenteditor.format.TreeElement;
import com.pjv.koranlu2.documenteditor.format.TreeElementI;

import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Luky on 11. 1. 2015.
 * Based on work by Jiberta, http://stackoverflow.com/questions/18717155/expandablelistview-like-treeview-android
 * Tree adapter representing XML tree
 */
public class TreeViewAdapter extends BaseAdapter {
    private static final int TREE_ELEMENT_PADDING_VAL = 25;
    public static final int UNCHECKED = -1;
    private static final String TAG = "treeViewAdapter";

    private final LayoutInflater mInflater;
    private final ArrayList<TreeElementI> mElementList;
    private final Context mContext;
    private final Bitmap iconCollapse;
    private final Bitmap iconExpand;

    /**
     * Checked item in menu, -1 if none
     */
    private int mChecked = UNCHECKED;

    public void setChecked(int checked) {
        this.mChecked = checked;
    }


    public TreeViewAdapter(Context mContext, ArrayList<TreeElementI> elementList ) {
        this.mContext = mContext;
        this.mElementList = elementList;

        iconCollapse = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_action_collapse);
        iconExpand = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_action_expand);

        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public List<TreeElementI> getListData() {
        return this.mElementList;
    }

    @Override
    public int getCount() {
        return this.mElementList.size();
    }

    @Override
    public Object getItem(int position) {
        return this.mElementList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View treeRow;
        ViewHolder holder;

        if (convertView != null) {
            treeRow = convertView;
            holder = (ViewHolder) treeRow.getTag();
        } else {
            treeRow = mInflater.inflate(R.layout.tree_row, null);
            holder = new ViewHolder();
            holder.setTextView((TextView) treeRow.findViewById(R.id.row_text));
            holder.setImageView((ImageView) treeRow.findViewById(R.id.row_image));
            treeRow.setTag(holder);
        }

        if (mChecked == position) {
            treeRow.setBackgroundColor(mContext.getResources().getColor(R.color.accent_material_light));
        } else {
            treeRow.setBackgroundColor(mContext.getResources().getColor(R.color.background_material_light));
        }

        holder.getIcon().setVisibility(View.VISIBLE);

        final TreeElementI elem = (TreeElementI) getItem(position);
        int level = elem.getLevel();

        styleNode(holder, elem, level);

        IconClickListener iconListener = new IconClickListener(this, position);
        holder.getIcon().setOnClickListener(iconListener);

        treeRow.setPadding(TREE_ELEMENT_PADDING_VAL * (level + 1),0,0,0);

        return treeRow;
    }

    private void styleNode(ViewHolder holder, TreeElementI elem, int level) {
      //  holder.getTextView().setPadding(TREE_ELEMENT_PADDING_VAL * (level + 1), holder.getTextView().getPaddingTop(), 0, holder.getTextView().getPaddingBottom());

        String holderText =
                ((elem.getNodeType()!= Node.TEXT_NODE && elem.getNodeType() != TreeElement.ROOT_ELEMENT) ? "<" : "") +
                (elem.getNodeType()==Node.COMMENT_NODE ? "!--" : "") +
                (elem.getNodeType()==Node.CDATA_SECTION_NODE ? "![CDATA[" : "") +
                (elem.getNodeType()==Node.PROCESSING_INSTRUCTION_NODE ? "?" : "") +
                (elem.getNodeType()==Node.DOCUMENT_TYPE_NODE ? "!DOCTYPE " : "") +
                 elem.getNodeTitle();

        SpannableStringBuilder spannableString = new SpannableStringBuilder(holderText);

        if (elem.getNodeType()==Node.ELEMENT_NODE) {
            spannableString.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(R.color.element_node)), 0, holderText.length(), 0);
          //  Log.v(TAG, holderText.length() + " ");
            for (AttributeNode currentNode : elem.getAttributes()) {
                spannableString = styleAttribute(spannableString, currentNode);
            }
        }

        //holder.getTextView().setText(holderText);

        if (elem.hasChild()) {
            if (elem.getNodeType()==Node.ELEMENT_NODE) {
                spannableString.append(">");
                spannableString.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(R.color.element_node)), spannableString.length()-1, spannableString.length(), 0);
            } else if (elem.getNodeType()==TreeElement.ROOT_ELEMENT)
                spannableString.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(R.color.root_node)), 0, spannableString.length(), 0);
            if (!elem.isExpanded()) {
                holder.getIcon().setImageBitmap(iconCollapse);
            } else if (elem.isExpanded()) {
                holder.getIcon().setImageBitmap(iconExpand);
            }
        } else if (!elem.hasChild()) {
            switch (elem.getNodeType()) {
                case Node.ELEMENT_NODE:
                    spannableString.append("/>");
                    spannableString.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(R.color.element_node)), spannableString.length()-2, spannableString.length(), 0);
                    break;
                case Node.COMMENT_NODE:
                    spannableString.append("-->");
                    break;
                case Node.CDATA_SECTION_NODE:
                    spannableString.append("]]>");
                    break;
                case Node.PROCESSING_INSTRUCTION_NODE:
                    spannableString.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(R.color.proccessing_node)), 0,spannableString.toString().indexOf(" "),0);
                    spannableString.append("?>");
                    spannableString.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(R.color.proccessing_node)), spannableString.length()-2, spannableString.length(), 0);
                    break;
                case Node.TEXT_NODE:
                    spannableString.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(R.color.text_node)), 0, spannableString.length(), 0);
                    break;
                case Node.DOCUMENT_TYPE_NODE:
                    spannableString.insert(spannableString.toString().trim().lastIndexOf(" ")+1,"\"");
                    spannableString.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(R.color.doctype_node)), 0,spannableString.toString().indexOf(" "),0);
                    spannableString.append("\">");
                    spannableString.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(R.color.doctype_node)), spannableString.length()-1, spannableString.length(), 0);
                    break;
            }

            holder.getIcon().setImageBitmap(iconCollapse);
            holder.getIcon().setVisibility(View.INVISIBLE);
        }

        holder.getTextView().setText(spannableString, TextView.BufferType.SPANNABLE);

    }

    private SpannableStringBuilder styleAttribute(SpannableStringBuilder targetString, AttributeNode currentNode) {
        SpannableStringBuilder attrName = new SpannableStringBuilder(currentNode.getNodeName());
        attrName.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(R.color.attribute_name)),0,attrName.length(),0);
        SpannableStringBuilder attrValue = new SpannableStringBuilder("\"" + currentNode.getNodeValue() + "\"");
        attrValue.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(R.color.attribute_value)),0,attrValue.length(),0);
        targetString = new SpannableStringBuilder(TextUtils.concat(targetString, " ", attrName, "=", attrValue));
        return targetString;
    }



    /**
     * ViewHolder pattern
     */
    private class ViewHolder {
        private ImageView icon;
        private TextView textView;

        public TextView getTextView() {
            return this.textView;
        }

        public void setTextView(TextView text) {
            this.textView = text;
        }

        public ImageView getIcon() {
            return this.icon;
        }

        public void setImageView(ImageView icon) {
            this.icon = icon;
        }
    }

    /**
     * Expands element on given position in element list
     */
    public void expandElement(int pos) {
        TreeElementI treeElement = mElementList.get(pos);
        if (!treeElement.isExpanded()) {
            treeElement.setExpanded(true);
            int level = treeElement.getLevel();
            int nextLevel = level + 1;

            for (TreeElementI element : treeElement.getChildList()) {
                element.setLevel(nextLevel);
                element.setExpanded(false);
                mElementList.add(pos + 1, element);
            }
            this.notifyDataSetChanged();
        }
    }

    public void collapseElement(int position) {
        mElementList.get(position).setExpanded(false);
        TreeElementI element = mElementList.get(position);
        ArrayList<TreeElementI> temp = new ArrayList<>();

        for (int i = position + 1; i < mElementList.size(); i++) {
            if (element.getLevel() >= mElementList.get(i).getLevel()) {
                break;
            }
            temp.add(mElementList.get(i));
        }
        mElementList.removeAll(temp);
        this.notifyDataSetChanged();
    }

    /**
     * Listener for TreeElement "Expand"/ "Collapse" button Click
     */
    private class IconClickListener implements View.OnClickListener {
        private ArrayList<TreeElementI> mList;
        private TreeViewAdapter mAdapter;
        private int position;

        public IconClickListener(TreeViewAdapter adapter, int position) {
            this.mList = (ArrayList<TreeElementI>) adapter.getListData();
            this.mAdapter = adapter;
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            if (!mList.get(position).hasChild()) {
                return;
            }
            if (mList.get(position).isExpanded()) {
                collapseElement(position);
            } else {
                expandElement(position);
            }
        }


    }
}