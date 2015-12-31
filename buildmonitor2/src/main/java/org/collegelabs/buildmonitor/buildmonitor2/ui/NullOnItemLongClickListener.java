package org.collegelabs.buildmonitor.buildmonitor2.ui;

import android.view.View;

public class NullOnItemLongClickListener implements OnItemLongClickListener{

    @Override
    public boolean onLongClick(View v, int position) {
        return false;
    }
}
