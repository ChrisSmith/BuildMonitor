package org.collegelabs.buildmonitor.buildmonitor2.buildhistory;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import org.collegelabs.buildmonitor.buildmonitor2.R;
import org.collegelabs.buildmonitor.buildmonitor2.builds.BuildStatus;
import org.collegelabs.buildmonitor.buildmonitor2.tc.models.Build;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import timber.log.Timber;

public class BuildHistoryHeader implements OnChartValueSelectedListener {

    private final Context _context;
    private final OnChartSelectedListener _onChartSelectedListener;
    @InjectView(R.id.build_history_chart) public BarChart chart;
    @InjectView(R.id.build_history_projectname) public TextView projectName;
    @InjectView(R.id.build_history_name) public TextView buildName;

    @InjectView(R.id.build_history_morestats) Button editCredentialsButton;

    private final View _view;

    public static SimpleDateFormat DateFormat = new SimpleDateFormat("yyyy-MM-dd");

    private ArrayList<String> _xvals;
    private ArrayList<String> _yvals;

    public BuildHistoryHeader(final Context context, final OnChartSelectedListener onChartSelectedListener, final int sqliteBuildId){
        _context = context;
        _onChartSelectedListener = onChartSelectedListener;
        _view = LayoutInflater.from(_context).inflate(R.layout.build_history_header, null);
        ButterKnife.inject(this, _view);
        customizeChart();

        editCredentialsButton.setOnClickListener(c -> context.startActivity(BuildStatisticsActivity.getIntent(context, sqliteBuildId)));
    }

    public View getView(){
        return _view;
    }

    public void updateTitles(String projectName, String buildName){
        this.buildName.setText(buildName);
        this.projectName.setText(projectName);
    }

    private void customizeChart() {
        chart.setDrawValueAboveBar(false);
        chart.setDescription("");
        chart.setGridBackgroundColor(_context.getColor(R.color.white));
        chart.setDoubleTapToZoomEnabled(false);
        chart.setPinchZoom(false);
        chart.getAxisRight().setEnabled(false);
        chart.setOnChartValueSelectedListener(this);

        XAxis xAxis = chart.getXAxis();
        xAxis.setGridColor(_context.getColor(R.color.grey));
        xAxis.setGridLineWidth(1.1f);

        YAxis yAxis = chart.getAxis(YAxis.AxisDependency.LEFT);
        yAxis.setGridColor(_context.getColor(R.color.grey));
        yAxis.setGridLineWidth(1.1f);

        Legend l = chart.getLegend();
        l.setPosition(Legend.LegendPosition.BELOW_CHART_RIGHT);
        l.setFormSize(8f);
        l.setFormToTextSpace(4f);
        l.setXEntrySpace(6f);
    }

    public void updateChart(List<Build> builds){

        HashMap<String, BuildStat> groups = new HashMap<>();

        for (Build b : builds){
            String key = DateFormat.format(b.startDate);
            BuildStat value = groups.get(key);
            value = value != null ? value : new BuildStat();

            if(b.status.equalsIgnoreCase("SUCCESS")){
                value.Passed++;
            }else{
                value.Failed++;
            }

            groups.put(key, value);
        }
        ArrayList<Map.Entry<String, BuildStat>> entries = new ArrayList<>(groups.entrySet());
        Collections.sort(entries, (lhs, rhs) -> lhs.getKey().compareTo(rhs.getKey()));

        ArrayList<String> xVals = new ArrayList<>();
        ArrayList<BarEntry> yVals1 = new ArrayList<>();
        int i = 0;

        for (Map.Entry<String, BuildStat> item : entries){
            BuildStat stat = item.getValue();

            yVals1.add(new BarEntry(new float[]{ stat.Passed, stat.Failed }, i++));
            xVals.add(item.getKey());
        }

        BarDataSet set1 = new BarDataSet(yVals1, "");
        set1.setColors(new int[]{
                _context.getColor(R.color.green_fill),
                _context.getColor(R.color.red_fill),
        });
        set1.setStackLabels(new String[] { "Passes", "Failures" });
        set1.setDrawValues(false);
        set1.setBarSpacePercent(20);

        ArrayList<BarDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1);

        BarData data = new BarData(xVals, dataSets);

        chart.setData(data);
        chart.invalidate();
        chart.setVisibility(View.VISIBLE);

        _xvals = xVals;
        _yvals = xVals;
    }

    @Override
    public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
        String date = _xvals.get(e.getXIndex());
        int stackIndex = h.getStackIndex();
        _onChartSelectedListener.onValueSelected(date, stackIndex == 0 ? BuildStatus.Success : BuildStatus.Failure);
    }

    @Override
    public void onNothingSelected() {
        _onChartSelectedListener.selectionCleared();
    }

    private static class BuildStat {
        public int Passed;
        public int Failed;
    }
}
