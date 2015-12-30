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
import org.collegelabs.buildmonitor.buildmonitor2.util.TimeUtil;

import java.text.DateFormat;
import java.util.ArrayList;

/**
 */
public class BuildAdapter extends ArrayAdapter<BuildViewModel> {

    private final LayoutInflater inflater;

    public BuildAdapter(Context context) {
        super(context, 0);
        inflater = LayoutInflater.from(context);
    }

    @Override
    public boolean isEmpty() {
        return super.isEmpty();
    }

    @Override
    public int getCount() {
        return super.getCount();
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? 0 : 1;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        BuildViewModel item = this.getItem(position);

        if(position == 0){
            // first item is the header
            view = view != null ? view : inflater.inflate(R.layout.main_listheader, parent, false);
            new HeaderViewHolder(view).Update(item);
            return view;
        }

        ViewHolder holder;
        if (view != null) {
            holder = (ViewHolder) view.getTag();
        } else {
            view = inflater.inflate(R.layout.build_rowitem, parent, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        }

        holder.status.setText(item.isRunning ? "[Running] " + item.status + " " + item.percentageComplete +"%" : item.status);
        holder.startTime.setText(TimeUtil.human(item.startDate));

        Resources resources = view.getContext().getResources();

        if(item.status.equals("NOPE.")) {
            holder.status.setTextColor(resources.getColor(R.color.red_stroke));
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

    static class HeaderViewHolder {
        @InjectView(R.id.main_textview_status) TextView _status;
        @InjectView(R.id.main_textview_summary) TextView _summary;

        public HeaderViewHolder(View view) {
            ButterKnife.inject(this, view);
        }

        public void Update(BuildViewModel viewModel){
            if(viewModel.isEmpty){
                _summary.setText("");
                _status.setText("Loading...");
            } else{
                _status.setText(viewModel.status);
                _summary.setText(viewModel.summary);
            }
        }
    }
}
