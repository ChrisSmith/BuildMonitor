package org.collegelabs.buildmonitor.buildmonitor2.buildstatus;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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

    private BuildChainAdapter _adapter;
    private BuildStatusViewModel _model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.buildstatus_activity);
        ButterKnife.inject(this);

        Bundle bundle = getIntent().getExtras();
        _model = new BuildStatusViewModel(bundle.getInt("sqliteBuildId", -1), bundle.getInt("buildTypeId", -1));
        _adapter = new BuildChainAdapter(this, this);
        _adapter.setStatusViewModel(_model);

        dependenciesView.setAdapter(_adapter);
        dependenciesView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        dependenciesView.setOnItemClickListener(this);


        _subscription = BuildMonitorApplication.Db.getAllBuildTypesWithCreds()
                .flatMap(b -> Observable.from(b))
                .filter(f -> f.buildType.sqliteBuildId == _model.BuildTypeId)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .first()
                .subscribe(this::onGotBuild, e -> Timber.e(e, "Failure getting build from db"));
    }

    private void onGotBuild(BuildTypeWithCredentials buildTypeWithCredentials) {
        RxUtil.unsubscribe(_subscription);

        getActionBar().setTitle(buildTypeWithCredentials.buildType.displayName);

        TeamCityService teamCityService = ServiceHelper.getService(buildTypeWithCredentials.credentials);
        _subscription = BuildChainObservable.create(teamCityService, _model.BuildId)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onReceivePartialBuildChain, e -> Timber.e(e, "Failure getting build details"));
    }

    private void onReceivePartialBuildChain(BuildDetailsResponse response) {
        if(_model.BuildId != response.buildId){
            // the final build will be in the header
            _adapter.addItem(response);
        }

        _model.addSnapshotDependency(response);
        _adapter.notifyItemChanged(0);
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
