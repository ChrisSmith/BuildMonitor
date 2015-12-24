package org.collegelabs.buildmonitor.buildmonitor2;

import android.app.Activity;
import android.os.Bundle;
import android.util.TimeUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import org.collegelabs.buildmonitor.buildmonitor2.tc.Credentials;
import org.collegelabs.buildmonitor.buildmonitor2.tc.ServiceHelper;
import org.collegelabs.buildmonitor.buildmonitor2.tc.TeamCityService;
import org.collegelabs.buildmonitor.buildmonitor2.tc.models.Build;
import org.collegelabs.buildmonitor.buildmonitor2.tc.models.BuildCollectionResponse;
import org.collegelabs.buildmonitor.buildmonitor2.util.TimeUtil;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.Observable;
import timber.log.Timber;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;

public class MainActivity extends Activity {

    @InjectView(R.id.main_textview_status) TextView _status;
    @InjectView(R.id.main_textview_summary) TextView _summary;

    private Subscription _sub;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        showLoadingView(); //set view model of some sort?
    }

    @Override
    protected void onStart() {
        super.onStart();



        TeamCityService service = ServiceHelper.getService(credentials);


        // TODO recursion so it blocks until the previous completes?
        // http://stackoverflow.com/questions/24557153/rx-subject-and-emitting-values-periodically

        int pageSize = 100;
        int offset = 0;
        String buildTypeId = "bt312";
        String buildLocator = "buildType:" + buildTypeId + ",running:any,canceled:any,count:" + pageSize + ",start:" + offset;;

        _sub = Observable.interval(/** initial delay */ 0,  /** interval */ 60, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.newThread())
                .flatMap(x -> service.getBuilds(buildLocator))
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
        _summary.setText("");
        _status.setText("Loading...");
    }

    private void UpdateUI(BuildCollectionResponse response) {
        String status;
        String summary = "";

        if(response.builds.size() == 0){
            status = "No Builds";
        }else{
            Build first = response.builds.get(0);
            summary = TimeUtil.human(first.startDate);

            if(first.running){
                summary += " " + first.percentageComplete + "%";

                switch (first.status){
                    case "FAILURE":
                        status = "NOPE.";
                        break;
                    case "SUCCESS":
                        status = "HASN'T FAILED YET.";
                        break;
                    default:
                        status = "???";
                }
            }else{
                switch (first.status){
                    case "FAILURE":
                        status = "NOPE.";
                        break;
                    case "SUCCESS":
                        status = "YUP!";
                        break;
                    default:
                        status = "???";
                }
            }
        }

        _status.setText(status);
        _summary.setText(summary);
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
}
