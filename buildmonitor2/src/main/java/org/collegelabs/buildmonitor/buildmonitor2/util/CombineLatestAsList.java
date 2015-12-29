package org.collegelabs.buildmonitor.buildmonitor2.util;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;

public class CombineLatestAsList {

    public static <T> Observable create(List<? extends Observable<? extends T>> sources){

        // TODO OnSubscribeCombineLatest uses the ring buffer as its max size (16 on android)
        // will either need to increase it, or use a different observable for the main activity

        // TODO this probably isn't too nice re: GC, it won't be good for
        // frequently changing observables, but the main activity is only ~1/min
        return Observable.combineLatest(sources, items -> {
            ArrayList<T> summaries = new ArrayList<>(items.length);

            for (Object obj : items) {
                summaries.add((T) obj);
            }

            return summaries;
        });
    }
}
