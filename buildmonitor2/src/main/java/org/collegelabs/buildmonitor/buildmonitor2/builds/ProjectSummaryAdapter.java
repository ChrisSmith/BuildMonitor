package org.collegelabs.buildmonitor.buildmonitor2.builds;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Checkable;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import org.collegelabs.buildmonitor.buildmonitor2.R;
import org.collegelabs.buildmonitor.buildmonitor2.ui.OnItemClickListener;
import org.collegelabs.buildmonitor.buildmonitor2.ui.OnItemLongClickListener;
import org.collegelabs.buildmonitor.buildmonitor2.ui.SelectableRecyclerAdapter;
import org.collegelabs.buildmonitor.buildmonitor2.util.TimeUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProjectSummaryAdapter extends SelectableRecyclerAdapter<ProjectSummaryAdapter.ViewHolder> {

    private final LayoutInflater _inflater;
    private final OnItemClickListener _clickListener;
    private final OnItemLongClickListener _longClickListener;
    private List<ProjectSummary> _items = new ArrayList<>();

    public ProjectSummaryAdapter(
            Context context,
            OnItemClickListener clickListener,
            OnItemLongClickListener longClickListener
    ) {
        _clickListener = clickListener;
        _longClickListener = longClickListener;
        _inflater = LayoutInflater.from(context);
        setHasStableIds(true);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = _inflater.inflate(R.layout.project_summary_rowitem, parent, false);
        return new ViewHolder(view, _clickListener, _longClickListener);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ProjectSummary item =  _items.get(position);

        holder.bind(item, isSelectable());
        ((Checkable)holder.itemView).setChecked(isSelected(position));
    }

    @Override
    public long getItemId(int position) {
        ProjectSummary item = _items.get(position);
        return item == null ? RecyclerView.NO_ID: item.sqliteBuildId;
    }

    @Override
    public int getItemCount() {
        return _items.size();
    }

    public void clear() {
        _items.clear();
        notifyDataSetChanged();
    }

    public void reset(List<ProjectSummary> items) {

        Collections.sort(items, (ProjectSummary lhs, ProjectSummary rhs) -> lhs.sqliteBuildId - rhs.sqliteBuildId);

        _items = items;
        notifyDataSetChanged();
    }

    public int[] getSelectedItemIds(){
        int[] positions = getSelectedPositions();
        int[] items = new int[positions.length];

        for(int i = 0; i < items.length; i++){
            items[i] = getItem(positions[i]).sqliteBuildId;
        }

        return items;
    }

    public ProjectSummary getItem(int position) {
        return _items.get(position);
    }

    static class ViewHolder extends SelectableViewHolder {
        @InjectView(R.id.project_summary_name) public TextView name;
        @InjectView(R.id.project_summary_status) public TextView status;
        @InjectView(R.id.project_summary_starttime) public TextView startTime;
        @InjectView(R.id.project_summary_checkmark) public ImageView checkmark;

        public ViewHolder(View view, OnItemClickListener clickListener, OnItemLongClickListener longClickListener) {
            super(view, clickListener, longClickListener);
            ButterKnife.inject(this, view);
        }

        public void bind(ProjectSummary item, boolean selectable){
            Context context = this.itemView.getContext();

            checkmark.setVisibility(selectable ? View.VISIBLE : View.GONE);

            name.setText(item.name);
            status.setText(item.isRunning ? "[Running] " + item.status + " " + item.percentageComplete +"%" : item.status.toString());
            startTime.setText(TimeUtil.human(item.startDate, false));

            if(item.status == BuildStatus.FailedToLoad){
                status.setTextColor(context.getColor(R.color.orange_stoke));
                status.setText(item.statusText);
            }
            else if(item.status == BuildStatus.Failure) {
                status.setTextColor(context.getColor(R.color.red_stroke));
            } else if (item.isRunning || item.status == BuildStatus.Loading){
                status.setTextColor(context.getColor(R.color.blue_stroke));
            } else {
                status.setTextColor(context.getColor(R.color.green_stroke));
            }
        }
    }
}
