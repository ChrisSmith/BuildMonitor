package org.collegelabs.buildmonitor.buildmonitor2.util;

import android.content.Context;

/**
 */
public class ToastUtil {
    public static void show(Context context, String text){
        android.widget.Toast.makeText(context, text, android.widget.Toast.LENGTH_SHORT).show();
    }

}
