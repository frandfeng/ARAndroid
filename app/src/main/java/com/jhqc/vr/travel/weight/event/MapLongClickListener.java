package com.jhqc.vr.travel.weight.event;

/**
 * Created by Solomon on 2017/10/18 0018.
 * 自定义 地图 长按 事件
 */
public interface MapLongClickListener {

    /**
     * 手指 按下 相对 与 地图左上角 原点  原始的 x ，y 坐标
     *
     * @param downX x坐标
     * @param downY y 坐标
     */
    void onLongClick(float downX, float downY);
}