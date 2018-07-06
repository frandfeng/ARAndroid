package com.jhqc.vr.travel.weight;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.jhqc.vr.travel.util.ViewUtils;

/**
 * Created by Solomon on 2017/11/3 0003.
 */
public class MarqueeTextView extends android.support.v7.widget.AppCompatTextView implements Runnable {
    private static final String TAG = "MarqueeTextView";

    // 设置跑马灯重复的次数，次数
    private int circleTimes = 1;

    //记录已经重复了多少遍
    private int hasCircled = 0;

    private int currentScrollPos = 0;

    // 跑马灯走一遍需要的时间（秒数）
    private int circleSpeed = 10;

    // 文字的宽度
    private int textWidth = 0;

    private boolean isMeasured = false;

    private boolean isStop = false;

    public MarqueeTextView(Context context) {
        super(context);
        this.removeCallbacks(this);
    }

    public MarqueeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.removeCallbacks(this);

    }

    /**
     * 画笔工具
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (!isMeasured) {
            getTextWidth();
            isMeasured = true;
        }
    }

    @Override
    public void setVisibility(int visibility) {
        // 二次进入时初始化成员变量
        isStop = false;
        isMeasured = false;
        this.hasCircled = 0;
        super.setVisibility(visibility);
    }

    @Override
    public void run() {
        // 起始滚动位置
        currentScrollPos += 1;
        scrollTo(currentScrollPos, 0);
        // Log.i(TAG, "pos"+currentScrollPos);
        // 判断滚动一次
        if (currentScrollPos >= textWidth) {
            // 从屏幕右侧开始出现
            currentScrollPos = -this.getWidth();
            //记录的滚动次数大设定的次数代表滚动完成，这个控件就可以隐藏了
            if (hasCircled >= this.circleTimes) {
                this.setVisibility(View.GONE);
                isStop = true;
            }
            hasCircled += 1;
        }
        if (this.getVisibility() == INVISIBLE) {
            setVisibility(VISIBLE);
        }

        if (!isStop) {
            // 滚动时间间隔
            postDelayed(this, circleSpeed);
        }
    }

    String text = "";

    public void setString(String str) {
        this.text = str;
        setText(text);
        this.setVisibility(INVISIBLE);
    }

    /**
     * 获取文本显示长度
     */

    private void getTextWidth() {
        Paint paint = this.getPaint();
        String str = this.text.toString();
        Log.i(TAG, str);
        if (str == null) {
            textWidth = 0;
        }
        textWidth = (int) paint.measureText(str);
    }

    /**
     * 设置滚动次数，达到次数后设置不可见
     *
     * @param circleTimes
     */
    public void setCircleTimes(int circleTimes) {
        this.circleTimes = circleTimes;
    }

    public void setSpeed(int speed) {
        this.circleSpeed = speed;
    }

    public void startScroll() {
//        if (this.getVisibility() == View.GONE) {
//            this.setVisibility(View.VISIBLE);
//        }
        this.removeCallbacks(this);
        post(this);
    }

    public void stopScroll() {
        isStop = false;
    }
}