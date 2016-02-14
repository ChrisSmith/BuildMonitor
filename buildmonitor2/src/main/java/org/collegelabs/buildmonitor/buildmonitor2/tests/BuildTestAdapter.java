package org.collegelabs.buildmonitor.buildmonitor2.tests;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.collegelabs.buildmonitor.buildmonitor2.R;
import org.collegelabs.buildmonitor.buildmonitor2.tc.models.BuildDetailsResponse;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class BuildTestAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final LayoutInflater _layoutInflater;
    private ArrayList<TestResult> _data = new ArrayList<>();
    private HeaderViewModel _headerViewModel = new HeaderViewModel();

    public BuildTestAdapter(Context context){
        _layoutInflater = LayoutInflater.from(context);
    }

    private int VIEW_TYPE_HEADER = 1;
    private int VIEW_TYPE_ROW = 2;

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if(viewType == VIEW_TYPE_ROW){
            View view = _layoutInflater.inflate(R.layout.buildtests_test_row, parent, false);
            return new ViewHolder(view);
        }else{
            View view = _layoutInflater.inflate(R.layout.buildtests_test_header, parent, false);
            return new HeaderViewHolder(view);
        }
    }

    @Override
    public long getItemId(int position) {
        if(isPositionHeader(position)){
            return RecyclerView.NO_ID;
        }

        return getItem(position).hashCode(); // ok?
    }

    public TestResult getItem(int position) {
        if (isPositionHeader(position)){
            return null;
        }
        return _data.get(position - 1);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if(isPositionHeader(position)){
            ((HeaderViewHolder)holder).bind(_headerViewModel);
        }
        else
        {
            TestResult item = getItem(position);
            ((ViewHolder)holder).bind(item);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (isPositionHeader(position)){
            return VIEW_TYPE_HEADER;
        }
        return VIEW_TYPE_ROW;

    }

    public void setHeader(HeaderViewModel header){
        _headerViewModel = header;
        notifyItemChanged(0);
    }

    private boolean isPositionHeader(int position) {
        return position == 0;
    }

    @Override
    public int getItemCount() {
        return _data.size() + 1;
    }

    public void replaceAll(ArrayList<TestResult> results){
        Collections.sort(results, ((lhs, rhs) -> {
            if(!lhs.Status.equals(rhs.Status)){
                return lhs.Status.compareTo(rhs.Status); // Failure first
            }

            return lhs.Name.compareTo(rhs.Name);
        }));

        _data = results;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        @InjectView(R.id.buildtests_test_row_name) public TextView textView;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.inject(this, view);
        }

        public void bind(TestResult testResult) {
            textView.setText(testResult.toString());
        }
    }

    public static SimpleDateFormat DateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static class HeaderViewHolder extends RecyclerView.ViewHolder
    {
        @InjectView(R.id.buildtests_test_header_agent) public TextView agent;
        @InjectView(R.id.buildtests_test_header_time) public TextView time;
        @InjectView(R.id.buildtests_test_header_status) public TextView status;
        @InjectView(R.id.buildtests_test_header_statusDetails) public TextView statusDetails;
        @InjectView(R.id.buildtests_test_header_viewlogs) public Button logsButton;

        public HeaderViewHolder(View view) {
            super(view);
            ButterKnife.inject(this, view);
        }

        public void bind(HeaderViewModel header) {

            BuildDetailsResponse details = header.BuildDetails;
            if(details == null){
                return;
            }

            agent.setText(details.agent.name);
            status.setText(details.status);
            statusDetails.setText(details.statusText);
            time.setText(DateFormat.format(details.startDate) + " " + DateFormat.format(details.finishDate));
            logsButton.setOnClickListener(header.onLogsClickListener);
        }
    }

    public static class HeaderViewModel {

        public BuildDetailsResponse BuildDetails;
        public View.OnClickListener onLogsClickListener;

        public HeaderViewModel() {}

        public HeaderViewModel(BuildDetailsResponse buildDetails, View.OnClickListener listener) {
            BuildDetails = buildDetails;
            onLogsClickListener = listener;
        }
    }
}
