package org.collegelabs.buildmonitor.buildmonitor2.buildstatus;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
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

public class BuildChainAdapter extends SelectableRecyclerAdapter<RecyclerView.ViewHolder> {

    private final LayoutInflater _inflater;
    private final OnItemClickListener _onItemClickListener;
    private List<BuildDetailsResponse> _builds = new ArrayList<>();
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    private BuildStatusViewModel _statusViewModel;

    public BuildChainAdapter(Context context, OnItemClickListener onItemClickListener){
        setHasStableIds(true);
        _inflater = LayoutInflater.from(context);
        _onItemClickListener = onItemClickListener;
    }

    public void addItem(BuildDetailsResponse response){
        _builds.add(response);
        notifyItemInserted(_builds.size());
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if(viewType == TYPE_HEADER){
            View view = _inflater.inflate(R.layout.buildchain_header, parent, false);
            return new HeaderViewHolder(view);
        }else{
            View view = _inflater.inflate(R.layout.buildchain_rowitem, parent, false);
            return new ViewHolder(view, _onItemClickListener);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if(isPositionHeader(position)){
            ((HeaderViewHolder)holder).bind(_statusViewModel);
        }else{
            ((ViewHolder)holder).bind(getItem(position));
        }
    }

    @Override
    public long getItemId(int position) {
        if(isPositionHeader(position)){
            return RecyclerView.NO_ID;
        }

        return getItem(position).buildId;
    }

    @Override
    public int getItemCount() {
        return _builds.size() + 1;
    }

    public BuildDetailsResponse getItem(int position) {
        if (isPositionHeader(position)){
            return null;
        }
        return _builds.get(position - 1);
    }

    @Override
    public int getItemViewType(int position) {
        if (isPositionHeader(position)){
            return TYPE_HEADER;
        }
        return TYPE_ITEM;
    }

    private boolean isPositionHeader(int position) {
        return position == 0;
    }

    public void setStatusViewModel(BuildStatusViewModel statusViewModel) {
        _statusViewModel = statusViewModel;
        notifyItemChanged(0);
    }

    public class HeaderViewHolder extends RecyclerView.ViewHolder {

        @InjectView(R.id.buildchain_header_name) TextView buildname;
        @InjectView(R.id.buildchain_header_details) TextView details;
        @InjectView(R.id.buildchain_header_status) TextView status;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);
        }

        public void bind(BuildStatusViewModel viewModel){
            Context context = itemView.getContext();
            BuildStatus buildStatus = viewModel.Status;

            buildname.setText(viewModel.Name);
            status.setText(buildStatus.toString());
            details.setText(viewModel.getFailedDeps());

            if(buildStatus == BuildStatus.Failure){
                status.setTextColor(context.getColor(R.color.red_stroke));
                details.setTextColor(context.getColor(R.color.red_stroke));
            } else if (viewModel.IsRunning || buildStatus == BuildStatus.Loading){
                status.setTextColor(context.getColor(R.color.blue_stroke));
                details.setTextColor(context.getColor(R.color.grey));
            } else {
                status.setTextColor(context.getColor(R.color.green_stroke));
                details.setTextColor(context.getColor(R.color.grey));
            }
        }
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
