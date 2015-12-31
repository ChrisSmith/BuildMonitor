package org.collegelabs.buildmonitor.buildmonitor2.ui;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public interface OnItemLongClickListener {
    boolean onLongClick(View v, int position);

}
