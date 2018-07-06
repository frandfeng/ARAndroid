package com.jhqc.vr.travel.struct;

import java.util.ArrayList;

/**
 * Created by Solomon on 2017/10/17 0017.
 */

public class PConfig {

    private ArrayList<PScenicSpot> datas;

    public ArrayList<PScenicSpot> getDatas() {
        return datas;
    }

    public void setDatas(ArrayList<PScenicSpot> datas) {
        this.datas = datas;
    }

    @Override
    public String toString() {
        return "PConfig{" +
                "datas=" + datas +
                '}';
    }
}
