package org.collegelabs.buildmonitor.buildmonitor2.buildhistory;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
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
import org.collegelabs.buildmonitor.buildmonitor2.util.ActivityUtil;
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

    @InjectView(android.R.id.list) public ListView listView;

    private Subscription _subscription;
    private BuildAdapter _adapter;
    private BuildHistoryHeader _header;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.build_history_activity);
        ButterKnife.inject(this);

        Bundle bundle = getIntent().getExtras();
        int buildId = bundle.getInt("buildId");

        _adapter = new BuildAdapter(this);
        listView.setAdapter(_adapter);

        _header = new BuildHistoryHeader(this);
        listView.addHeaderView(_header.getView());

        listView.setOnItemClickListener(this::onItemClick);

        _subscription = BuildMonitorApplication.Db.getAllBuildTypesWithCreds()
                .flatMap(b -> Observable.from(b))
                .filter(f -> f.buildType.id == buildId)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .first()
                .subscribe(this::onGotBuild, e -> Timber.e(e, "Failure getting project"));

    }

    private void onItemClick(AdapterView adapterView, View view, int position, long id) {
        if(position == AbsListView.INVALID_POSITION){
            return;
        }

        ActivityUtil.openUrl(this, () -> {
            BuildViewModel item = (BuildViewModel) adapterView.getItemAtPosition(position);
            return item == null ? null : item.webUrl;
        });
    }


    private void onGotBuild(final BuildTypeWithCredentials build) {
        _header.updateTitles(build.buildType.projectName, build.buildType.name);
        getActionBar().setTitle(build.buildType.displayName);
        unsubscribe();

        GregorianCalendar cal = new GregorianCalendar();
        cal.add(Calendar.MONTH, -2);

        // get the most recent 1K builds in the last 2 months
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

    private void UpdateUI(BuildCollectionResponse response) {

        _adapter.clear();
        ArrayList<BuildViewModel> models = new ArrayList<>(response.builds.size());
        for (Build b : response.builds){
            models.add(new BuildViewModel(b));
        }
        _adapter.addAll(models);
        _header.updateChart(response.builds);
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
