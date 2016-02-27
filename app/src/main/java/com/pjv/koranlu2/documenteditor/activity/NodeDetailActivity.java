package com.pjv.koranlu2.documenteditor.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.pjv.koranlu2.documenteditor.R;
import com.pjv.koranlu2.documenteditor.format.AttributeNode;
import com.pjv.koranlu2.documenteditor.format.TreeElement;
import com.pjv.koranlu2.documenteditor.format.TreeElementI;
import com.pjv.koranlu2.documenteditor.adapter.AttributeViewAdapter;
import com.pjv.koranlu2.documenteditor.parser.XMLChar;

import org.w3c.dom.Node;

/**
 * Activity that handles node content changes and attribute adding to elements
 */
public class NodeDetailActivity extends ActionBarActivity implements AttributeDialog.AttributeEditListener {
    private static final String TAG = "NodeDetailActivity";
    public static final String EXTRA_REQUEST = "requestCode";
    public static final String EXTRA_NODE = "treeNode";
    public static final String EXTRA_POSITION = "position";
    public static final String EXTRA_NODE_TYPE = "nodeType";
    public static final int REQUEST_EDIT = 1;
    public static final int REQUEST_CREATE = 2;
    private EditText nodeContent;
    private int listPosition;
    private ListView mAttributeListView;
    private AttributeViewAdapter mAdapter;
    private TreeElement treeElement;

    @Override
    public void onBackPressed() {
        cancelActivity();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_node_detail);
        mAttributeListView = (ListView) findViewById(R.id.attr_listView);
        nodeContent = (EditText) findViewById(R.id.edit_node);

        if (getIntent().getIntExtra(EXTRA_REQUEST,REQUEST_EDIT)==REQUEST_EDIT) {
            treeElement = (TreeElement) getIntent().getSerializableExtra(EXTRA_NODE);

            nodeContent.setText(treeElement.getNodeTitle());
            nodeContent.setSelection(nodeContent.getText().length());
      /*  InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(nodeContent, InputMethodManager.SHOW_IMPLICIT);*/
        } else if (getIntent().getIntExtra(EXTRA_REQUEST,REQUEST_EDIT)==REQUEST_CREATE) {
            TreeElementI parentElement = (TreeElement) getIntent().getSerializableExtra(EXTRA_NODE);
            int nodeId = getIntent().getIntExtra(EXTRA_NODE_TYPE,Node.ELEMENT_NODE);
            treeElement = new TreeElement("",nodeTypeConverter(nodeId));
            parentElement.addChild(treeElement);
        }
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        listPosition = getIntent().getIntExtra(EXTRA_POSITION,1);
        System.out.println(listPosition + "found NodeDetail");

        mAdapter = new AttributeViewAdapter(this, treeElement.getAttributes());
        mAttributeListView.setAdapter(mAdapter);

    }

    private short nodeTypeConverter(int nodeId) {
        switch (nodeId) {
            case R.id.add_element_node:
                return Node.ELEMENT_NODE;
            case R.id.add_text_node:
                return Node.TEXT_NODE;
            case R.id.add_comment_node:
                return Node.COMMENT_NODE;
            case R.id.add_cdata_node:
                return Node.CDATA_SECTION_NODE;
            case R.id.add_processing_node:
                return Node.PROCESSING_INSTRUCTION_NODE;
            default:
                throw new UnsupportedOperationException(getString(R.string.not_exist));
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_node_detail, menu);

        //hide add attribute menu option
        if (treeElement.getNodeType()!= Node.ELEMENT_NODE) {
            MenuItem menuItem = menu.findItem(R.id.action_add_attr);
            menuItem.setVisible(false);
            this.invalidateOptionsMenu();
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(nodeContent.getWindowToken(), 0);

        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_commit:
                String textContent = nodeContent.getText().toString();
                if (treeElement.getNodeType() == Node.ELEMENT_NODE) {
                    String msg;
                    if (textContent.replaceAll("\\s+", "").equals("")) {
                        Toast.makeText(this, getString(R.string.whitespace_error),Toast.LENGTH_SHORT).show();
                        return false;
                    } else if (!textContent.replaceAll("\\s+", "").equals(textContent.trim())) {
                        msg = getString(R.string.whitespace_error);
                        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                        nodeContent.setText(textContent.replaceAll("\\s+", ""));
                        treeElement.setNodeTitle(textContent.replaceAll("\\s+", ""));
                        textContent = textContent.replaceAll("\\s+", "");
                    }
                    if (XMLChar.isValidName(textContent.trim())) {
                        treeElement.setNodeTitle(textContent.trim());
                    } else {
                        msg = getString(R.string.illegal_chars);
                        Toast.makeText(this, msg,Toast.LENGTH_SHORT).show();
                        return false;
                    }
                } else {
                    treeElement.setNodeTitle(textContent.trim());
                }
                Intent resultIntent = new Intent().putExtra(EXTRA_NODE, treeElement);
                resultIntent.putExtra(EXTRA_POSITION, listPosition);
                setResult(RESULT_OK, resultIntent);
                finish();
                return true;
            case R.id.action_add_attr:
                AttributeDialog dialog = new AttributeDialog();
                Bundle bundle = new Bundle();
                bundle.putInt("position",-1);
                dialog.setArguments(bundle);
                dialog.show(this.getFragmentManager(),"attributeEdit");
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);

                //dialog.setPosition(-1);
                return true;
            case android.R.id.home:
                cancelActivity();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void cancelActivity() {
        Log.v(TAG, "home clicked");
        Intent canceledIntent = new Intent().putExtra(EXTRA_POSITION, listPosition);
        System.out.println(listPosition + "cancelled NodeDetail");
        setResult(RESULT_CANCELED, canceledIntent);
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();  // Always call the superclass method first
        Log.v(TAG, "onStop");
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(nodeContent.getWindowToken(), 0);
    }

    @Override
    public void onFinishEditDialog(String attrName, String attrValue, int pos) {
        Log.v(TAG,"onFinishEdit");

        if (attrName.replaceAll("\\s+", "").isEmpty()) {
            Toast.makeText(this,getString(R.string.attribute_empty), Toast.LENGTH_LONG).show();
            return;
        }

        if (attrName.contains(" ")) {
            attrName = attrName.replaceAll("\\s+", "");
            Toast.makeText(this,getString(R.string.attribute_whitespace), Toast.LENGTH_LONG).show();
        }

        for (int i = 0; i < treeElement.getAttributes().size(); i++) {
            AttributeNode node = treeElement.getAttributes().get(i);
            if ((node.getNodeName().equals(attrName))&&(pos!=i)) {
                Toast.makeText(this,getString(R.string.already_exists), Toast.LENGTH_LONG).show();
                return;
            }
        }

        if (pos == -1) {
            treeElement.getAttributes().add(new AttributeNode(attrName,attrValue));
        } else {
            treeElement.getAttributes().get(pos).setNodeName(attrName);
            treeElement.getAttributes().get(pos).setNodeValue(attrValue);
        }

        mAdapter.notifyDataSetChanged();
    }


}
