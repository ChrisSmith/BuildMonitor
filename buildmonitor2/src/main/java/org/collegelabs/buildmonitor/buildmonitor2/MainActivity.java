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
import android.widget.ListView;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import org.collegelabs.buildmonitor.buildmonitor2.builds.BuildAdapter;
import org.collegelabs.buildmonitor.buildmonitor2.builds.BuildViewModel;
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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class MainActivity extends Activity implements AdapterView.OnItemClickListener {
    @InjectView(android.R.id.list) ListView _listview;

    private Subscription _sub;
    private BuildAdapter _adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        ButterKnife.inject(this);

        _adapter = new BuildAdapter(this, new ArrayList<>());
        _listview.setAdapter(_adapter);
        _listview.setOnItemClickListener(this);

        showLoadingView(); //set view model of some sort?
    }

    @Override
    protected void onStart() {
        super.onStart();

        int pageSize = 100;
        int offset = 0;
        String buildTypeId = "bt312";
        String buildLocator = "buildType:" + buildTypeId + ",running:any,canceled:any,count:" + pageSize + ",start:" + offset;;

        Observable<TeamCityService> o1 = ServiceHelper.getService(BuildMonitorApplication.Db);
        final int initDelay = 0;
        final int interval = 60;
        Observable<Long> o2 = Observable.interval(initDelay, interval, TimeUnit.SECONDS);

        _sub = Observable.combineLatest(o1, o2, Pair::create)
                .subscribeOn(Schedulers.newThread())
                .flatMap(x -> x.first.getBuilds(buildLocator))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::UpdateUI, e -> Timber.e(e, "Failure getting project"))
                ;
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
        _adapter.add(new BuildViewModel()); // empty item for the header
    }

    private void UpdateUI(BuildCollectionResponse response) {

        // TODO handle 0 builds

        ArrayList<BuildViewModel> viewModels = new ArrayList<>(response.builds.size());

        for(Build b : response.builds){
            viewModels.add(new BuildViewModel(b));
        }

        _adapter.clear();
        _adapter.addAll(viewModels);
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
        BuildViewModel viewModel = _adapter.getItem(position);
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(viewModel.webUrl)));
        } catch (Exception e) {
            Timber.e("Failed to open " + viewModel.webUrl, e);
        }
    }
}
