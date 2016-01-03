package org.collegelabs.buildmonitor.buildmonitor2.buildstatus;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;

import org.collegelabs.buildmonitor.buildmonitor2.BuildMonitorApplication;
import org.collegelabs.buildmonitor.buildmonitor2.R;
import org.collegelabs.buildmonitor.buildmonitor2.storage.BuildTypeWithCredentials;
import org.collegelabs.buildmonitor.buildmonitor2.tc.ServiceHelper;
import org.collegelabs.buildmonitor.buildmonitor2.tc.TeamCityService;
import org.collegelabs.buildmonitor.buildmonitor2.tc.models.BuildDetailsResponse;
import org.collegelabs.buildmonitor.buildmonitor2.ui.OnItemClickListener;
import org.collegelabs.buildmonitor.buildmonitor2.ui.SelectableRecyclerView;
import org.collegelabs.buildmonitor.buildmonitor2.util.ActivityUtil;
import org.collegelabs.buildmonitor.buildmonitor2.util.RxUtil;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class BuildStatusActivity extends Activity implements OnItemClickListener {

    @InjectView(R.id.buildstatus_dependencies) public SelectableRecyclerView dependenciesView;

    private Subscription _subscription;
    private int _buildId;
    private int _buildTypeId;
    private BuildChainAdapter _adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.buildstatus_activity);
        ButterKnife.inject(this);

        Bundle bundle = getIntent().getExtras();
        _buildId = bundle.getInt("sqliteBuildId");
        _buildTypeId = bundle.getInt("buildTypeId");

        _adapter = new BuildChainAdapter(this, this);
        dependenciesView.setLayoutManager(new LinearLayoutManager(this));
        dependenciesView.setAdapter(_adapter);
        dependenciesView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        dependenciesView.setOnItemClickListener(this);

        _subscription = BuildMonitorApplication.Db.getAllBuildTypesWithCreds()
                .flatMap(b -> Observable.from(b))
                .filter(f -> f.buildType.sqliteBuildId == _buildTypeId)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .first()
                .subscribe(this::onGotBuild, e -> Timber.e(e, "Failure getting build from db"));
    }

    private void onGotBuild(BuildTypeWithCredentials buildTypeWithCredentials) {
        RxUtil.unsubscribe(_subscription);

        TeamCityService teamCityService = ServiceHelper.getService(buildTypeWithCredentials.credentials);
        _subscription = BuildChainObservable.create(teamCityService, _buildId)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onReceivePartialBuildChain, e -> Timber.e(e, "Failure getting build details"));
    }

    private void onReceivePartialBuildChain(BuildDetailsResponse response) {
        _adapter.addItem(response);
    }

    @Override
    protected void onDestroy() {
        RxUtil.unsubscribe(_subscription);
        _subscription = null;
        super.onDestroy();
    }

    @Override
    public void onClick(View v, int position) {
        ActivityUtil.openUrl(this, () -> {
            BuildDetailsResponse item = _adapter.getItem(position);
            return item.webUrl;
        });
    }

    public static Intent getIntent(Context context, int buildTypeId, int buildId){
        Intent intent = new Intent(context, BuildStatusActivity.class);
        intent.putExtra("sqliteBuildId", buildId);
        intent.putExtra("buildTypeId", buildTypeId);
        return intent;
    }
}
