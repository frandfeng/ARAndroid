package com.jhqc.vr.travel.struct;

/**
 * Created by Solomon on 2017/10/17 0017.
 */

public class Entry<K, V> {

    K key;

    V value;

    public Entry(K key, V value) {
        this.setKey(key);
        this.setValue(value);
    }

    public K getKey() {
        return key;
    }

    public void setKey(K key) {
        this.key = key;
    }

    public V getValue() {
        return value;
    }

    public void setValue(V value) {
        this.value = value;
    }
}
