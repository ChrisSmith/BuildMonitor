package org.collegelabs.buildmonitor.buildmonitor2.util;

import rx.Subscription;

import java.util.List;

/**
 */
public class RxUtil {

    public static void unsubscribe(List<Subscription> subscriptions) {
        for(Subscription s : subscriptions){
            unsubscribe(s);
        }
        subscriptions.clear();
    }

    public static void unsubscribe(Subscription s) {
        if(s != null && !s.isUnsubscribed()){
            s.unsubscribe();
        }
    }
}
