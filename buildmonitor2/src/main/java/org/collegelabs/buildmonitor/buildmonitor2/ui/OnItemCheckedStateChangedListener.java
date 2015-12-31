package org.collegelabs.buildmonitor.buildmonitor2.ui;

import android.view.ActionMode;

public interface OnItemCheckedStateChangedListener {
    void onItemCheckedStateChanged(ActionMode mode, int position, boolean checked);


    class NullOnItemCheckedStateChangedListener implements OnItemCheckedStateChangedListener {
        @Override
        public void onItemCheckedStateChanged(ActionMode mode, int position, boolean checked) {

        }
    }
}


