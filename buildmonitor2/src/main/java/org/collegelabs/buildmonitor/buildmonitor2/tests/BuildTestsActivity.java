package org.collegelabs.buildmonitor.buildmonitor2.tests;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import org.collegelabs.buildmonitor.buildmonitor2.BuildMonitorApplication;
import org.collegelabs.buildmonitor.buildmonitor2.R;
import org.collegelabs.buildmonitor.buildmonitor2.storage.BuildTypeWithCredentials;
import org.collegelabs.buildmonitor.buildmonitor2.tc.ServiceHelper;
import org.collegelabs.buildmonitor.buildmonitor2.ui.SelectableRecyclerView;
import org.collegelabs.buildmonitor.buildmonitor2.util.RxUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class BuildTestsActivity extends Activity {

    @InjectView(R.id.buildtests_list) public SelectableRecyclerView recyclerView;
    private BuildTestAdapter _adapter;
    private int _sqliteBuildId;
    private Subscription _subscription;
    private int _buildId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.buildtests_activity);
        ButterKnife.inject(this);

        _adapter = new BuildTestAdapter(this);
        recyclerView.setAdapter(_adapter);

        Bundle bundle = getIntent().getExtras();
        _sqliteBuildId = bundle.getInt("sqliteBuildId");
        _buildId = bundle.getInt("buildId");

        _subscription = BuildMonitorApplication.Db.getAllBuildTypesWithCreds()
                .flatMap(b -> Observable.from(b))
                .filter(f -> f.buildType.sqliteBuildId == _sqliteBuildId)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .first()
                .subscribe(this::onGotBuild, e -> Timber.e(e, "Failure getting project"));
    }

    private void onGotBuild(BuildTypeWithCredentials build) {
        RxUtil.unsubscribe(_subscription);

        _subscription = ServiceHelper.getService2(build.credentials)
                .getTestResults(_buildId)
                .map(r -> {
                    try(InputStream in = r.getBody().in()){
                        return new TestCsvParser().parse(in);
                    } catch (IOException e) {
                        e.printStackTrace();
                        return new ArrayList<TestResult>();
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onGotTests, e -> Timber.e(e, "Failed to get tests"));
    }

    private void onGotTests(ArrayList<TestResult> testResults) {
        _adapter.replaceAll(testResults);
    }

    @Override
    protected void onDestroy() {
        RxUtil.unsubscribe(_subscription);
        _subscription = null;
        super.onDestroy();
    }

    public static Intent getIntent(Context context, int buildId, int sqliteBuildId){
        Intent intent = new Intent(context, BuildTestsActivity.class);
        intent.putExtra("buildId", buildId);
        intent.putExtra("sqliteBuildId", sqliteBuildId);
        return intent;
    }
}
