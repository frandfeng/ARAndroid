package com.jhqc.vr.travel.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Solomon on 2017/10/17 0017.
 */

public class MConfig {

    private String configID;

    /* 地图JPG图片名称 */
    private String mapFileName;

    /** 左上角经伟度 locTopL[1]是纬度：lat,  locTopL[0]是纬度：lon*/
    private float[] locTopL;

    private float[] locTopR;

    private float[] locBottomL;

    private float[] locBottomR;

    private ArrayList<MScenicSpot> scenicSpotList;

    public String getConfigID() {
        return configID;
    }

    public void setConfigID(String configID) {
        this.configID = configID;
    }

    public String getMapFileName() {
        return mapFileName;
    }

    public void setMapFileName(String mapFileName) {
        this.mapFileName = mapFileName;
    }

    public float[] getLocTopL() {
        return locTopL;
    }

    public void setLocTopL(float[] locTopL) {
        this.locTopL = locTopL;
    }

    public float[] getLocTopR() {
        return locTopR;
    }

    public void setLocTopR(float[] locTopR) {
        this.locTopR = locTopR;
    }

    public float[] getLocBottomL() {
        return locBottomL;
    }

    public void setLocBottomL(float[] locBottomL) {
        this.locBottomL = locBottomL;
    }

    public float[] getLocBottomR() {
        return locBottomR;
    }

    public void setLocBottomR(float[] locBottomR) {
        this.locBottomR = locBottomR;
    }

    public ArrayList<MScenicSpot> getScenicSpotList() {
        return scenicSpotList;
    }

    public void setScenicSpotList(ArrayList<MScenicSpot> scenicSpotList) {
        this.scenicSpotList = scenicSpotList;
    }

    @Override
    public String toString() {
        return "MConfig{" +
                "configID='" + configID + '\'' +
                ", mapFileName='" + mapFileName + '\'' +
                ", locTopL=" + Arrays.toString(locTopL) +
                ", locTopR=" + Arrays.toString(locTopR) +
                ", locBottomL=" + Arrays.toString(locBottomL) +
                ", locBottomR=" + Arrays.toString(locBottomR) +
                ", scenicSpotList=" + scenicSpotList +
                '}';
    }
}
