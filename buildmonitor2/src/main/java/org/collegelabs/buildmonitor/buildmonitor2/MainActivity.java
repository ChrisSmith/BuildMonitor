package org.collegelabs.buildmonitor.buildmonitor2;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import butterknife.ButterKnife;
import butterknife.InjectView;

import org.collegelabs.buildmonitor.buildmonitor2.buildhistory.BuildHistoryActivity;
import org.collegelabs.buildmonitor.buildmonitor2.builds.*;
import org.collegelabs.buildmonitor.buildmonitor2.storage.BuildTypeWithCredentials;
import org.collegelabs.buildmonitor.buildmonitor2.util.CombineLatestAsList;
import org.collegelabs.buildmonitor.buildmonitor2.util.RxUtil;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.Observable;
import timber.log.Timber;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class MainActivity extends Activity implements AdapterView.OnItemClickListener, ActionMode.Callback, AbsListView.MultiChoiceModeListener {
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
        _gridView.setMultiChoiceModeListener(this);
        _gridView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);

        showLoadingView(); //set view model of some sort?
    }

    @Override
    protected void onStart() {
        super.onStart();

        final int initDelay = 0;
        final int interval = 60;

        Observable<Long> pulse = Observable.interval(initDelay, interval, TimeUnit.SECONDS);
        Observable<ArrayList<ProjectSummary>> o1 = BuildMonitorApplication.Db.getAllBuildTypesWithCreds()
                .flatMap(builds -> getObs(builds, pulse));

        _sub = o1.startWith(getEmptyProjectSummaryList())
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::UpdateUI, e -> Timber.e(e, "Failure getting project"));

    }

    @NonNull
    private Observable<ArrayList<ProjectSummary>> getObs(List<BuildTypeWithCredentials> builds, Observable<Long> pulse) {

        List<Observable<ProjectSummary>> sources = new ArrayList<>(builds.size());

        for(BuildTypeWithCredentials buildType : builds){
            final String displayName = buildType.buildType.displayName;
            final int buildId = buildType.buildType.id;

            ProjectSummaryService service = new ProjectSummaryService();

            ProjectSummary loadingSummary = new ProjectSummary();
            loadingSummary.status = BuildStatus.Loading;
            loadingSummary.name = displayName;

            sources.add(pulse
                .flatMap(i -> service.getMostRecentBuild(buildType))
                .map(response -> ProjectSummaryService.makeProjectSummary(buildId, displayName, response))
                .onErrorReturn(t -> makeErrorViewModel(displayName, t))
                .startWith(loadingSummary)
            );
        }

        return CombineLatestAsList.create(sources);
    }

    private ProjectSummary makeErrorViewModel(String displayName, Throwable throwable) {
        ProjectSummary summary = new ProjectSummary();
        summary.name = displayName;
        summary.status = BuildStatus.FailedToLoad;
        summary.statusText = throwable.getMessage();

        Timber.e("Failed to load build", throwable);

        return summary;
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

        RxUtil.unsubscribe(_sub);
        _sub = null;
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
//            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(projectSummary.webUrl)));
            startActivity(BuildHistoryActivity.getIntent(this, projectSummary.buildId));
        } catch (Exception e) {
            Timber.e(e, "Failed to open " + projectSummary.webUrl);
        }
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        mode.getMenuInflater().inflate(R.menu.main_contextual, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

        if(item.getItemId() == R.id.action_delete_builds){

            long[] checkedItemIds = _gridView.getCheckedItemIds();
            for(long id : checkedItemIds){
                Timber.d("Deleting %d", id);
                BuildMonitorApplication.Db.deleteBuildType(id);
            }

            mode.finish();
            return true;
        }

        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) { }

    @Override
    public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {

        int count = _gridView.getCheckedItemCount();
        String title = count == 1 ? " build selected" : " builds selected";
        mode.setTitle(count + title);
    }
}
