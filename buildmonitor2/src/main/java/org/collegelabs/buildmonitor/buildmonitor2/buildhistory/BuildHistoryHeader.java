package org.collegelabs.buildmonitor.buildmonitor2.buildhistory;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import org.collegelabs.buildmonitor.buildmonitor2.R;
import org.collegelabs.buildmonitor.buildmonitor2.tc.models.Build;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class BuildHistoryHeader {

    private final Context _context;
    @InjectView(R.id.build_history_chart) public BarChart chart2;
    @InjectView(R.id.build_history_projectname) public TextView projectName;
    @InjectView(R.id.build_history_name) public TextView buildName;
    private final View _view;

    public BuildHistoryHeader(Context context){
        _context = context;
        _view = LayoutInflater.from(_context).inflate(R.layout.build_history_header, null);
        ButterKnife.inject(this, _view);
        customizeChart();
    }

    public View getView(){
        return _view;
    }

    public void updateTitles(String projectName, String buildName){
        this.buildName.setText(buildName);
        this.projectName.setText(projectName);
    }

    private void customizeChart() {
        chart2.setDrawValueAboveBar(false);
        chart2.setDrawHighlightArrow(false);
        chart2.setDescription("");
        chart2.getAxisRight().setEnabled(false);
        XAxis xAxis = chart2.getXAxis();
        xAxis.setGridColor(_context.getColor(R.color.grey));
        xAxis.setGridLineWidth(1.1f);

        YAxis yAxis = chart2.getAxis(YAxis.AxisDependency.LEFT);
        yAxis.setGridColor(_context.getColor(R.color.grey));
        yAxis.setGridLineWidth(1.1f);

        chart2.setGridBackgroundColor(_context.getColor(R.color.white));

        Legend l = chart2.getLegend();
        l.setPosition(Legend.LegendPosition.BELOW_CHART_RIGHT);
        l.setFormSize(8f);
        l.setFormToTextSpace(4f);
        l.setXEntrySpace(6f);
    }

    public void updateChart(List<Build> builds){
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        HashMap<String, BuildStat> groups = new HashMap<>();

        for (Build b : builds){
            String key = df.format(b.startDate);
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
        set1.setHighlightEnabled(false);
        set1.setDrawValues(false);
        set1.setBarSpacePercent(20);

        ArrayList<BarDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1);

        BarData data = new BarData(xVals, dataSets);

        chart2.setData(data);
        chart2.invalidate();
    }

    private static class BuildStat {
        public int Passed;
        public int Failed;
    }
}
