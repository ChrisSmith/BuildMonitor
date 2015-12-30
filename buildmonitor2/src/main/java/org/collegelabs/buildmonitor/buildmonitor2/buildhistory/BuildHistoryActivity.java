package org.collegelabs.buildmonitor.buildmonitor2.buildhistory;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

import com.db.chart.Tools;
import com.db.chart.model.Bar;
import com.db.chart.model.BarSet;
import com.db.chart.view.AxisController;
import com.db.chart.view.BarChartView;
import com.db.chart.view.XController;
import com.db.chart.view.YController;

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
import java.util.Collections;
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

    @InjectView(R.id.build_history_chart) public BarChartView chart;
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

        _subscription = new ProjectSummaryService().getBuilds(build, 100, 0)
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

        BarSet passedBarset = new BarSet();
        BarSet failedBarset = new BarSet();

        ArrayList<Map.Entry<String, BuildStat>> entries = new ArrayList<>(groups.entrySet());
        Collections.sort(entries, (lhs, rhs) -> lhs.getKey().compareTo(rhs.getKey()));

        for (Map.Entry<String, BuildStat> item : entries){
            passedBarset.addBar(new Bar(item.getKey(), item.getValue().Passed));
            failedBarset.addBar(new Bar(item.getKey(), item.getValue().Failed));
        }

        passedBarset.setColor(getResources().getColor(R.color.green_fill, getTheme()));
        failedBarset.setColor(getResources().getColor(R.color.red_fill, getTheme()));
        chart.addData(failedBarset);
        chart.addData(passedBarset);

        chart.setSetSpacing(Tools.fromDpToPx(-15));
        chart.setBarSpacing(Tools.fromDpToPx(35));
        chart.setRoundCorners(Tools.fromDpToPx(2));


        Paint gridPaint = new Paint();
        gridPaint.setColor(Color.parseColor("#8986705C"));
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setAntiAlias(true);
        gridPaint.setStrokeWidth(Tools.fromDpToPx(.75f));

        chart.setBorderSpacing(5)
                .setAxisBorderValues(0, 10, 2)
                .setGrid(BarChartView.GridType.FULL, 10, entries.size(), gridPaint)
                .setXLabels(XController.LabelPosition.OUTSIDE)
                .setYLabels(YController.LabelPosition.OUTSIDE)
                .setLabelsColor(Color.parseColor("#86705c"))
                .setAxisColor(Color.parseColor("#86705c"));


        chart.show();

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
