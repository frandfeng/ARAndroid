package com.jhqc.vr.travel.unity.model;

import com.google.gson.reflect.TypeToken;

/**
 * Created by Solomon on 2017/10/29 0029.
 */

public class UARScan {

    public int state = 0;

    public String identifiedName;

    public UARScan(int state, String name) {
        this.state = state;
        this.identifiedName = name;
    }
}
