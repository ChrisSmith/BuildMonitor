package org.collegelabs.buildmonitor.buildmonitor2.builds;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import org.collegelabs.buildmonitor.buildmonitor2.R;
import org.collegelabs.buildmonitor.buildmonitor2.util.TimeUtil;

/**
 */
public class ProjectSummaryAdapter extends ArrayAdapter<ProjectSummary> {

    private final LayoutInflater inflater;

    public ProjectSummaryAdapter(Context context) {
        super(context, R.layout.project_summary_rowitem);
        inflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ProjectSummary item = this.getItem(position);

        ViewHolder holder;
        if (view != null) {
            holder = (ViewHolder) view.getTag();
        } else {
            view = inflater.inflate(R.layout.project_summary_rowitem, parent, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        }

        holder.name.setText(item.name);
        holder.status.setText(item.isRunning ? "[Running] " + item.status + " " + item.percentageComplete +"%" : item.status.toString());
        holder.startTime.setText(TimeUtil.human(item.startDate));

        Resources resources = view.getContext().getResources();

        if(item.status == BuildStatus.FailedToLoad){
            holder.status.setTextColor(resources.getColor(R.color.orange_stoke));
            holder.status.setText(item.statusText);
        }
        else if(item.status == BuildStatus.Failure) {
            holder.status.setTextColor(resources.getColor(R.color.red_stroke));
        } else if (item.isRunning || item.status == BuildStatus.Loading){
            holder.status.setTextColor(resources.getColor(R.color.blue_stroke));
        } else {
            holder.status.setTextColor(resources.getColor(R.color.green_stroke));
        }

        return view;
    }

    static class ViewHolder {
        @InjectView(R.id.project_summary_name) public TextView name;
        @InjectView(R.id.project_summary_status) public TextView status;
        @InjectView(R.id.project_summary_starttime) public TextView startTime;

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}
