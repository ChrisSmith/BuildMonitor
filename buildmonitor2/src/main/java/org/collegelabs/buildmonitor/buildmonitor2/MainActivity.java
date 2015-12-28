package org.collegelabs.buildmonitor.buildmonitor2;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import org.collegelabs.buildmonitor.buildmonitor2.builds.*;
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

        //String buildTypeId = "";
        List<String> builds = new ArrayList<>();

        final int initDelay = 0;
        final int interval = 60;
        Observable<Long> o2 = Observable.interval(initDelay, interval, TimeUnit.SECONDS);

        ArrayList<ProjectSummary> emptyList = new ArrayList<>(builds.size());
        for (String build : builds){
            ProjectSummary summary = new ProjectSummary();
            summary.status = BuildStatus.Loading;
            emptyList.add(summary);
        }

        _sub = o2.flatMap(f -> new ProjectSummaryService().getSummaries(builds))
                .startWith(emptyList)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::UpdateUI, e -> Timber.e(e, "Failure getting project"))
                ;
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
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
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
