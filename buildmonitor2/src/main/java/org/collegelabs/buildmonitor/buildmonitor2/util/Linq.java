package org.collegelabs.buildmonitor.buildmonitor2.util;

import org.apache.commons.csv.CSVParser;
import org.collegelabs.buildmonitor.buildmonitor2.tests.TestCsvParser;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

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

    public static <E> Iterable<E> where(Iterable<E> items, Func1<E, Boolean> filter){

        return () -> new Iterator<E>() {

            Iterator<E> source = items.iterator();
            E next = null;
            boolean nextIsSet = false;

            @Override
            public boolean hasNext() {
                if(nextIsSet){
                    return true;
                }

                while(source.hasNext()){
                    next = source.next();

                    if(filter.call(next)){
                        nextIsSet = true;
                        return true;
                    }
                }

                return false;
            }

            @Override
            public E next() {
                if(hasNext()){
                    nextIsSet = false;
                    return next;
                }else{
                    throw new NoSuchElementException();
                }
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("remove is not supported");
            }
        };
    }


    public static <E> ArrayList<E> toList(Iterable<E> items){
        ArrayList<E> result = new ArrayList<>();
        for(E item : items){
            result.add(item);
        }

        return result;
    }

    public static <S, R> ArrayList<R> toList(Iterable<S> source, Func1<S, R> transform) {
        ArrayList<R> result = new ArrayList<>();
        for(S item : source){
            result.add(transform.call(item));
        }
        return result;
    }


    public static <S, K> HashMap<K, List<S>> groupBy(Iterable<S> source, Func1<S, K> keySelector){
        return groupBy(source, keySelector, v -> v);
    }

    public static <S, K, V> HashMap<K, List<V>> groupBy(Iterable<S> source, Func1<S, K> keySelector, Func1<S, V> valueSelector){
        HashMap<K, List<V>> result = new HashMap<>();
        for(S item : source){
            K key = keySelector.call(item);

            if(!result.containsKey(key)){
                result.put(key, new ArrayList<>());
            }

            List<V> list = result.get(key);
            V value = valueSelector.call(item);
            list.add(value);
        }
        return result;
    }
}
