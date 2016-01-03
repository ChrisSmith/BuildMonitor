package org.collegelabs.buildmonitor.buildmonitor2.buildstatus;

import org.collegelabs.buildmonitor.buildmonitor2.tc.TeamCityService;
import org.collegelabs.buildmonitor.buildmonitor2.tc.models.Build;
import org.collegelabs.buildmonitor.buildmonitor2.tc.models.BuildDetailsResponse;
import org.collegelabs.buildmonitor.buildmonitor2.util.RxUtil;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;

public class BuildChainObservable implements Observable.OnSubscribe<BuildDetailsResponse> {

    public static Observable<BuildDetailsResponse> create(TeamCityService service, int fromBuildId){
        return Observable.create(new BuildChainObservable(service, fromBuildId));
    }

    private final TeamCityService _service;
    private final int _originalBuildId;


    private Object _lock = new Object();
    private HashMap<Integer, Subscription> _requests = new HashMap<>();

    public BuildChainObservable(TeamCityService service, int fromBuildId){
        _service = service;
        _originalBuildId = fromBuildId;
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

            _requests.put(buildId, _service.getBuild(buildId)
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
            addBuild(subscriber, b.id);
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
