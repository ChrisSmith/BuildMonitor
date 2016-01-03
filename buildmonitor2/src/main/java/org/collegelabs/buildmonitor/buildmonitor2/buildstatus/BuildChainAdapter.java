package org.collegelabs.buildmonitor.buildmonitor2.buildstatus;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.collegelabs.buildmonitor.buildmonitor2.R;
import org.collegelabs.buildmonitor.buildmonitor2.builds.BuildStatus;
import org.collegelabs.buildmonitor.buildmonitor2.builds.SelectableViewHolder;
import org.collegelabs.buildmonitor.buildmonitor2.tc.models.BuildDetailsResponse;
import org.collegelabs.buildmonitor.buildmonitor2.ui.NullOnItemLongClickListener;
import org.collegelabs.buildmonitor.buildmonitor2.ui.OnItemClickListener;
import org.collegelabs.buildmonitor.buildmonitor2.ui.SelectableRecyclerAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class BuildChainAdapter extends SelectableRecyclerAdapter<BuildChainAdapter.ViewHolder> {

    private final LayoutInflater _inflater;
    private final OnItemClickListener _onItemClickListener;
    private List<BuildDetailsResponse> _builds = new ArrayList<>();

    public BuildChainAdapter(Context context, OnItemClickListener onItemClickListener){
        setHasStableIds(true);
        _inflater = LayoutInflater.from(context);
        _onItemClickListener = onItemClickListener;
    }

    public void addItem(BuildDetailsResponse response){
        _builds.add(response);
        notifyItemInserted(_builds.size() - 1);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = _inflater.inflate(R.layout.buildchain_rowitem, parent, false);
        return new ViewHolder(view, _onItemClickListener);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(_builds.get(position));
    }

    @Override
    public long getItemId(int position) {
        return _builds.get(position).buildId;
    }

    @Override
    public int getItemCount() {
        return _builds.size();
    }

    public BuildDetailsResponse getItem(int position) {
        return _builds.get(position);
    }


    public class ViewHolder extends SelectableViewHolder {

        @InjectView(R.id.buildchain_rowitem_buildname) TextView buildname;
        @InjectView(R.id.buildchain_rowitem_details) TextView details;
        @InjectView(R.id.buildchain_rowitem_status) TextView status;

        public ViewHolder(View view, OnItemClickListener clickListener){
            super(view, clickListener, new NullOnItemLongClickListener());
            ButterKnife.inject(this, view);
        }

        public void bind(BuildDetailsResponse response) {
            buildname.setText(response.buildType.name);
            status.setText(response.status);

            if(!response.status.equalsIgnoreCase(response.statusText)){
                details.setVisibility(View.VISIBLE);
                details.setText(response.statusText);
            }else{
                details.setVisibility(View.GONE);
            }

            BuildStatus buildStatus = response.status.equalsIgnoreCase("SUCCESS") ? BuildStatus.Success : BuildStatus.Failure;
            Context context = itemView.getContext();

            if(buildStatus == BuildStatus.Failure){
                status.setTextColor(context.getColor(R.color.red_stroke));
                details.setTextColor(context.getColor(R.color.red_stroke));
            } else if (response.running){
                status.setTextColor(context.getColor(R.color.blue_stroke));
                details.setTextColor(context.getColor(R.color.grey));
            } else {
                status.setTextColor(context.getColor(R.color.green_stroke));
                details.setTextColor(context.getColor(R.color.grey));
            }
        }
    }
}
