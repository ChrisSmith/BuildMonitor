package org.collegelabs.buildmonitor.buildmonitor2.builds;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
import android.view.*;
import android.widget.*;

import butterknife.ButterKnife;
import butterknife.InjectView;
import com.jakewharton.rxbinding.widget.RxAdapterView;
import com.jakewharton.rxbinding.widget.RxSearchView;

import org.collegelabs.buildmonitor.buildmonitor2.BuildMonitorApplication;
import org.collegelabs.buildmonitor.buildmonitor2.R;
import org.collegelabs.buildmonitor.buildmonitor2.storage.BuildTypeDto;
import org.collegelabs.buildmonitor.buildmonitor2.tc.BuildTypeCollectionResponse;
import org.collegelabs.buildmonitor.buildmonitor2.tc.Credentials;
import org.collegelabs.buildmonitor.buildmonitor2.tc.ServiceHelper;
import org.collegelabs.buildmonitor.buildmonitor2.tc.TeamCityService;
import org.collegelabs.buildmonitor.buildmonitor2.tc.models.BuildType;
import org.collegelabs.buildmonitor.buildmonitor2.util.RxUtil;
import org.collegelabs.buildmonitor.buildmonitor2.util.ToastUtil;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class EditBuildActivity extends Activity {

    @InjectView(R.id.edit_build_account) public Spinner _spinner;
    @InjectView(R.id.edit_build_add_server) public ImageButton _addServer;
    @InjectView(R.id.edit_build_displayname) public EditText _displayname;
    @InjectView(android.R.id.list) public ListView _listView;
    @InjectView(R.id.edit_build_toolbar) public Toolbar _toolbar;

    private CredentialAdapter _credentialAdapter;
    private BuildTypeAdapter _buildTypeAdapter;
    private List<Subscription> _subscriptions = new ArrayList<>();
    private Subscription _buildTypesSub;
    private List<BuildType> _buildTypes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_build_activity);
        ButterKnife.inject(this);

        _toolbar.findViewById(R.id.actionbar_discard).setOnClickListener(v -> finish());
        _toolbar.setTitle("");
        setActionBar(_toolbar);

        _buildTypeAdapter = new BuildTypeAdapter(this);
        _listView.setAdapter(_buildTypeAdapter);

        _credentialAdapter = new CredentialAdapter(this);
        _spinner.setAdapter(_credentialAdapter);
        _addServer.setOnClickListener(c -> showAddServerDialog());

        _subscriptions.add(BuildMonitorApplication.Db.GetAllCredentials()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::credentialsChange, e -> {
                    ToastUtil.show(this, "Failed to get credentials");
                    Timber.e(e, "Failed to get credentials");
                }));

        _subscriptions.add(RxAdapterView.itemSelections(_spinner)
                .filter(id -> id != AdapterView.INVALID_POSITION)
                .subscribe(this::spinnerSelected, e -> {
                    ToastUtil.show(this, "Failed to select spinner");
                    Timber.e(e, "Failed to select spinner");
                }));
    }

    private void spinnerSelected(int position) {
        RxUtil.unsubscribe(_buildTypesSub);

        Credentials item = _credentialAdapter.getItem(position);
        TeamCityService service = ServiceHelper.getService(item);

        _buildTypesSub = service.getBuildTypes()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .startWith(new BuildTypeCollectionResponse())
                .subscribe(this::UpdateUI, e -> {
                    ToastUtil.show(this, "Failed to load build types");
                    Timber.e(e, "Failure getting build types");
                });
    }

    private void showAddServerDialog() {
        EditCredentialsDialog dialog = new EditCredentialsDialog();
        dialog.show(getFragmentManager(), "credentialsDialog");
    }

    private void credentialsChange(List<Credentials> credentials) {
        _credentialAdapter.clear();
        _credentialAdapter.addAll(credentials);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RxUtil.unsubscribe(_subscriptions);
        RxUtil.unsubscribe(_buildTypesSub);
        _buildTypesSub = null;
    }

    private void UpdateUI(BuildTypeCollectionResponse buildTypeCollectionResponse) {
        setVisibleBuilds(buildTypeCollectionResponse.buildTypes);
        _buildTypes = new ArrayList<>(buildTypeCollectionResponse.buildTypes);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_build_menu, menu);
        SearchView searchItem = (SearchView) menu.findItem(R.id.action_search).getActionView();
        setupSearchListener(searchItem);

        return true;
    }

    private void setupSearchListener(SearchView searchItem) {

        _subscriptions.add(
                RxSearchView.queryTextChanges(searchItem)
                .subscribeOn(AndroidSchedulers.mainThread())
                .debounce(50, TimeUnit.MILLISECONDS)
                .map(query -> Pair.create(query.toString(), new ArrayList<>(_buildTypes)))
                .map(pair -> {

                    if(pair.first.length() == 0){
                        return pair.second;
                    }

                    String query = pair.first.toLowerCase();
                    List<BuildType> items = new ArrayList<>();
                    for(BuildType b : pair.second){
                        if(b.name.toLowerCase().contains(query)
                            || b.projectName.toLowerCase().contains(query)){
                            items.add(b);
                        }
                    }
                    return items;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::setVisibleBuilds,
                e -> {
                    Timber.e(e, "Search failed");
                }));
    }

    private void setVisibleBuilds(List<BuildType> builds) {
        _listView.clearChoices();
        _buildTypeAdapter.clear();
        _buildTypeAdapter.addAll(builds);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_save_build) {
            saveBuild();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void saveBuild() {
        int position = _spinner.getSelectedItemPosition();
        if(position == AdapterView.INVALID_POSITION){
            ToastUtil.show(this, "Please enter server credentials");
            return;
        }
        final Credentials credentials = _credentialAdapter.getItem(position);

        position = _listView.getCheckedItemPosition();
        if(position == AdapterView.INVALID_POSITION){
            ToastUtil.show(this, "Please select a build type");
            return;
        }

        final BuildType buildType = _buildTypeAdapter.getItem(position);
        String displayName = _displayname.getText().toString();
        if(displayName.length() == 0){
            displayName = buildType.name;
        }

        final BuildTypeDto dto = new BuildTypeDto(displayName, buildType, credentials);

        try {
            BuildMonitorApplication.Db.insertBuildType(dto);
            finish();
        }catch (Exception e){
            ToastUtil.show(this, "Failed to save changes");
            Timber.e(e, "Failed to insert buildType");
        }
    }


    public static Intent getIntent(Context context){
        return new Intent(context, EditBuildActivity.class);
    }
}
