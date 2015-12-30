package org.collegelabs.buildmonitor.buildmonitor2.buildhistory;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import org.collegelabs.buildmonitor.buildmonitor2.BuildMonitorApplication;
import org.collegelabs.buildmonitor.buildmonitor2.R;
import org.collegelabs.buildmonitor.buildmonitor2.builds.BuildAdapter;
import org.collegelabs.buildmonitor.buildmonitor2.builds.BuildViewModel;
import org.collegelabs.buildmonitor.buildmonitor2.builds.ProjectSummaryService;
import org.collegelabs.buildmonitor.buildmonitor2.storage.BuildTypeWithCredentials;
import org.collegelabs.buildmonitor.buildmonitor2.tc.models.Build;
import org.collegelabs.buildmonitor.buildmonitor2.tc.models.BuildCollectionResponse;
import org.collegelabs.buildmonitor.buildmonitor2.util.RxUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;


public class BuildHistoryActivity extends Activity {

    @InjectView(R.id.build_history_chart) public BarChart chart2;
    @InjectView(R.id.build_history_projectname) public TextView projectName;
    @InjectView(R.id.build_history_name) public TextView buildName;
    @InjectView(android.R.id.list) public ListView listView;

    private Subscription _subscription;
    private BuildAdapter _adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.build_history_activity);
        ButterKnife.inject(this);

        Bundle bundle = getIntent().getExtras();
        int buildId = bundle.getInt("buildId");

        _adapter = new BuildAdapter(this);
        listView.setAdapter(_adapter);

        chart2.setDrawValueAboveBar(false);
        chart2.setDrawHighlightArrow(false);
        chart2.setDescription("");
        chart2.getAxisRight().setEnabled(false);
        XAxis xAxis = chart2.getXAxis();
        xAxis.setGridColor(getColor(R.color.grey));
        xAxis.setGridLineWidth(1.1f);

        YAxis yAxis = chart2.getAxis(YAxis.AxisDependency.LEFT);
        yAxis.setGridColor(getColor(R.color.grey));
        yAxis.setGridLineWidth(1.1f);

        chart2.setGridBackgroundColor(getColor(R.color.white));

        Legend l = chart2.getLegend();
        l.setPosition(Legend.LegendPosition.BELOW_CHART_RIGHT);
        l.setFormSize(8f);
        l.setFormToTextSpace(4f);
        l.setXEntrySpace(6f);

        _subscription = BuildMonitorApplication.Db.getAllBuildTypesWithCreds()
                .flatMap(b -> Observable.from(b))
                .filter(f -> f.buildType.id == buildId)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .first()
                .subscribe(this::onGotBuild, e -> Timber.e(e, "Failure getting project"));

    }

    private void onGotBuild(final BuildTypeWithCredentials build) {
        buildName.setText(build.buildType.name);
        projectName.setText(build.buildType.projectName);
        getActionBar().setTitle(build.buildType.displayName);
        unsubscribe();

        GregorianCalendar cal = new GregorianCalendar();
        cal.add(Calendar.WEEK_OF_YEAR, -2);

        // get the most recent 1K builds in the last 2 weeks
        _subscription = new ProjectSummaryService().getBuilds(build, 1000, 0, cal.getTime())
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(this::UpdateUI, e -> Timber.e(e, "Failure getting project"));
    }

    @Override
    protected void onDestroy() {
        unsubscribe();
        super.onDestroy();
    }

    public static class BuildStat {
        public int Passed;
        public int Failed;
    }

    private void UpdateUI(BuildCollectionResponse response) {

        _adapter.clear();
        ArrayList<BuildViewModel> models = new ArrayList<>(response.builds.size());
        for (Build b : response.builds){
            models.add(new BuildViewModel(b));
        }
        _adapter.addAll(models);

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        HashMap<String, BuildStat> groups = new HashMap<>();

        for (Build b : response.builds){
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
                getColor(R.color.green_fill),
                getColor(R.color.red_fill),
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

    private void unsubscribe() {
        RxUtil.unsubscribe(_subscription);
        _subscription = null;
    }

    public static Intent getIntent(Context context, int buildId){
        Intent intent = new Intent(context, BuildHistoryActivity.class);
        intent.putExtra("buildId", buildId);
        return intent;
    }
}
