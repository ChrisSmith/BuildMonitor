package org.collegelabs.buildmonitor.buildmonitor2.logs;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;

import org.collegelabs.buildmonitor.buildmonitor2.BuildMonitorApplication;
import org.collegelabs.buildmonitor.buildmonitor2.R;
import org.collegelabs.buildmonitor.buildmonitor2.storage.BuildTypeWithCredentials;
import org.collegelabs.buildmonitor.buildmonitor2.tc.ServiceHelper;
import org.collegelabs.buildmonitor.buildmonitor2.ui.OnItemClickListener;
import org.collegelabs.buildmonitor.buildmonitor2.util.DiskUtil;
import org.collegelabs.buildmonitor.buildmonitor2.util.RxUtil;
import org.collegelabs.buildmonitor.buildmonitor2.util.TimeUtil;

import java.io.File;
import java.io.RandomAccessFile;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class BuildLogActivity extends Activity implements OnItemClickListener {

    private int _buildId;
    private int _sqliteBuildId;
    private Subscription _subscription;

    private BuildLogAdapter logAdapter;
    private LogSearchResultsAdapter searchAdapter;
    private boolean showingSearchViews = false;

    private MenuItem searchMenuItem;
    private String _fileName;
    private File _file;

    @InjectView(R.id.buildlog_list) public RecyclerView logList;

    public static Intent getIntent(Context context, int sqliteBuildId, int buildId) {
        Intent intent = new Intent(context, BuildLogActivity.class);
        intent.putExtra("buildId", buildId);
        intent.putExtra("sqliteBuildId", sqliteBuildId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.build_log_activity);
        ButterKnife.inject(this);

        Bundle bundle = getIntent().getExtras();

        _sqliteBuildId = bundle.getInt("sqliteBuildId", 0);
        _buildId = bundle.getInt("buildId", 0);

        if (_buildId == 0 || _sqliteBuildId == 0) {
            Toast.makeText(this, "Invalid buildId", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        final File cacheDir = getCacheDir();
        _fileName = "buildLog-" + _buildId + ".txt";
        _file = new File(cacheDir, _fileName);


        logAdapter = new BuildLogAdapter();
        searchAdapter = new LogSearchResultsAdapter(this, this);

        logList.setAdapter(logAdapter);

        displaySearchViews(false);

        _subscription = BuildMonitorApplication.Db.getAllBuildTypesWithCreds()
                .flatMap(b -> Observable.from(b))
                .filter(f -> f.buildType.sqliteBuildId == _sqliteBuildId)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .first()
                .subscribe(this::onBuildLoadedDetails, e -> Timber.e(e, "Failure getting project"));
    }


    @Override
    protected void onDestroy() {
        RxUtil.unsubscribe(_subscription);
        _subscription = null;
        super.onDestroy();
    }
    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_build_log, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchMenuItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchMenuItem.getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        searchMenuItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                displaySearchViews(true);
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                displaySearchViews(false);
                return true;
            }
        });

        return true;
    }


    private void onBuildLoadedDetails(BuildTypeWithCredentials build) {

        ServiceHelper.getService(build.credentials)
                .getLog(_buildId)
                .map(r -> DiskUtil.WriteBytesToDisk(r, _file))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onLogDownloaded, e -> Timber.e(e, "Failed to download log"));
    }

    private void onLogDownloaded(String path){
        logAdapter.Update(path);
    }

    private void displaySearchViews(boolean showSearch){
        showingSearchViews = showSearch;

        if(showingSearchViews){
            logList.setAdapter(searchAdapter);
        } else {
            logList.setAdapter(logAdapter);
        }
    }


    private void handleIntent(Intent intent) {
        if (!Intent.ACTION_SEARCH.equals(intent.getAction())) {
            return;
        }

        searchAdapter.clear();

        final String query = intent.getStringExtra(SearchManager.QUERY);
        Timber.d("searching for: " + query);

        Observable.defer(() -> Observable.just(SearchForText(query, _file)))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(r -> {

                    if (r.Results.size() > 0) {
                        searchAdapter.addAll(r.Results);
                    } else {
//                        setEmptyText("No matches found for '" + query + "'");
                    }

                }, e -> Timber.e(e, "Failed to search"));
    }

    private static LogSearchResults SearchForText(String query, File file) {

        long startTime = System.currentTimeMillis();

        try {
            char[] needle = query.toCharArray();

            BufferedRandomAccessFile f = new BufferedRandomAccessFile(new RandomAccessFile(file, "r"));

            return BoyerMooreSearch.search(f, needle);

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            Timber.d("Search took " + TimeUtil.human(System.currentTimeMillis() - startTime) + " for " + query);
        }
    }

    @Override
    public void onClick(View v, int position) {
        if(!showingSearchViews){
            return;
        }

        LogSearchResults.LogSearchResult result = searchAdapter.getItem(position);
        final int positionForOffset = logAdapter.getPositionForOffset(result.offset);

        searchMenuItem.collapseActionView();

        logList.postDelayed(() -> logList.scrollToPosition(positionForOffset), 0);
    }
}
