package com.pjv.koranlu2.documenteditor.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.ipaulpro.afilechooser.utils.FileUtils;
import com.pjv.koranlu2.documenteditor.R;
import com.pjv.koranlu2.documenteditor.fileprocessing.FileProcessor;
import com.pjv.koranlu2.documenteditor.fileprocessing.ObjectSerializer;
import com.pjv.koranlu2.documenteditor.format.TreeElement;
import com.pjv.koranlu2.documenteditor.format.TreeElementI;
import com.pjv.koranlu2.documenteditor.adapter.TreeViewAdapter;
import com.pjv.koranlu2.documenteditor.parser.XMLParser;

import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;


/**
 * Main Activity handling XML tree display and edit
 */
public class MainActivity extends ActionBarActivity implements FileNameDialog.FileNameListener {

    private static final String TAG = "Main";
    private static final int OPEN_FILE = 1234;

    private ListView mListView;
    private TreeViewAdapter mAdapter;
    FileProcessor mFileProcessor;
    private final XMLParser mParser = new XMLParser();
    ArrayList<TreeElementI> mElementList = new ArrayList<>();
    private int lastPosition;
    private boolean isExpanded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(TAG, "onCreate");
        setContentView(R.layout.activity_main);
        mListView = (ListView) findViewById(R.id.tree_content);

        // Get the intent that started this activity
        Intent intent = getIntent();
        Uri data = intent.getData();

        if ((intent.getType() != null) && intent.getType().contains("text/") && intent.getType().contains("xml")) {
            mFileProcessor = new FileProcessor(this, data.getPath());
            if (!mFileProcessor.processFile()) {
                return;
            }
        }

        mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);

        mListView.setOnItemClickListener(new ItemClickListener(this));
        mListView.setMultiChoiceModeListener(new LongClickListener(this));
    }


    /**
     * Listener For TreeElement Item Click
     */
    private class ItemClickListener implements AdapterView.OnItemClickListener {
        private final Context mContext;

        public ItemClickListener(Context mContext) {
            this.mContext = mContext;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            TreeElement treeElement = (TreeElement) parent.getItemAtPosition(position);
            if (treeElement.getNodeType() == TreeElement.ROOT_ELEMENT) return;
            Intent intent = new Intent(mContext, NodeDetailActivity.class);
            //intent.putExtra(NodeDetailActivity.EXTRA_NODE,list.get(position).getNodeTitle());
            intent.putExtra(NodeDetailActivity.EXTRA_NODE, treeElement);
            intent.putExtra(NodeDetailActivity.EXTRA_POSITION, position);
            intent.putExtra("requestCode", NodeDetailActivity.REQUEST_EDIT);
            Log.v(TAG, "starting");
            ((Activity) mContext).startActivityForResult(intent, NodeDetailActivity.REQUEST_EDIT);
        }
    }

    /**
     * Listener for TreeElement Item LongClick
     */
    private class LongClickListener implements AbsListView.MultiChoiceModeListener {
        private final Context mContext;

        public LongClickListener(Context mContext) {
            this.mContext = mContext;
        }

        private TreeElementI checkedElement;
        private int checkedPosition;

        @Override
        public void onItemCheckedStateChanged(ActionMode mode, int position,
                                              long id, boolean checked) {
            // Here you can do something when items are selected/de-selected,
            // such as update the title in the CAB
            checkedElement = mElementList.get(position);
            mAdapter.setChecked(position);
            checkedPosition = position;
            if ((checkedElement.getNodeType()!=Node.ELEMENT_NODE)&&(checkedElement.getNodeType()!=TreeElement.ROOT_ELEMENT)) {
                mode.getMenu().findItem(R.id.action_add_ctx).setVisible(false);
            }
            if (checkedElement.getNodeType()==TreeElement.ROOT_ELEMENT) {
                mode.getMenu().findItem(R.id.action_copy_ctx).setVisible(false);
                mode.getMenu().findItem(R.id.action_delete_ctx).setVisible(false);
            }

        }


        /**
         * Context menu click handler
         */
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            // Respond to clicks on the actions in the CAB
            switch (item.getItemId()) {
                case R.id.action_add_ctx:
                    PopupMenu popup = new PopupMenu(mContext, ((Activity)mContext).findViewById(R.id.action_add_ctx));
                    MenuInflater inflater = popup.getMenuInflater();
                    popup.setOnMenuItemClickListener(new PopupMenuItemClickListener(mode));
                    inflater.inflate(R.menu.menu_node_selection, popup.getMenu());

                    checkRootMultiplicity(popup);
                 /*   if (checkedElement.hasText()) {
                        popup.getMenu().findItem(R.id.add_text_node).setVisible(false);
                    }*/
                    popup.show();
                    return true;
                case R.id.action_delete_ctx:
                    TreeElementI childToDel = mElementList.get(checkedPosition);
                    childToDel.getParent().deleteChild(childToDel);

                    ArrayList<TreeElementI> listToDel = new ArrayList<>();
                    listToDel.add(childToDel);
                    for (int i = checkedPosition + 1; i < mElementList.size(); i++) {
                        if (childToDel.getLevel() >= mElementList.get(i).getLevel()) {
                            break;
                        }
                        listToDel.add(mElementList.get(i));
                    }
                    mElementList.removeAll(listToDel);

                    mode.finish();
                    return true;
                case R.id.action_copy_ctx:
                    android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", checkedElement.getNodeTitle()
                            + ((checkedElement.getAttributes().isEmpty()) ? "" : " " + checkedElement.getAttributes().toString()));
                    clipboard.setPrimaryClip(clip);

                    Toast.makeText(getApplicationContext(), getString(R.string.text_copied), Toast.LENGTH_SHORT).show();
                    mode.finish();
                    return true;
                default:
                    return false;
            }
        }

        /**
         * Ensures only one root element is present
         * @param popup menu
         */
        private void checkRootMultiplicity(PopupMenu popup) {
            if (checkedElement.getNodeType()== TreeElement.ROOT_ELEMENT) {
                for (TreeElementI child : checkedElement.getChildList()) {
                    if (child.getNodeType() == Node.ELEMENT_NODE) {
                        popup.getMenu().findItem(R.id.add_element_node).setVisible(false);
                        return;
                    }
                }
            }
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate the menu for the CAB
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.menu_context_main, menu);
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            // Here you can make any necessary updates to the activity when
            // the CAB is removed. By default, selected items are deselected/unchecked.
            mAdapter.setChecked(TreeViewAdapter.UNCHECKED);
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            // Here you can perform updates to the CAB due to
            // an invalidate() request
            return false;
        }

        /**
         * Popup menu handler, contains element type for creation
         */
        private class PopupMenuItemClickListener implements PopupMenu.OnMenuItemClickListener{
            private final ActionMode actionMode;

            private PopupMenuItemClickListener(ActionMode actionMode) {
                this.actionMode = actionMode;
            }

            /**
             * Starts NodeDetailActivity for new element creation
             */
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                Intent intent = new Intent(mContext, NodeDetailActivity.class);
                intent.putExtra(NodeDetailActivity.EXTRA_NODE, checkedElement);
                intent.putExtra(NodeDetailActivity.EXTRA_POSITION, checkedPosition);
                intent.putExtra(NodeDetailActivity.EXTRA_NODE_TYPE, menuItem.getItemId());
                intent.putExtra("requestCode", NodeDetailActivity.REQUEST_CREATE);
                Log.v(TAG, "add node starting");
                startActivityForResult(intent, NodeDetailActivity.REQUEST_CREATE);
                actionMode.finish();
                return true;
            }
        }

    }

    /**
     * Main menu click handler
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveToFile();
                return true;
            case R.id.action_export_json:
                if (!mElementList.isEmpty()) {
                    Log.v(TAG, "not empty");
                    final Handler mHandler = new Handler() {
                        @Override
                        public void handleMessage(Message msg) {
                            String mString = (String) msg.obj;
                            Toast.makeText(getApplicationContext(), mString, Toast.LENGTH_SHORT).show();
                        }
                    };
                    Runnable saveHandler = new Runnable() {
                        @Override
                        public void run() {
                            Message msg = new Message();
                            if (mParser.exportToJson(mElementList.get(0))) {
                                msg.obj = getString(R.string.export_successful);
                            } else msg.obj = getString(R.string.export_error);
                            mHandler.sendMessage(msg);
                        }
                    };
                    Thread thread = new Thread(saveHandler);
                    thread.start();
                } else Log.v(TAG, "Empty");
                return true;
            case R.id.action_load:
                //load();
                // Create the ACTION_GET_CONTENT Intent
                Intent getContentIntent = FileUtils.createGetContentIntent("text/xml");

                Intent intent = Intent.createChooser(getContentIntent, getString(R.string.choose_file));
                startActivityForResult(intent, OPEN_FILE);
                return true;
            case R.id.action_expand_all:
                if (isExpanded) {
                    if (!mElementList.isEmpty()) {
                        ArrayList<TreeElementI> childList = mElementList.get(0).getChildList();
                        for (int i = 0; i < childList.size(); i++) {
                            if (childList.get(i).getNodeType()==Node.ELEMENT_NODE) {
                                mAdapter.collapseElement(i);
                                item.setTitle(getString(R.string.action_expand_all));
                                item.setIcon(R.drawable.ic_expand_more_white);
                                isExpanded = false;
                            }
                        }
                    }
                } else {
                    expandAll();
                    item.setTitle(getString(R.string.action_collapse_all));
                    item.setIcon(R.drawable.ic_collapse_white);
                    isExpanded=true;
                }
                return true;
            case R.id.action_about:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.about_message)
                        .setTitle(R.string.action_about);
                AlertDialog dialog = builder.create();
                dialog.show();
                return true;
            case R.id.action_new_file:
                FileNameDialog fileNameDialog = new FileNameDialog();
                Bundle args = new Bundle();
                args.putInt("REQUEST_CODE",FileNameDialog.REQUEST_NEW_FILE);
                fileNameDialog.setArguments(args);
                fileNameDialog.show(getFragmentManager(),"fileNameDialog");
                return true;
            case R.id.action_save_as:
                FileNameDialog newFileDialog = new FileNameDialog();
                Bundle arg = new Bundle();
                arg.putInt("REQUEST_CODE",FileNameDialog.REQUEST_SAVE_AS);
                newFileDialog.setArguments(arg);
                newFileDialog.show(getFragmentManager(),"fileNameDialog");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    /**
     * Saves to XML file
     */
    private void saveToFile() {
        if (!mElementList.isEmpty()) {
            Log.v(TAG, "not empty");
            final Handler mHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    String mString = (String) msg.obj;
                    Toast.makeText(getApplicationContext(), mString, Toast.LENGTH_SHORT).show();
                }
            };
            Runnable saveHandler = new Runnable() {
                @Override
                public void run() {
                    Message msg = new Message();
                    if (mParser.saveToFile(mElementList.get(0))) {
                        msg.obj = getString(R.string.saved_succesfully);
                    } else {
                        msg.obj = getString(R.string.save_error);
                    }
                    mHandler.sendMessage(msg);
                }
            };
            Thread thread = new Thread(saveHandler);
            thread.start();

        } else Log.v(TAG, "Empty");
    }

    @Override
    public void onFinishEditDialog(String fileName, int requestCode) {
        fileName += ".xml";
        FileProcessor newFileProcessor = new FileProcessor(this, fileName);
        if (!newFileProcessor.createFile()) return;
        mFileProcessor = newFileProcessor;
        String name = mFileProcessor.getCurrentFile().getName();
        getSupportActionBar().setTitle(getString(R.string.app_name) + " - " + name);
        mParser.setFile(mFileProcessor.getCurrentFile());
        if (requestCode == FileNameDialog.REQUEST_NEW_FILE) {
            mElementList.clear();
            mElementList.add(0, new TreeElement(name, TreeElement.ROOT_ELEMENT));
            mAdapter = new TreeViewAdapter(this, mElementList);
            mListView.setAdapter(mAdapter);
        } else {
            mElementList.get(0).setNodeTitle(mFileProcessor.getCurrentFile().getName());
            mAdapter.notifyDataSetChanged();
            saveToFile();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.v(TAG, "activity result");

        if (resultCode == RESULT_CANCELED) {
            if ((requestCode == NodeDetailActivity.REQUEST_CREATE) || (requestCode == NodeDetailActivity.REQUEST_EDIT)) {
                lastPosition = data.getIntExtra(NodeDetailActivity.EXTRA_POSITION, 1);
            }
            Log.v(TAG, "Canceled result");
           // loadFromPreferences();
            saveToPreferences();
        } else if (resultCode == RESULT_OK) {
            if (requestCode == OPEN_FILE) {
                final Uri uri = data.getData();

                // Get the File path from the Uri
                String path = FileUtils.getPath(this, uri);
                FileProcessor newProcessor = new FileProcessor(this,path);
                if (!newProcessor.processFile()) {
                    return;
                }
                mFileProcessor = newProcessor;
                getSupportActionBar().setTitle(getString(R.string.app_name) + " - " + mFileProcessor.getCurrentFile().getName());
                loadFile();

            } else {
                lastPosition = data.getIntExtra(NodeDetailActivity.EXTRA_POSITION, 1);
                if (requestCode == NodeDetailActivity.REQUEST_EDIT) {
                    TreeElement updatedElement = (TreeElement) data.getSerializableExtra(NodeDetailActivity.EXTRA_NODE);

                    mElementList.get(lastPosition).update(updatedElement);
                    mElementList.set(lastPosition, updatedElement);

                    mAdapter.notifyDataSetChanged();
                    saveToPreferences();
                    mElementList = null;
                } else if (requestCode == NodeDetailActivity.REQUEST_CREATE) {
                    TreeElement createdElement = (TreeElement) data.getSerializableExtra(NodeDetailActivity.EXTRA_NODE);
                    if (createdElement.getNodeType() == Node.TEXT_NODE) {
                        mElementList.get(lastPosition).addChild(createdElement);
                        if (mElementList.get(lastPosition).isExpanded())
                            mElementList.add(++lastPosition, createdElement);
                        else mAdapter.expandElement(lastPosition++);
                    } else {
                        mElementList.get(lastPosition).addFirstChild(createdElement);
                        if (mElementList.get(lastPosition).isExpanded()) {
                            int pos = lastPosition + 1;
                            while (pos < mElementList.size()) {
                                if (createdElement.getLevel() > mElementList.get(pos).getLevel()) {
                                    break;
                                }
                                pos++;
                            }
                            mElementList.add(pos, createdElement);
                            lastPosition = pos;
                        } else {
                            mAdapter.expandElement(lastPosition);
                        }
                    }

                    mAdapter.notifyDataSetChanged();
                    saveToPreferences();
                }
            }
        }
    }

    /**
     * Save currently edited file to preferences
     */
    void saveToPreferences() {
        //save the mElementList to preferences
        SharedPreferences prefs = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        try {
            editor.putString("TREE", ObjectSerializer.serialize(mElementList));
            editor.putInt("LASTPOSITION", lastPosition);
            if (mFileProcessor != null)
                editor.putString("LASTFILE", mFileProcessor.getCurrentFile().getPath() );
            else
                editor.putString("LASTFILE", "");
         /*   FileOutputStream fos = openFileOutput("ProgressFile",Context.MODE_PRIVATE);
            ObjectOutputStream oos= new ObjectOutputStream(fos);
            oos.writeObject(mElementList);
            oos.close();
            fos.close();*/
        } catch (IOException e) {
            Log.e(TAG, "Serialization error", e);
        }
        editor.apply();

    }

    /**
     * Initializes load operation
     */
    private void load() {
        SharedPreferences prefs = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        //load objects from preferences
        String lastFile = prefs.getString("LASTFILE", "");

        if (mFileProcessor != null) { //rotation
            if (lastFile.equals(mFileProcessor.getCurrentFile().getPath())) {
                loadFromPreferences();
            } else {
                loadFile();
            }
            getSupportActionBar().setTitle(getString(R.string.app_name) + " - " + mFileProcessor.getCurrentFile().getName());
        } else {
            if (!lastFile.equals("")) {
                mFileProcessor = new FileProcessor(this, lastFile);
                if (!mFileProcessor.processFile()) {
                    return;
                }
                if (lastFile.equals(mFileProcessor.getCurrentFile().getPath())) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);

                    builder.setMessage(R.string.file_changed_message)
                            .setTitle(R.string.change_file);
                    builder.setPositiveButton(R.string.load_prefs, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            mParser.setFile(mFileProcessor.getCurrentFile());
                            loadFromPreferences();
                        }
                    });
                    builder.setNegativeButton(R.string.reload_file, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            loadFile();
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                else
                    loadFile();
                getSupportActionBar().setTitle(getString(R.string.app_name) + " - " + mFileProcessor.getCurrentFile().getName());
            }
        }
    }

    /**
     * Loads document from shared preferences
     */
    @SuppressWarnings("unchecked")
    private void loadFromPreferences() {
        SharedPreferences prefs = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        try {
             /*   FileInputStream fis = openFileInput("ProgressFile");
                ObjectInputStream ois = new ObjectInputStream(fis);
                mElementList = (ArrayList) ois.readObject();
                ois.close();
                fis.close();*/
            mElementList = (ArrayList<TreeElementI>) ObjectSerializer.deserialize(prefs.getString("TREE", ObjectSerializer.serialize(new ArrayList<TreeElementI>())));
            lastPosition = prefs.getInt("LASTPOSITION",1);

            mAdapter = new TreeViewAdapter(this, mElementList);
            mListView.setAdapter(mAdapter);
            if (!mElementList.isEmpty()) {
                mAdapter.expandElement(0);
            }
        } catch (IOException e) {
            Log.e(TAG, "Deserialization error", e);
        }
    }

    /**
     * Loads and parses document from file
     */
    private void loadFile() {
        try {
            Log.v(TAG, "Loading file");
            //mParser = new XmlParser(mFileProcessor.getCurrentFile());
            mParser.setFile(mFileProcessor.getCurrentFile());
            TreeElement rootElement = mParser.parse();

            mElementList = new ArrayList<>();
            mElementList.add(rootElement);
            if (mElementList!=null)
                mAdapter = new TreeViewAdapter(this,mElementList);
            mListView.setAdapter(mAdapter);
            mAdapter.expandElement(0);
           // expandAll();
        } catch (IOException | SAXException | ParserConfigurationException e) {
            Toast.makeText(getApplicationContext(), R.string.file_not_read, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Cannot be opened", e);
        }
    }

    /**
     * Expands whole document
     */
    private void expandAll() {
        int pos = 0;
        while (pos<mElementList.size()) {
            mAdapter.expandElement(pos++);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();  // Always call the superclass method first
        Log.v(TAG, "onResume");
        //takes to much time to scroll longer
        if (lastPosition<50) {
            mListView.post(new Runnable() {
                @Override
                public void run() {
                    mListView.smoothScrollToPosition(lastPosition);
                }
            });
        } else {
            mListView.setSelection(lastPosition);
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveToPreferences();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.v(TAG, "OnRestore");
        load();
    }


    @Override
    protected void onStop() {
        super.onStop();  // Always call the superclass method first
        Log.v(TAG, "onStop");
        //Save the current draft, because the activity is stopping
        //and we want to be sure the current progress isn't lost.
        saveToPreferences();
    }

    @Override
    protected void onStart() {
        super.onStart();  // Always call the superclass method first
        Log.v(TAG, "onStart");
        load();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();  // Always call the superclass method first
        Log.v(TAG, "onDestroy");
    }

    @Override
    protected void onRestart() {
        super.onRestart();  // Always call the superclass method first
        Log.v(TAG, "onRestart");
    }
}
