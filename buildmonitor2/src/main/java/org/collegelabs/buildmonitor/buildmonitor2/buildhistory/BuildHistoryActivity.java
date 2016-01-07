package org.collegelabs.buildmonitor.buildmonitor2.buildhistory;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import org.collegelabs.buildmonitor.buildmonitor2.BuildMonitorApplication;
import org.collegelabs.buildmonitor.buildmonitor2.R;
import org.collegelabs.buildmonitor.buildmonitor2.builds.BuildAdapter;
import org.collegelabs.buildmonitor.buildmonitor2.builds.BuildStatus;
import org.collegelabs.buildmonitor.buildmonitor2.builds.ProjectSummaryService;
import org.collegelabs.buildmonitor.buildmonitor2.buildstatus.BuildStatusActivity;
import org.collegelabs.buildmonitor.buildmonitor2.storage.BuildTypeWithCredentials;
import org.collegelabs.buildmonitor.buildmonitor2.tc.TcUtil;
import org.collegelabs.buildmonitor.buildmonitor2.tc.models.Build;
import org.collegelabs.buildmonitor.buildmonitor2.tc.models.BuildCollectionResponse;
import org.collegelabs.buildmonitor.buildmonitor2.util.RxUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;
import static org.collegelabs.buildmonitor.buildmonitor2.util.Linq.*;


public class BuildHistoryActivity extends Activity implements OnChartSelectedListener {

    @InjectView(android.R.id.list) public ListView listView;

    private Subscription _subscription;
    private BuildAdapter _adapter;
    private BuildHistoryHeader _header;
    private List<Build> _builds = new ArrayList<>();
    private int _buildId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.build_history_activity);
        ButterKnife.inject(this);

        Bundle bundle = getIntent().getExtras();
        _buildId = bundle.getInt("sqliteBuildId");

        _adapter = new BuildAdapter(this);
        listView.setAdapter(_adapter);

        _header = new BuildHistoryHeader(this, this, _buildId);
        listView.addHeaderView(_header.getView());

        listView.setOnItemClickListener(this::onItemClick);

        _subscription = BuildMonitorApplication.Db.getAllBuildTypesWithCreds()
                .subscribeOn(Schedulers.newThread())
                .flatMap(b -> Observable.from(b))
                .filter(f -> f.buildType.sqliteBuildId == _buildId)
                .first()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onGotBuild, e -> Timber.e(e, "Failure getting project"));

    }

    private void onItemClick(AdapterView adapterView, View view, int position, long id) {
        if(position == AbsListView.INVALID_POSITION){
            return;
        }

        if(position == 0){
            // chart
            selectionCleared();
            _header.chart.highlightValues(null);
            return;
        }

        Build item = (Build) adapterView.getItemAtPosition(position);
        if(item != null){
            startActivity(BuildStatusActivity.getIntent(this, _buildId, item.id));
        }
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
        _adapter.addAll(response.builds);
        _header.updateChart(response.builds);
        _builds = new ArrayList<>(response.builds);
    }

    private void unsubscribe() {
        RxUtil.unsubscribe(_subscription);
        _subscription = null;
    }

    public static Intent getIntent(Context context, int buildId){
        Intent intent = new Intent(context, BuildHistoryActivity.class);
        intent.putExtra("sqliteBuildId", buildId);
        return intent;
    }

    @Override
    public void onValueSelected(String date, BuildStatus buildStatus) {

        Timber.d("Filtering by %s %s", date, buildStatus.toString());

        final ArrayList<Build> builds = toList(where(_builds,
                b -> TcUtil.getBuildStatus(b.status) == buildStatus
                        && BuildHistoryHeader.DateFormat.format(b.startDate).equals(date)));

        _adapter.clear();
        _adapter.addAll(builds);
    }

    @Override
    public void selectionCleared() {
        _adapter.clear();
        _adapter.addAll(_builds);
    }
}
