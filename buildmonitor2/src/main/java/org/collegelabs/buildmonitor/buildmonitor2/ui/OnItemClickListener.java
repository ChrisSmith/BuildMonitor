package org.collegelabs.buildmonitor.buildmonitor2.ui;

import android.view.View;

public interface OnItemClickListener {
    void onClick(View v, int position);

    class NullOnItemClickListener implements OnItemClickListener{

        @Override
        public void onClick(View v, int position) {

        }
    }
}
