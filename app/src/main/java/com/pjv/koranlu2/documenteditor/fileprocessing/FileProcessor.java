package com.pjv.koranlu2.documenteditor.fileprocessing;

import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.pjv.koranlu2.documenteditor.R;

import java.io.File;
import java.io.IOException;

/**
 * Created by Luky on 13. 1. 2015.
 * Handles file opening and creation
 */
public class FileProcessor {
    private final Context mContext;
    private String mFilePath;
    private static final String TAG = "FileProcessing";

    public File getCurrentFile() {
        return currentFile;
    }

    File currentFile;

    public FileProcessor(Context context, String filePath) {
        mContext = context;
        mFilePath = filePath;
    }

    public boolean processFile() {
        currentFile = new File(mFilePath);
        if (!currentFile.exists() || !currentFile.canRead()) {
            Log.v(TAG, "can not read");
            Toast.makeText(mContext,mContext.getString(R.string.file_not_read), Toast.LENGTH_LONG).show();
            return false;
        }
        if (currentFile.canRead())
            Log.v(TAG, "can read");

        String extension = MimeTypeMap.getFileExtensionFromUrl(mFilePath);
        if (extension != null) {
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            String type = mime.getMimeTypeFromExtension(extension);
            if ((type == null ) || (!type.equals("text/xml")))  {
                Toast.makeText(mContext,mContext.getString(R.string.file_not_read), Toast.LENGTH_LONG).show();
                return false;
            }
            return true;
        }

        return false;
    }


    public boolean createFile() {
        try {
            File dir = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS), "docEdit");
            if (!dir.mkdirs()) {
                Log.e(TAG, "Directory not created");
            }
            currentFile = new File(dir,mFilePath);
            System.out.println(currentFile.getPath());

            System.out.println(currentFile.getAbsolutePath());
            if (!currentFile.createNewFile()) {
                Toast.makeText(mContext, mContext.getString(R.string.file_exists), Toast.LENGTH_LONG).show();
                return false;
            }
            mFilePath = currentFile.getPath();
        } catch (IOException e) {
            Toast.makeText(mContext, mContext.getString(R.string.file_not_created), Toast.LENGTH_LONG).show();
        }
        return true;
    }


}
