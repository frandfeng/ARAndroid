package com.jhqc.vr.travel.util;

import android.graphics.PointF;
import android.support.annotation.Size;

import com.jhqc.vr.travel.manager.ConfigManager;
import com.jhqc.vr.travel.model.MConfig;

import java.io.Console;

/**
 * Created by Solomon on 2017/10/19 0019.
 */

public class LoctionConvertUtils {

    /**获取对应经度和纬度在图片上的位置坐标
     * @param pf  初始宽高
     * @param ll
     * @param config
     * @return
     */
    @Deprecated
    public static @Size float[] getXYbyOffset(PointF pf, @Size float[] ll, MConfig config) {
        float[] xy = new float[2];
        xy[0] = ll[0] / ConfigManager.DEFAULT_MUTI ;
        xy[1] = ll[1] / ConfigManager.DEFAULT_MUTI;

        LogUtils.logLoc("定位位置：" + xy[0] + "  " + xy[1]);
        return xy;
    }

    /**获取对应经度和纬度在图片上的位置坐标
     * @param pf  初始宽高
     * @param ll
     * @param config
     * @return
     */
    public static @Size float[] getXYbyLL(PointF pf, @Size float[] ll, MConfig config) {
        LogUtils.logArith("---------------");
        float[] xy = new float[2];

        /** 注：locBottomR[1]是纬度：lat,  locTopL[0]是经度：lon */
        float[] locBottomR = config.getLocBottomR();
        float[] locTopL = config.getLocTopL();

        /** 经纬度区间值 [lat, long]*/
        float[] offsetLatLong = getBaseOffsetLatLong(locTopL, locBottomR);
        LogUtils.logArith("经纬度区间值:" + offsetLatLong[0] +" " + offsetLatLong[1]);
        LogUtils.logArith("经纬度坐标："  + ll[0] + "  " + ll[1]);

        /** 注：locTopL[1]是纬度：lat,  locTopL[0]是经度：lon */
        /** y轴坐标上边点, 因为y轴是从下往上递增 */
        float baseLat = locTopL[1];
        /** x轴坐标左边点 */
        float baseLong = locTopL[0];

        /** y坐标比例 ： 上边最大y - 点y的y / 范围 */
        float scaleLat = (/*offsetLatLong[0] - */(baseLat - ll[0])) / offsetLatLong[0];
        /** x坐标比例 ： 点x的x - 左边起始x / 范围 */
        float scaleLon = (/*offsetLatLong[1] - */(ll[1] - baseLong)) / offsetLatLong[1];
        LogUtils.logArith("经纬度减去基准距离："  + (baseLat - ll[0]) + "  " + (ll[1] - baseLong));
        LogUtils.logArith("经纬度在范围内缩放后比例："  + scaleLat + "  " + scaleLon);

        xy[0] = scaleLon * pf.x;
        xy[1] = scaleLat * pf.y;

        LogUtils.logArith("计算后图片上坐标：" + xy[0] + "  " + xy[1]);
        LogUtils.logArith("---------------");
        return xy;
    }

    /**计算一个经纬度与另一个之间在图片上的位置距离相应的坐标
     * @param pf  初始宽高
     * @param a 第一个点
     * @param b 第二个点
     * @return
     */
    public static @Size float[] distanceBettwenXY(PointF pf, @Size float[] a, @Size float[] b, MConfig config) {
        float[] xy = new float[2];

        /** 注：locBottomR[1]是纬度：lat,  locTopL[0]是经度：lon */
        float[] locBottomR = config.getLocBottomR();
        float[] locTopL = config.getLocTopL();

        /** 经纬度区间值 [lat, long]*/
        float[] offsetLatLong = getBaseOffsetLatLong(locTopL, locBottomR);

        /** x坐标比例 ： 点x的x - 左边起始x / 范围 */
        xy[0] = (b[1] - a[1]) / offsetLatLong[1]  * pf.x;
        /** y坐标比例 ： 上边y - 点y的y / 范围 */
        xy[1] = (a[0] - b[0]) / offsetLatLong[0] * pf.y;

        LogUtils.logArith("经纬度移动：" + (b[0] - a[0]) + "  " + (b[1] - a[1]));
        LogUtils.logArith("图片移动xy：" + xy[0] + "  " + xy[1]);
        return xy;
    }

    /**
     * 从结束经纬度减去开始， 得到区间值数组,返回数组：{latOffset, longOffset}
     * @param locTopL
     * @param locBottomR
     * @return
     */
    private static @Size float[] getBaseOffsetLatLong(@Size float[] locTopL, @Size float[] locBottomR) {
        float offsetLong = locBottomR[0] - locTopL[0];
        float offsetLat = locTopL[1] - locBottomR[1];
        LogUtils.logArith("offsetLat = " + locTopL[1] + " - " + locBottomR[1] +" = " + offsetLat );
        LogUtils.logArith("offsetLong = " + locBottomR[0] + " - " + locTopL[0] +" = " + offsetLong );
        return new float[]{offsetLat, offsetLong};
    }

    /**计算一个经纬度与另一个之间在图片上的位置距离相应的坐标
     * @param a 第一个点
     * @param b 第二个点
     * @return
     */
    public static double distanceBettwenLoc(@Size float[] a, @Size float[] b) {
        float dx = b[0] - a[0];
        float dy = b[1] - a[1];
        /** 使用勾股定理返回两点之间的距离 */
        return (float) Math.sqrt(dx * dx + dy * dy);
        //TODO  经纬度时使用   return Distance(a[0], a[1], b[0], b[1]);
    }

    public static double HaverSin(double theta)
    {
        double v = Math.sin(theta / 2);
        return v * v;
    }


    static double EARTH_RADIUS = 6371.0;//km 地球半径 平均值，千米

    /// <summary>
    /// 给定的经度1，纬度1；经度2，纬度2. 计算2个经纬度之间的距离。
    /// </summary>
    /// <param name="lat1">经度1</param>
    /// <param name="lon1">纬度1</param>
    /// <param name="lat2">经度2</param>
    /// <param name="lon2">纬度2</param>
    /// <returns>距离（公里、千米）</returns>
    public static double Distance(double lat1,double lon1, double lat2,double lon2)
    {
        //用haversine公式计算球面两点间的距离。
        //经纬度转换成弧度
        lat1 = ConvertDegreesToRadians(lat1);
        lon1 = ConvertDegreesToRadians(lon1);
        lat2 = ConvertDegreesToRadians(lat2);
        lon2 = ConvertDegreesToRadians(lon2);

        //差值
        double vLon = Math.abs(lon1 - lon2);
        double vLat = Math.abs(lat1 - lat2);

        //h is the great circle distance in radians, great circle就是一个球体上的切面，它的圆心即是球心的一个周长最大的圆。
        double h = HaverSin(vLat) + Math.cos(lat1) * Math.cos(lat2) * HaverSin(vLon);

        double distance = 2 * EARTH_RADIUS * Math.asin(Math.sqrt(h));

        return distance;
    }

    /// <summary>
    /// 将角度换算为弧度。
    /// </summary>
    /// <param name="degrees">角度</param>
    /// <returns>弧度</returns>
    public static double ConvertDegreesToRadians(double degrees)
    {
        return degrees * Math.PI / 180;
    }

    public static double ConvertRadiansToDegrees(double radian)
    {
        return radian * 180.0 / Math.PI;
    }

}
