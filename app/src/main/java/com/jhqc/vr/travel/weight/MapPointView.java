package com.jhqc.vr.travel.weight;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jhqc.AR_3D.R;
import com.jhqc.vr.travel.model.MScenicSpot;
import com.jhqc.vr.travel.util.LogUtils;
import com.jhqc.vr.travel.util.ViewUtils;

/**
 * 自定义 地图 坐标 点
 * Created by Administrator on 2016/6/30.
 */
public class MapPointView extends LinearLayout {

    public static final int MARK_PERSON = 4;
    public static final int MARK_POINT = 5;

    public static final int TRAVEL_BIGGER_BLUE = 6;
    public static final int TRAVEL_BIGGER_ORIG = 7;

    public static final int TRAVEL_MUSICE_BLUE = 8;
    public static final int TRAVEL_MUSICE_ORIG = 9;

    public static final int TRAVEL_MUSICE_PLAY_BLUE = 10;
    public static final int TRAVEL_MUSICE_PLAY_ORIG = 11;

    private ImageView pointIcon;
    private TextView pointTitle;

    private Context context;
    // 在原图上初始 位置
    private double firstX;
    private double firstY;

    // 点的 边界
    private double borderTop;
    private double borderLeft;
    // 点 的 显示 状态
    private int type = TRAVEL_MUSICE_BLUE;
    // 点 的 名称
    private String title;
    // 是否 显示 点 名称
    private boolean isTitleShow;

    private Object object;

    public MapPointView(Context context) {
        super(context);
        init();
    }

    public MapPointView(Context context, double pointX, double pointY, int type, boolean isTitleShow, String title) {
        super(context);
        this.context = context;
        this.firstX = pointX;
        this.firstY = pointY;
        this.type = type;
        this.title = title;
        this.isTitleShow = isTitleShow;
        init();
    }

    public void release() {
        ViewUtils.releaseImageViewResource(pointIcon);
        ViewUtils.releaseBackgroundDrawable(pointIcon);
        ViewUtils.releaseBackgroundDrawable(pointTitle);
        this.setOnClickListener(null);
    }

    public static class Builder {
        public static MapPointView build(Context context, double pointX, double pointY, int pointType, boolean isTitleShow, String title) {
            LogUtils.logView("Builder.build(Point):"+pointX + "  " + pointY);
           return new MapPointView(context, pointX, pointY, pointType, isTitleShow, title);
        }
    }

    public void setFirstXShow(float x) {
        x -= borderLeft;
        setX(x);
    }

    public void setFirstYShow(float y) {
        y -= borderTop;
        setY(y);
    }

    public void init() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.view_mymap_point, this);
        pointIcon = (ImageView) view.findViewById(R.id.pointIcon);
        pointTitle = (TextView) view.findViewById(R.id.pointTitle);

        // 设置 显示 内容
        setPointIcon();
        pointTitle.setText(title);

        // 测量 边界
        measureBorder();
    }

    /**
     * 测量 地图点 的 边界
     */
    private void measureBorder() {
        int height = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        int width = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);

        this.pointIcon.measure(width, height);
        this.pointTitle.measure(width, height);
        this.measure(width, height);

        if (!isTitleShow) {
            pointTitle.setVisibility(INVISIBLE);
        } else {
            pointTitle.setVisibility(VISIBLE);
        }

        this.borderLeft = (this.getMeasuredWidth() - this.pointIcon.getMeasuredWidth()) / 2;
        this.borderTop = (this.getMeasuredHeight() - this.pointIcon.getMeasuredHeight() - this.pointTitle.getMeasuredHeight());

        Log.i("testss", this.borderLeft + "fffffff=========" + this.borderTop);
    }

    public void updatePointType(int type) {
        if (this.type != type) {
            this.type = type;
            setPointIcon();
        }
    }

    /**
     * 设置 显示 图标
     */
    private void setPointIcon() {
        // 默认 点显示 在 左上角的位置
        int resID;
        switch (this.type) {
            case MARK_PERSON:
                resID = R.drawable.pointicon_people;
                break;
            case TRAVEL_BIGGER_BLUE:
                resID = R.drawable.bigger_blue;
                break;
            case TRAVEL_BIGGER_ORIG:
                resID = R.drawable.bigger_orig;
                break;
            case TRAVEL_MUSICE_BLUE:
                resID = R.drawable.music_blue;
                break;
            case TRAVEL_MUSICE_ORIG:
                resID = R.drawable.music_orig;
                break;
            case TRAVEL_MUSICE_PLAY_BLUE:
                resID = R.drawable.playing_anim_blue;
                break;
            case TRAVEL_MUSICE_PLAY_ORIG:
                resID = R.drawable.playing_anim_orig;
                break;
            case MARK_POINT:
                resID = R.drawable.loction;
                break;
            default:
                resID = R.drawable.music_blue;
                break;
        }

        this.pointIcon.setImageResource(resID);
        if (this.pointIcon.getDrawable() != null && this.pointIcon.getDrawable() instanceof AnimationDrawable) {
            AnimationDrawable animationDrawable = ((AnimationDrawable)this.pointIcon.getDrawable());
            animationDrawable.setOneShot(false);
            animationDrawable.start();
        }
    }

    public MapPointView setFirstX(double firstX) {
        this.firstX = firstX;
        return this;
    }

    public MapPointView setFirstY(double firstY) {
        this.firstY = firstY;
        return this;
    }

    public double getFirstX() {
        return firstX;
    }

    public double getFirstY() {
        return firstY;
    }

    public float getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        this.pointTitle.setText(title);
    }

    public Object getObject() {
        return object;
    }

    public MapPointView setObject(Object object) {
        this.object = object;
        return this;
    }
}
