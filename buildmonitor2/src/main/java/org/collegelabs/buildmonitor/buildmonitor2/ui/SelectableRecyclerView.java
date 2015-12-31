package org.collegelabs.buildmonitor.buildmonitor2.ui;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class SelectableRecyclerView extends RecyclerView
        implements OnItemClickListener,
        OnItemLongClickListener {

    private ActionModeCallbackWrapper _actionModeCallback = new ActionModeCallbackWrapper();
    private OnItemClickListener _onClickListener = new NullOnItemClickListener();
    private OnItemCheckedStateChangedListener _onItemCheckedStateChangedListener = new OnItemCheckedStateChangedListener.NullOnItemCheckedStateChangedListener();
    private ActionMode _actionMode;

    public SelectableRecyclerView(Context context) {
        super(context);
    }

    public SelectableRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public SelectableRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setActionModeCallback(ActionMode.Callback callback){
        _actionModeCallback = new ActionModeCallbackWrapper(callback);
    }

    public void setOnItemClickListener(OnItemClickListener onClickListener){
        _onClickListener = onClickListener;
    }

    public void setOnItemCheckedStateChangedListener(OnItemCheckedStateChangedListener listener){
        _onItemCheckedStateChangedListener = listener;
    }

    @Override
    public void setAdapter(Adapter adapter) {
        super.setAdapter((SelectableRecyclerAdapter)adapter);
    }

    public SelectableRecyclerAdapter getSelectableAdapter(){
        return (SelectableRecyclerAdapter) getAdapter();
    }


    @Override
    public void onClick(View v, int position) {

        if(isInSelectMode()){
            toggleSelection(position);
        }else{
            _onClickListener.onClick(v, position);
        }
    }

    private boolean isInSelectMode() {
        return _actionMode != null;
    }

    @Override
    public boolean onLongClick(View v, int position) {
        startEditMode();
        toggleSelection(position);

        return true;
    }

    public void startEditMode() {
        if(_actionMode == null){
            _actionMode = startActionMode(_actionModeCallback);

            SelectableRecyclerAdapter adapter = getSelectableAdapter();
            adapter.setIsSelectable(true);
        }
    }

    private void toggleSelection(int position){
        SelectableRecyclerAdapter adapter = getSelectableAdapter();

        boolean checked = adapter.toggleSelection(position);
        _onItemCheckedStateChangedListener.onItemCheckedStateChanged(_actionMode, position, checked);

        if(adapter.getSelectedItemCount() == 0){
            _actionMode.finish();
        }
    }

    private class ActionModeCallbackWrapper implements ActionMode.Callback {

        private final ActionMode.Callback _callback;

        public ActionModeCallbackWrapper(){
            this(new NullActionModeCallback());
        }

        public ActionModeCallbackWrapper(ActionMode.Callback callback){
            _callback = callback;
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            return _callback.onCreateActionMode(mode, menu);
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return _callback.onPrepareActionMode(mode, menu);
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            return _callback.onActionItemClicked(mode, item);
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            _callback.onDestroyActionMode(mode);
            getSelectableAdapter().setIsSelectable(false);
            _actionMode = null;
        }
    }

}
