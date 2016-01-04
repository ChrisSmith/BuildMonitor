package org.collegelabs.buildmonitor.buildmonitor2.tests;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.collegelabs.buildmonitor.buildmonitor2.R;
import org.collegelabs.buildmonitor.buildmonitor2.builds.SelectableViewHolder;
import org.collegelabs.buildmonitor.buildmonitor2.ui.NullOnItemLongClickListener;
import org.collegelabs.buildmonitor.buildmonitor2.ui.OnItemClickListener;
import org.collegelabs.buildmonitor.buildmonitor2.ui.OnItemLongClickListener;
import org.collegelabs.buildmonitor.buildmonitor2.ui.SelectableRecyclerAdapter;

import java.util.ArrayList;
import java.util.Collections;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class BuildTestAdapter extends SelectableRecyclerAdapter<BuildTestAdapter.ViewHolder> {

    private final LayoutInflater _layoutInflater;
    private ArrayList<TestResult> _data = new ArrayList<>();

    public BuildTestAdapter(Context context){
        _layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = _layoutInflater.inflate(R.layout.buildtests_test_row, parent, false);
        return new ViewHolder(view, new OnItemClickListener.NullOnItemClickListener(), new NullOnItemLongClickListener());
    }

    //TODO stable ids????

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        TestResult item = _data.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return _data.size();
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

    public static class ViewHolder extends SelectableViewHolder{

        @InjectView(R.id.buildtests_test_row_name) public TextView textView;

        public ViewHolder(View view, OnItemClickListener clickListener, OnItemLongClickListener longClickListener) {
            super(view, clickListener, longClickListener);
            ButterKnife.inject(this, view);
        }

        public void bind(TestResult testResult) {
            textView.setText(testResult.toString());
        }
    }
}
