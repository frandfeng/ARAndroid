package com.jhqc.vr.travel.weight.event.support;

import android.graphics.Matrix;
import android.graphics.PointF;

import com.jhqc.vr.travel.util.LogUtils;

/**
 * Created by Solomon on 2017/10/19 0019.
 */

public class MapMatrix extends Matrix {

    /**
     * 总宽高
     */
    PointF realPoint;

    /**
     * 图片控件宽高
     */
    PointF viewPoint;

    /**
     * Left Top
     */
//    PointF ltPoint;

    /**
     * 右下角位置
     * Right Bottom
     */
    PointF rbPoint;

    public MapMatrix() {
        realPoint = new PointF();
        viewPoint = new PointF();
//        ltPoint = new PointF();
        rbPoint = new PointF();
    }

    public void setRealPoint(float xLenth, float yLenth) {
        this.realPoint.x = xLenth;
        this.realPoint.y = yLenth;
    }

    public void setViewPoint(float xLenth, float yLenth) {
        this.viewPoint.x = xLenth;
        this.viewPoint.y = yLenth;
    }

    public void setRBPoint(float x, float y) {
        LogUtils.logView("setRBPoint:" + x + " " + y);
        this.rbPoint.x = x;
        this.rbPoint.y = y;
    }

    public PointF getRealPoint() {
        return realPoint;
    }

    public MapMatrix setRealPoint(PointF realPoint) {
        this.realPoint = realPoint;
        return this;
    }

    public PointF getViewPoint() {
        return viewPoint;
    }

    public MapMatrix setViewPoint(PointF viewPoint) {
        this.viewPoint = viewPoint;
        return this;
    }

    public PointF getRbPoint() {
        return rbPoint;
    }

    public MapMatrix setRbPoint(PointF rbPoint) {
        this.rbPoint = rbPoint;
        return this;
    }
}
