package org.collegelabs.buildmonitor.buildmonitor2.builds;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import org.collegelabs.buildmonitor.buildmonitor2.R;
import org.collegelabs.buildmonitor.buildmonitor2.tc.models.Build;
import org.collegelabs.buildmonitor.buildmonitor2.util.TimeUtil;

import java.text.DateFormat;
import java.util.ArrayList;

/**
 */
public class BuildAdapter extends ArrayAdapter<Build> {

    private final LayoutInflater inflater;

    public BuildAdapter(Context context) {
        super(context, 0);
        inflater = LayoutInflater.from(context);
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        Build item = this.getItem(position);

        ViewHolder holder;
        if (view != null) {
            holder = (ViewHolder) view.getTag();
        } else {
            view = inflater.inflate(R.layout.build_rowitem, parent, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        }


        holder.status.setText(item.running ? "[Running] " + item.status + " " + item.percentageComplete +"%" : item.status);
        holder.startTime.setText(TimeUtil.human(item.startDate));

        Resources resources = view.getContext().getResources();

        BuildStatus status = item.status.equalsIgnoreCase("SUCCESS") ? BuildStatus.Success : BuildStatus.Failure;
        if(status == BuildStatus.Failure) {
            holder.status.setTextColor(resources.getColor(R.color.red_stroke));
        } else if(item.running){
            holder.status.setTextColor(resources.getColor(R.color.blue_stroke));
        } else {
            holder.status.setTextColor(resources.getColor(R.color.green_stroke));
        }

        return view;
    }

    static class ViewHolder {
        @InjectView(R.id.build_status) public TextView status;
        @InjectView(R.id.build_starttime) public TextView startTime;

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}
