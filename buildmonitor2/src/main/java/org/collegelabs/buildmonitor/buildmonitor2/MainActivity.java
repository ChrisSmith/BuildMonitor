package org.collegelabs.buildmonitor.buildmonitor2;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import org.collegelabs.buildmonitor.buildmonitor2.builds.*;
import org.collegelabs.buildmonitor.buildmonitor2.storage.BuildTypeDto;
import org.collegelabs.buildmonitor.buildmonitor2.storage.BuildTypeWithCredentials;
import org.collegelabs.buildmonitor.buildmonitor2.tc.Credentials;
import org.collegelabs.buildmonitor.buildmonitor2.tc.ServiceHelper;
import org.collegelabs.buildmonitor.buildmonitor2.tc.TeamCityService;
import org.collegelabs.buildmonitor.buildmonitor2.tc.models.Build;
import org.collegelabs.buildmonitor.buildmonitor2.tc.models.BuildCollectionResponse;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.Observable;
import timber.log.Timber;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends Activity implements AdapterView.OnItemClickListener {
    @InjectView(android.R.id.list) GridView _gridView;

    private Subscription _sub;
    private ProjectSummaryAdapter _adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        ButterKnife.inject(this);

        _adapter = new ProjectSummaryAdapter(this);
        _gridView.setAdapter(_adapter);
        _gridView.setOnItemClickListener(this);

        showLoadingView(); //set view model of some sort?
    }

    @Override
    protected void onStart() {
        super.onStart();

        final int initDelay = 0;
        final int interval = 60;

        Observable<List<BuildTypeWithCredentials>> o1 = BuildMonitorApplication.Db.getAllBuildTypesWithCreds();
        Observable<Long> o2 = Observable.interval(initDelay, interval, TimeUnit.SECONDS);

        _sub =  Observable.combineLatest(o1, o2, Pair::create)
                .map(p -> p.first)
                .flatMap(builds -> new ProjectSummaryService().getSummaries(builds)) // TODO use combineLatest to display items from db first?
                .startWith(getEmptyProjectSummaryList())
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::UpdateUI, e -> Timber.e(e, "Failure getting project"))
                ;
    }

    private ArrayList<ProjectSummary> getEmptyProjectSummaryList() {
        ArrayList<ProjectSummary> emptyList = new ArrayList<>();
        ProjectSummary summary = new ProjectSummary();
        summary.status = BuildStatus.Loading;
        emptyList.add(summary);
        return emptyList;
    }

    private void UpdateUI(List<ProjectSummary> summaries) {
        // TODO handle 0 builds
        _adapter.clear();
        _adapter.addAll(summaries);
    }

    @Override
    protected void onStop() {
        super.onStop();

        if(_sub != null && !_sub.isUnsubscribed()){
            _sub.unsubscribe();
            _sub = null;
        }
    }

    private void showLoadingView(){
        _adapter.clear();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        if(id == R.id.action_add_build){
            startActivity(EditBuildActivity.getIntent(this));
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ProjectSummary projectSummary = _adapter.getItem(position);
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(projectSummary.webUrl)));
        } catch (Exception e) {
            Timber.e("Failed to open " + projectSummary.webUrl, e);
        }
    }
}
