package org.collegelabs.buildmonitor.buildmonitor2.util;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import rx.functions.Func0;
import timber.log.Timber;

public class ActivityUtil {

    public static void openUrl(Context context, Func0<String> action){
        String url = "<unassigned>";
        try {
            url = action.call();
            if(url == null){
                return;
            }

            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (Exception e) {
            Timber.e(e, "Failed to open " + url);
        }
    }
}
