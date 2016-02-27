package com.pjv.koranlu2.documenteditor.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.EditText;

import com.pjv.koranlu2.documenteditor.R;

/**
 * Attribute dialog handles single attribute editing
 */
public class AttributeDialog extends DialogFragment {
    private EditText mEditAttributeName;
    private EditText mEditAttributeValue;
    private int mPosition;

    private void sendResult() {
        AttributeEditListener activity = (AttributeEditListener) getActivity();
        activity.onFinishEditDialog(mEditAttributeName.getText().toString(), mEditAttributeValue.getText().toString(), mPosition);
       // this.dismiss();
    }

    public interface AttributeEditListener {
        void onFinishEditDialog(String attrName, String attrValue, int lastPosition);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        Bundle args = getArguments();
        mPosition = args.getInt("position",-1);

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(inflater.inflate(R.layout.dialog_attribute, null))
                // Add action buttons
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        mEditAttributeName = ((EditText)getDialog().findViewById(R.id.attr_name));
                        mEditAttributeValue = ((EditText)getDialog().findViewById(R.id.attr_value));
                        sendResult();

                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        AttributeDialog.this.getDialog().cancel();
                    }
                });


        return builder.create();
    }


}
