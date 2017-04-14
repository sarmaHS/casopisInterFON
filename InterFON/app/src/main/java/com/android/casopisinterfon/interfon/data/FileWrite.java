package com.android.casopisinterfon.interfon.data;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStreamWriter;

public class FileWrite {

    private final static String TAG = "FILE_WRITE_EXCEPTION";

    public void writeFile(Context context, String file, String data){
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(file, Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        } catch (IOException e) {
            Log.e(TAG, "File write failed: " + e.toString());
        }
    }
}
