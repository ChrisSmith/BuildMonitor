package org.collegelabs.buildmonitor.buildmonitor2.ui;

import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;

import org.collegelabs.buildmonitor.buildmonitor2.logs.LogSearchResults;

import java.util.ArrayList;

public abstract class SelectableRecyclerAdapter<VH extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<VH>
{

    private SparseBooleanArray selectedItems;
    private boolean _selectable;

    public SelectableRecyclerAdapter() {
        selectedItems = new SparseBooleanArray();
    }

    public void setIsSelectable(boolean selectable){
        _selectable = selectable;
        if(!_selectable){
            clearSelections();
        }
        notifyDataSetChanged();
    }

    public boolean isSelectable(){
        return _selectable;
    }

    public boolean isSelected(int position) { return selectedItems.get(position, false); }

    public boolean toggleSelection(int position) {
        boolean isSelected;
        if (selectedItems.get(position, false)) {
            selectedItems.delete(position);
            isSelected = false;
        } else {
            selectedItems.put(position, true);
            isSelected = true;
        }
        notifyItemChanged(position);
        return isSelected;
    }

    public void clearSelections() {
        int[] selection = getSelectedPositions();
        selectedItems.clear();
        for (int i : selection) {
            notifyItemChanged(i);
        }
    }

    public int getSelectedItemCount() {
        return selectedItems.size();
    }

    /**
     *
     * @return selected positions not ids
     */
    public int[] getSelectedPositions() {
        final int size = selectedItems.size();
        int[] selected = new int[size];
        for (int i = 0; i < size; ++i) {
            selected[i] = selectedItems.keyAt(i);
        }
        return selected;
    }
}