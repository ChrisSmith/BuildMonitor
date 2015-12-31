package org.collegelabs.buildmonitor.buildmonitor2.builds;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import org.collegelabs.buildmonitor.buildmonitor2.ui.OnItemClickListener;
import org.collegelabs.buildmonitor.buildmonitor2.ui.OnItemLongClickListener;

public abstract class SelectableViewHolder extends RecyclerView.ViewHolder
        implements View.OnLongClickListener, View.OnClickListener
{

    private final OnItemClickListener _clickListener;
    private final OnItemLongClickListener _longClickListener;

    public SelectableViewHolder(View view,
                                OnItemClickListener clickListener,
                                OnItemLongClickListener longClickListener) {
        super(view);
        _clickListener = clickListener;
        _longClickListener = longClickListener;
        view.setOnClickListener(this);
        view.setOnLongClickListener(this);
    }

    @Override
    public boolean onLongClick(View v) {
        return _longClickListener.onLongClick(v, getAdapterPosition());
    }

    @Override
    public void onClick(View v) {
        _clickListener.onClick(v, getAdapterPosition());
    }

}
