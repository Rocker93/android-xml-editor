package com.pjv.koranlu2.documenteditor.activity;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.pjv.koranlu2.documenteditor.R;

/**
 * Creates and handles dialog that asks for file name
 */
public class FileNameDialog extends DialogFragment {
    private EditText mEditFileName;
    public final static int REQUEST_NEW_FILE = 1;
    public final static int REQUEST_SAVE_AS = 2;

    private void sendResult(int REQUEST_CODE) {
        FileNameListener activity = (FileNameListener) getActivity();
        activity.onFinishEditDialog(mEditFileName.getText().toString(), REQUEST_CODE);
    }


    public interface FileNameListener {
        void onFinishEditDialog(String fileName, int requestCode);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        builder.setTitle(getString(R.string.choose_file_name));
        final int requestCode = getArguments().getInt("REQUEST_CODE",1);

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(inflater.inflate(R.layout.dialog_file_name, null))
                // Add action buttons
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        mEditFileName = ((EditText)getDialog().findViewById(R.id.file_name));
                        sendResult(requestCode);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        FileNameDialog.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }

}
