package org.collegelabs.buildmonitor.buildmonitor2.buildhistory;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.collegelabs.buildmonitor.buildmonitor2.BuildMonitorApplication;
import org.collegelabs.buildmonitor.buildmonitor2.R;
import org.collegelabs.buildmonitor.buildmonitor2.builds.BuildStatus;
import org.collegelabs.buildmonitor.buildmonitor2.tc.models.BuildDetailsResponse;
import org.collegelabs.buildmonitor.buildmonitor2.util.RxUtil;
import org.collegelabs.buildmonitor.buildmonitor2.util.ToastUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;


public class BuildStatisticsActivity extends Activity implements OnChartValueSelectedListener {

    private Subscription _subscription;
    private int _buildId;

    @InjectView(R.id.build_statistics_chart) BarChart _chart;
    @InjectView(R.id.build_statistics_chart_textview) TextView _textView;
    private BarData _barData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.buildstatistics_activity);
        ButterKnife.inject(this);

        Bundle extras = getIntent().getExtras();
        _buildId = extras.getInt("sqliteBuildId");

        _chart.setOnChartValueSelectedListener(this);

        StatisticsService statsService = new StatisticsService();

        _subscription = BuildMonitorApplication.Db.getAllBuildTypesWithCreds()
                .subscribeOn(Schedulers.newThread())
                .flatMap(b -> Observable.from(b))
                .filter(f -> f.buildType.sqliteBuildId == _buildId)
                .first()
                .flatMap(b -> statsService.getRecentFailedBuilds(b))
                .scan(new BuildStatistics(), this::scan)
                .debounce(500, TimeUnit.MILLISECONDS)
                .map(s -> s.caclulate())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onGotStats, e -> {
                    ToastUtil.show(this, "Failed to get stats");
                    Timber.e(e, "Failure getting project");
                });
    }

    private void onGotStats(BarData barData) {
        _barData = barData;

        _chart.clear();
        _chart.setData(barData);
        _chart.invalidate();
        _chart.setVisibility(View.VISIBLE);
    }

    private BuildStatistics scan(BuildStatistics statistics, BuildDetailsResponse response) {
        return statistics.addFailingBuild(response);
    }

    @Override
    protected void onDestroy() {
        RxUtil.unsubscribe(_subscription);
        _subscription = null;
        super.onDestroy();
    }


    public static Intent getIntent(Context context, int buildId){
        Intent intent = new Intent(context, BuildStatisticsActivity.class);
        intent.putExtra("sqliteBuildId", buildId);
        return intent;
    }

    @Override
    public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
        if(_barData == null){
            onNothingSelected();
            return;
        }

        BarEntry bar = (BarEntry) e;

        BarDataSet set1 = _barData.getDataSetByIndex(0);
        String date = _chart.getXValue(e.getXIndex());
        int labelIndex = h.getStackIndex();

        String[] labels = set1.getStackLabels();
        String label = labels[labelIndex];

        float value = bar.getVals()[labelIndex];
        _textView.setText(value + " Failures on " + date + " of '" + label+"'");
    }

    @Override
    public void onNothingSelected() {
        _textView.setText("");
    }
}
