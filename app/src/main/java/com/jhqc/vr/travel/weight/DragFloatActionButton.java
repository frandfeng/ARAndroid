package com.jhqc.vr.travel.weight;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.jhqc.AR_3D.R;
import com.jhqc.vr.travel.util.ScreenUtils;
import com.jhqc.vr.travel.util.ViewUtils;

/**
 * Created by Solomon on 2017/10/27 0027.
 */

public class DragFloatActionButton extends FrameLayout {

    private int screenWidth;
    private int screenHeight;
    private int screenWidthHalf;
    private int statusHeight;

    static final int PADDING = 12;

    ImageView imageView;

    ImageView coverImageView;

    MarqueeTextView textView;

    TouchEventListener touchEventListener;

    public DragFloatActionButton(Context context) {
        super(context);
        init();
    }

    public DragFloatActionButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DragFloatActionButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    int pddding = ViewUtils.dipToPixel(PADDING, getContext());
    private void init(){
        initScreenWH();

        imageView = new CircleImageView(this.getContext());
        LayoutParams iLp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        iLp.gravity = Gravity.CENTER;
        iLp.setMargins(pddding, pddding, pddding, pddding);
        imageView.setLayoutParams(iLp);
        imageView.setImageResource(R.drawable.drag);

        coverImageView = new ImageView(this.getContext());
        LayoutParams coverLp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        coverImageView.setLayoutParams(coverLp);
        coverImageView.setBackgroundResource(R.drawable.drag_center);

        textView = new MarqueeTextView(this.getContext());
        LayoutParams tvLp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        textView.setLayoutParams(tvLp);
        textView.setSingleLine();
        textView.setFocusable(true);
        textView.setText("");
        textView.setTextSize(ViewUtils.dipToPixel(ScreenUtils.getScreenType(getContext()) == ScreenUtils.ScreenType.SUPER_LARGE ? 2.31f : 3.8f,
                getContext()));
        textView.setTextColor(getResources().getColor(R.color.whrite_half));
        textView.setShadowLayer(2, 2, 2, getResources().getColor(R.color.black));

        this.addView(imageView);
        this.addView(coverImageView);
        this.addView(textView);
    }

    public void initScreenWH() {
        screenWidth= ScreenUtils.getScreenWidth(getContext());
        screenWidthHalf = screenWidth/2;
        screenHeight=ScreenUtils.getScreenHeight(getContext());
        statusHeight=ScreenUtils.getStatusHeight(getContext());
    }

    private int lastX;
    private int lastY;

    private boolean isDrag;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    public void setOnTouchEventListener(@Nullable TouchEventListener l) {
        this.touchEventListener = l;
    }

    private int downX;
    private int downY;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int rawX = (int) event.getRawX();
        int rawY = (int) event.getRawY();
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                isDrag = false;
                getParent().requestDisallowInterceptTouchEvent(true);
                lastX=rawX;
                lastY=rawY;
                downX = rawX;
                downY = rawY;

                if (this.touchEventListener != null) {
                    this.touchEventListener.onDown(rawX, rawY);
                }
                break;
            case MotionEvent.ACTION_MOVE:
//                isDrag = true;
                //计算手指移动了多少
                int dx=rawX-lastX;
                int dy=rawY-lastY;
                //这里修复一些华为手机无法触发点击事件的问题
                /*int distance= (int) Math.sqrt(dx*dx+dy*dy);
                if(distance <= 5){
                    isDrag = false;
                    break;
                }*/
                float x=getX()+dx;
                float y=getY()+dy;
                //检测是否到达边缘 左上右下
                x=x<0?0:x>screenWidth-getWidth()?screenWidth-getWidth():x;
                y=y<statusHeight?statusHeight:y+getHeight()>screenHeight?screenHeight-getHeight():y;
                setX(x);
                setY(y);
                lastX=rawX;
                lastY=rawY;
                if (this.touchEventListener != null) {
                    this.touchEventListener.onMove(rawX, rawY);
                }
                break;
            case MotionEvent.ACTION_UP:
                int moveX = lastX - downX;
                int moveY = lastY - downY;
                if (Math.abs(moveX) > 10 || Math.abs(moveY) > 10) {
                    isDrag = true;
                } else {
                    isDrag = false;
                }

                if(isDrag){
                    //恢复按压效果
                    setPressed(false);
                    if(rawX>=screenWidthHalf){
                        animate().setInterpolator(new DecelerateInterpolator())
                                .setDuration(500)
                                .xBy(screenWidth-getWidth()-getX())
                                .start();
                    }else {
                        ObjectAnimator oa=ObjectAnimator.ofFloat(this,"x",getX(),0);
                        oa.setInterpolator(new DecelerateInterpolator());
                        oa.setDuration(500);
                        oa.start();
                    }
                }
                if (this.touchEventListener != null) {
                    this.touchEventListener.onUp(rawX, rawY);
                }
                break;
        }
        //如果是拖拽则消耗事件，否则正常传递即可。
        return isDrag || super.onTouchEvent(event);
    }

    public boolean isClick() {
        return !isDrag;
    }

    public int getLastX() {
        return lastX;
    }

    public int getLastY() {
        return lastY;
    }

    public TouchEventListener getTouchEventListener() {
        return touchEventListener;
    }

    public DragFloatActionButton setTouchEventListener(TouchEventListener touchEventListener) {
        this.touchEventListener = touchEventListener;
        return this;
    }

    public void setScaleType(ImageView.ScaleType scaleType) {
        this.imageView.setScaleType(scaleType);
    }

    public void setImageResource(int imageResource) {
        this.imageView.setImageResource(imageResource);
    }

    public void setImageDrawable(Drawable drawable) {
        this.imageView.setImageDrawable(drawable);
    }

    public void postEventionText(String name) {
        textView.setString(name == null ? "" : name);
        textView.startScroll();
    }

    public void postStopEvention() {
        textView.stopScroll();
    }

    public ImageView getImageView() {
        return imageView;
    }

    public interface TouchEventListener {
        void onDown(int x, int y);

        void onMove(int x, int y);

        void onUp(int x, int y);

        void onClick(int x, int y);
    }
}
