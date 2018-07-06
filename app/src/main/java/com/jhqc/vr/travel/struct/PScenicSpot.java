package com.jhqc.vr.travel.struct;

/**
 * Created by Solomon on 2017/10/17 0017.
 */

public class PScenicSpot {
    /**
     * id : 110001
     * name : 船坞
     * location : 116.275675,40.006042
     * icon : beichuanwu
     * detail :         陛下，颐和园的船坞是国内现存面积最大、保存最完好的船坞建筑，颐和园大船坞堪称园中一宝。历经百年风雨，这座船坞已经残破不堪，经过修葺后的大船坞已经重现昔日的风采。         大船坞位于颐和园万寿山西麓，小苏州河以北，是皇家泊船的地方。据了解，乾隆修建清漪园时便在园中建起一座船坞，它宽18米，距水面7米，进深50米，跨度超过了故宫的三大殿，当时皇家最大的游船“昆明喜龙号”就停泊在这里。1860年，这座船坞被英法联军焚毁。后来慈禧在清漪园原址修建颐和园，除复建原有船坞外，还在旁边新建了两座小船坞。修葺时，挑开了大船坞屋顶，更换因漏雨而腐朽的木椽；把已经歪闪的梁柱进行打光拨正，并用铁箍加固；屋顶及四壁的彩画也经过重新油饰。
     */

    private int id;

    private String name;

    private String location;

    private String icon;

    private String detail;

    private String deviceID;

    private String uuid;

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

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getDeviceID() {
        return deviceID;
    }

    public String getUuid() {
        return uuid;
    }

    public PScenicSpot setUuid(String uuid) {
        this.uuid = uuid;
        return this;
    }

    public PScenicSpot setDeviceID(String deviceID) {
        this.deviceID = deviceID;
        return this;
    }

    @Override
    public String toString() {
        return "PScenicSpot{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", location='" + location + '\'' +
                ", icon='" + icon + '\'' +
                ", detail='" + detail + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof PScenicSpot) {
            return this.getId() == ((PScenicSpot) obj).getId();
        }
        return super.equals(obj);
    }

}