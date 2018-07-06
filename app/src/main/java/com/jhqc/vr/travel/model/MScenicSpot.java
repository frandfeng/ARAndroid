package com.jhqc.vr.travel.model;

/**
 * Created by Solomon on 2017/10/17 0017.
 * 景点Model
 */

public class MScenicSpot {

    /* 景点ID */
    private int id;

    /* 景点名 */
    private String name;

    /* 景点经度 */
    private float longitude;

    /* 景点纬度 */
    private float latitude ;

    /* 景点图标文件名称 */
    private String iconFileName;

    /* 景点介绍 */
    private String des;

    /* 定位设备ID */
    private String deviceID;

    /* 定位设备UUID */
    private String uuid;

    public boolean isPlayed;

    public boolean isNear;

    public boolean isNearest;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getLongitude() {
        return longitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    public float getLatitude() {
        return latitude;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public String getIconFileName() {
        return iconFileName;
    }

    public void setIconFileName(String iconFileName) {
        this.iconFileName = iconFileName;
    }

    public String getDes() {
        return des;
    }

    public void setDes(String des) {
        this.des = des;
    }

    public String getDeviceID() {
        return deviceID;
    }

    public MScenicSpot setDeviceID(String deviceID) {
        this.deviceID = deviceID;
        return this;
    }

    public String getUuid() {
        return uuid;
    }

    public MScenicSpot setUuid(String uuid) {
        this.uuid = uuid;
        return this;
    }

    @Override
    public String toString() {
        return "MScenicSpot{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", longitude=" + longitude +
                ", latitude=" + latitude +
                ", iconFileName='" + iconFileName + '\'' +
                ", des='" + des + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof MScenicSpot) {
            return this.getId() == ((MScenicSpot) obj).getId();
        }
        return super.equals(obj);
    }
}
