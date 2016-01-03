package org.collegelabs.buildmonitor.buildmonitor2.util;

import java.util.HashMap;

import rx.functions.Func1;

public class Linq {

    public static <K,V,E> HashMap<K, V> toMap(Iterable<E> items, Func1<E, K> keySelector, Func1<E, V> valueSelector){
        HashMap<K, V> map = new HashMap<>();

        for(E item : items){
            map.put(keySelector.call(item), valueSelector.call(item));
        }

        return map;
    }


    public static <K, E> HashMap<K, E> toMap(Iterable<E> items, Func1<E, K> keySelector){
        HashMap<K, E> map = new HashMap<>();

        for(E item : items){
            map.put(keySelector.call(item), item);
        }

        return map;
    }
}
