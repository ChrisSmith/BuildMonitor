package org.collegelabs.buildmonitor.buildmonitor2.buildstatus;

import org.collegelabs.buildmonitor.buildmonitor2.builds.BuildStatus;
import org.collegelabs.buildmonitor.buildmonitor2.tc.TcUtil;
import org.collegelabs.buildmonitor.buildmonitor2.tc.TeamCityService;
import org.collegelabs.buildmonitor.buildmonitor2.tc.models.Build;
import org.collegelabs.buildmonitor.buildmonitor2.tc.models.BuildDetailsResponse;
import org.collegelabs.buildmonitor.buildmonitor2.util.RxUtil;

import java.util.HashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Func1;
import timber.log.Timber;

public class BuildChainObservable implements Observable.OnSubscribe<BuildDetailsResponse> {

    public static Observable<BuildDetailsResponse> create(TeamCityService service, int fromBuildId, Func1<Build, Boolean> filter){
        return Observable.create(new BuildChainObservable(service, fromBuildId, filter));
    }

    public static Observable<BuildDetailsResponse> create(TeamCityService service, int fromBuildId){
        return create(service, fromBuildId, b -> true);
    }

    private final TeamCityService _service;
    private final int _originalBuildId;
    private final Func1<Build, Boolean> _buildFilter;
    
    private static Semaphore _sem = new Semaphore(10);

    private Object _lock = new Object();
    private HashMap<Integer, Subscription> _requests = new HashMap<>();

    public BuildChainObservable(TeamCityService service, int fromBuildId, Func1<Build, Boolean> buildFilter){
        _service = service;
        _originalBuildId = fromBuildId;
        _buildFilter = buildFilter;
    }

    //TODO for TC 9+ we can use this build locator to grab all builds in one request snapshotDependency:(to:(id:sqliteBuildId)


    @Override
    public void call(Subscriber<? super BuildDetailsResponse> subscriber) {
        addBuild(subscriber, _originalBuildId);

        subscriber.add(new Subscription() {
            private final AtomicBoolean unsubscribed = new AtomicBoolean();

            @Override
            public void unsubscribe() {
                if(unsubscribed.compareAndSet(false, true)){
                    cancelOutstanding();
                }
            }

            @Override
            public boolean isUnsubscribed() {
                return unsubscribed.get();
            }
        });
    }

    private void addBuild(Subscriber<? super BuildDetailsResponse> subscriber, int buildId) {
        synchronized (_lock){
            if(_requests.containsKey(buildId)){
                return;
            }

            try {
                // TODO remove this hack. I need a better way to throttle requests
                _sem.acquire();
            } catch (InterruptedException e) {
                subscriber.onError(e);
            }

            _requests.put(buildId, _service.getBuild(buildId)
                    .doOnEach(b -> _sem.release())
                    .map(b -> toBuild(subscriber, b))
                    .subscribe(
                            b -> {
                                removeBuild(buildId);
                                if(subscriber.isUnsubscribed()){
                                    cancelOutstanding();
                                    return;
                                }

                                subscriber.onNext(b);
                            },
                            e -> {
                                removeBuild(buildId);
                                subscriber.onError(e);
                            }
                    ));
        }
    }

    private void removeBuild(int buildId) {
        synchronized (_lock){
            _requests.put(buildId, null);
        }
    }

    private BuildDetailsResponse toBuild(Subscriber<? super BuildDetailsResponse> subscriber, BuildDetailsResponse response) {

        for(Build b : response.snapshotDependencies.builds){
            if(_buildFilter.call(b)){
                addBuild(subscriber, b.id);
            }
        }

        return response;
    }

    private void cancelOutstanding() {
        synchronized (_lock){
            for(Integer key : _requests.keySet()){
                Subscription s = _requests.get(key);
                RxUtil.unsubscribe(s);
            }
            _requests.clear();
        }
    }
}
