package com.jhqc.vr.travel.weight;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.jhqc.AR_3D.R;
import com.jhqc.vr.travel.util.LogUtils;
import com.jhqc.vr.travel.util.ViewUtils;
import com.jhqc.vr.travel.weight.event.MapLongClickListener;
import com.jhqc.vr.travel.weight.event.support.MapMatrix;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * 自定义 地图 控件
 * Created by user on 2016/3/3.
 */
public class MapView extends RelativeLayout implements View.OnClickListener, View.OnLongClickListener {

    /** 屏幕宽高 */
    public float SCREEN_WIDTH, SCREEN_HEIGHT;

    // 显示 地图 底图和线 的 控件
    public BaseImageView baseImageView;

    public ImageView mapBiggerView, mapSmallerView, mapOriginalView;

    /** 记录地图左上角的点View，做标识使用 */
    public View OriginalView;

    private boolean isScaleEnable = false;

    public float MAX_SCALE = 1.5f;

    public float MIN_SCALE = 1;

    private float DEFAULT_SCALE = MIN_SCALE;

    // 首次 放大缩小的 倍数
    public float currentScale = DEFAULT_SCALE;

    // 地图上标记的点
    public HashMap<Object, MapPointView> pointMaps;

    // 长按 标志
    public boolean Touch_longPressTag = false;

    // 点击 标志
    public boolean Touch_clickTag = false;
    /**
     * 手指 第一次 按下的 事件 坐标
     */
    public float touchDownX;

    public float touchDownY;

    /**
     * 第一次 按下时间
     */
    public long downTime;

    // 自定义 地图 长按 监听
    private MapLongClickListener mapOnLongClickListener;

    private MapTouchListener touchListener;

    public MapView(Context context) {
        super(context);
        init();
    }

    public MapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MapView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    // 初始化
    private void init() {
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metric = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metric);
        SCREEN_WIDTH = metric.widthPixels;     // 屏幕宽度（像素）
        SCREEN_HEIGHT = metric.heightPixels;   // 屏幕高度（像素）

        LayoutInflater.from(getContext()).inflate(R.layout.view_mymap, this);
        baseImageView = (BaseImageView) this.findViewById(R.id.imageView);
        mapBiggerView = (ImageView) this.findViewById(R.id.mapBigger);
        mapSmallerView = (ImageView) this.findViewById(R.id.mapSmaller);
        mapOriginalView = (ImageView) this.findViewById(R.id.mapOriginal);

        OriginalView = new View(getContext());
        OriginalView.setBackgroundColor(getResources().getColor(R.color.black));
        pointMaps = new HashMap<>();
        // 设置 触摸 监听
        mapBiggerView.setOnClickListener(this);
        mapSmallerView.setOnClickListener(this);
        mapOriginalView.setOnClickListener(this);
    }

    /**
     * 设置地图底图 并 显示
     *
     * @param bitmap
     * @param bitmapWith
     * @param bitmapHeight
     */
    Bitmap bitmap;
    public void setMap(Bitmap bitmap, int bitmapWith, int bitmapHeight) {
        this.bitmap = bitmap;
        baseImageView.setImageBitmap(bitmap);
        if (isScaleEnable()) {
            float scale = DEFAULT_SCALE;

            if (bitmapWith < SCREEN_WIDTH) {
                scale = SCREEN_WIDTH / (float) bitmapWith;
            }
            if (bitmapHeight < SCREEN_HEIGHT && scale < ((float) SCREEN_HEIGHT / (float) bitmapHeight)) {
                scale = SCREEN_HEIGHT / (float) bitmapHeight;
            }

            if (bitmapWith > SCREEN_WIDTH * 1.0) {
                scale = (float) ((SCREEN_WIDTH * 1.0) / bitmapWith);
            }

            scale(scale);
            DEFAULT_SCALE = scale;
            MIN_SCALE = scale;
        }
        touchListener = new MapTouchListener(this);
        this.setOnTouchListener(touchListener);
    }

    /**
     * 根据 传入 的 缩放 比例 显示 地图
     *
     * @param scale 地图缩放 比例
     */
    private void scale(float scale) {
        this.currentScale = scale;
        MapMatrix matrix = new MapMatrix();
        matrix.set(baseImageView.getImageMatrix());
        // 放大缩小 适应屏幕宽度
        matrix.postScale(this.currentScale, this.currentScale, 0, 0);
        OriginalView.setX((OriginalView.getX() * scale));
        OriginalView.setY((OriginalView.getY() * scale));
        baseImageView.setImageMatrix(matrix);
        baseImageView.invalidate();
        /*float[] values = new float[9];
        matrix.getValues(values);
        invalidateMap(values);*/
    }

    /**
     * 根据 当前 缩放 比例
     * 移动 点 的 位置
     * 用于 放大缩小 按钮
     */
    public void moveMapPoints() {
        Iterator iterator = pointMaps.keySet().iterator();
        while (iterator.hasNext()) {
            MapPointView point = pointMaps.get(iterator.next());
            // 设置 点 的 初始位置

            point.setFirstXShow((float) (point.getFirstX() * currentScale));
            point.setFirstYShow((float) (point.getFirstY() * currentScale));
        }
    }

    /**
     * 根据 当前 缩放 比例
     * 移动 线 的 位置
     * 用于 放大缩小 按钮
     */
    public void moveMapLines() {
        for (int i = 0; i < baseImageView.getLineSize(); i++) {
            BaseImageView.MapLineCoord line = baseImageView.getLine(i);
            line.setViewX(line.getFirstX() * currentScale);
            line.setViewY(line.getFirstY() * currentScale);
        }
        baseImageView.invalidate();
    }

    /**
     * 添加一个点 并显示
     */
    public void addMapPoint(MapPointView mapPointView) {

        pointMaps.put(mapPointView.getTag(), mapPointView);
        mapPointView.setFirstXShow((float) (mapPointView.getFirstX() * currentScale + OriginalView.getX()));
        mapPointView.setFirstYShow((float) (mapPointView.getFirstY() * currentScale + OriginalView.getY()));
        this.addView(mapPointView);
    }

    public void moveMapLocationXY(MapPointView mapPointView, float x, float y) {
        mapPointView.setFirstX(mapPointView.getFirstX() + x);
        mapPointView.setFirstY(mapPointView.getFirstY() + y);
        mapPointView.setFirstXShow((float) (mapPointView.getFirstX() * currentScale + OriginalView.getX()));
        mapPointView.setFirstYShow((float) (mapPointView.getFirstY() * currentScale + OriginalView.getY()));
        mapPointView.invalidate();
    }

    //POINT_TAG_LOCTION
    public void indexPiontView(final MapPointView mapPointView) {
        if (mapPointView == null) {
            return;
        }
        final float pInX = (float) (mapPointView.getFirstX() * currentScale/* + OriginalView.getX()*/);
        final float pInY = (float) (mapPointView.getFirstY() * currentScale/* + OriginalView.getY()*/);
        indexPointToCenter(pInX, pInY);
    }

    private void indexPointToCenter(float pInX, float pInY) {
        if (System.currentTimeMillis() - baseImageView.getLastCurrentMills() < 15 * 1000) {
            LogUtils.logOther("时间不到，暂不定位到定位位置");
            return;
        }

        currentMatrix.set(this.baseImageView.getImageMatrix());
        PointF centerPoint = baseImageView.getBitmapInCenterXY();
        float[] values = new float[9];
        currentMatrix.getValues(values);

        float realW = baseImageView.getRealWidth(values);
        float realH = baseImageView.getRealHeight(values);
        float width = baseImageView.getImageWidth();
        float height = baseImageView.getImageHeight();

        PointF viewPoint = new PointF(width, height);
        PointF realPoint = new PointF(realW, realH);

        LogUtils.logOther("-----------------------");
        LogUtils.logOther("中心点 ：" + centerPoint.x+" " + centerPoint.y);
        LogUtils.logOther("要移动到位置 ：" + pInX+" " + pInY);

        if ((int)pInX == (int) centerPoint.x && (int)pInY == (int) centerPoint.y) {
            LogUtils.logOther("移动距离太近，不移动");
            return;
        }

        if (pInX < 0 || pInY < 0) {
            LogUtils.logOther("坐标属左或上角边界外，不移动");
            return;
        }

        if (pInX > realPoint.x || pInY > realPoint.y) {
            LogUtils.logOther("坐标属右或下角边界外，不移动");
            return;
        }

        /*if (pInX < viewPoint.x/2 || pInY < viewPoint.y / 2) {
            return;
        }

        if (pInX + viewPoint.x/2 > realPoint.x || pInY + viewPoint.y/2 > realPoint.y) {
            return;
        }*/

        //判断是否越出右边界
        if (pInX + (viewPoint.x / 2) > realPoint.x) {
            pInX = realPoint.x - (viewPoint.x / 2);
            LogUtils.logOther("<判断是否越出右边界>");
        } else if (pInX < (viewPoint.x / 2)) {
            //判断是否超出左边界
            pInX = (viewPoint.x / 2);
            LogUtils.logOther("<判断是否越出左边界>");
        }

        //判断是否越出下边界
        if (pInY + (viewPoint.y / 2) >= realPoint.y) {
            pInY = realPoint.y - (viewPoint.y / 2);
            LogUtils.logOther("<判断是否越出下边界>");
        } else if (pInY <= (viewPoint.y / 2)) {
            //判断是否越出上边界
            pInY = (viewPoint.y / 2);
            LogUtils.logOther("<判断是否越出上边界>");
        }

        float x = -(pInX - centerPoint.x/* - values[2]*/);
        float y = -(pInY - centerPoint.y/* - values[5]*/);

        LogUtils.logOther("已移动点 ：" + values[2]+" " + values[5]);
        LogUtils.logOther("只能移动到位置 ：" + pInX+" " + pInY);
        LogUtils.logOther("移动 ：" + x+" " + y);
        LogUtils.logOther("-----------------------");
        //TODO 手动操作后白屏

        matrix.set(currentMatrix);
        matrix.postTranslate(x, y);
        // 放大缩小 适应屏幕宽度
        baseImageView.setImageMatrix(matrix);

        invalidateMap(baseImageView.getMatrixValues());
    }

    public void invalidateMap(float[] matrixValues) {
        // 地图原点移动
        this.OriginalView.setX((float) (0 * matrixValues[0] + matrixValues[2]));
        this.OriginalView.setY((float) (0 * matrixValues[4] + matrixValues[5]));
        this.currentScale = matrixValues[0];

        // 移动 点
        Iterator iterator = this.pointMaps.keySet().iterator();
        while (iterator.hasNext()) {
            Object key = iterator.next();
            MapPointView point = this.pointMaps.get(key);
            double scaleX = point.getFirstX() * matrixValues[0];
            double scaleY = point.getFirstY() * matrixValues[4];

            point.setFirstXShow((float) (scaleX + matrixValues[2]));
            point.setFirstYShow((float) (scaleY + matrixValues[5]));
        }

        // 移动 线
        for (int i = 0; i < this.baseImageView.getLineSize(); i++) {
            float v1 = this.baseImageView.getLine(i).getFirstX() * matrixValues[0] + matrixValues[2];
            float v2 = this.baseImageView.getLine(i).getFirstY() * matrixValues[4] + matrixValues[5];
            this.baseImageView.getLine(i).setViewX(v1);
            this.baseImageView.getLine(i).setViewY(v2);
        }
        this.baseImageView.invalidate();


        /**
         *如果 外层为ScrollView 此句代码是解决
         * 地图的移动 和 ScrollView 的滚动冲突的
         * 当触摸事件在地图范围内时，ScrollView 滚动事件无法响应
         * 当触摸事件在 地图范围外时，ScrollView可以滚动
         */
        this.getParent().requestDisallowInterceptTouchEvent(true);
    }

    /**
     * 添加 线 的 坐标
     * 至少 两个 ，且 这两个 坐标不重复 ，线才会显示
     *
     * @param x
     * @param y
     */
    public void addMapLine(float x, float y) {
        BaseImageView.MapLineCoord mapLineCoord = baseImageView.new MapLineCoord(x, y,
                x * currentScale + OriginalView.getX(), y * currentScale + OriginalView.getY());
        baseImageView.addLine(mapLineCoord);
    }

    /**
     * 地图 按钮 点击 事件
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
//            case R.id.mapBigger:
//                plus();
//                break;
//            case R.id.mapOriginal:
//                restore();
//                break;
//            case R.id.mapSmaller:
//                decrease();
//                break;
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (Touch_longPressTag) {
            float firstDownX = getTouchDownX();
            float firstDownY = getTouchDownY();
            float fingerX = firstDownX - getX();
            float fingerY = firstDownY - getY();

            float originalPointViewX = OriginalView.getX();
            float originalPointViewY = OriginalView.getY();


            float x = (fingerX - originalPointViewX) / currentScale;
            float y = (fingerY - originalPointViewY) / currentScale;

            // 长按 事件 回调
            this.mapOnLongClickListener.onLongClick(x, y);
            return true;
        }
        return false;
    }

    public void plus() {
        if (!isScaleEnable()) {
            return;
        }
        if (this.currentScale >= MAX_SCALE) {
            return;
        }
        scale(this.currentScale + 0.5f);
        moveMapPoints();
        moveMapLines();
    }

    public void restore() {
        scale(DEFAULT_SCALE);
        moveMapPoints();
        moveMapLines();
    }

    public void decrease() {
        if (!isScaleEnable()) {
            return;
        }
        if (this.currentScale <= MIN_SCALE) {
            return;
        }
        scale(this.currentScale - 0.5f);
        moveMapPoints();
        moveMapLines();
    }

    public void release() {
        clearMapLines();
        clearMapPoints();
        ViewUtils.releaseImageViewResource(baseImageView);
        ViewUtils.releaseBackgroundDrawable(baseImageView);
        if(baseImageView.getDrawable() != null) {
            baseImageView.getDrawable().setCallback(null);
        }
        baseImageView.setImageBitmap(null);
        baseImageView.removeCallbacks(null);


        clearMapPoints();

        this.removeAllViews();
    }

    /**********************************************************************************************/
    /**
     * 加多个 点
     *
     * @param list
     */
    public void setPointMaps(ArrayList<MapPointView> list) {
        clearMapPoints();
        for (MapPointView pointView : list) {
            this.addMapPoint(pointView);
        }
    }

    /**
     * 加 多条 线
     *
     * @param mapLineCoords
     */
    public void setMapLines(ArrayList<BaseImageView.MapLineCoord> mapLineCoords) {
        this.baseImageView.clearLines();
        this.baseImageView.addLines(mapLineCoords);
    }

    /**
     * 清空 点
     */
    public void clearMapPoints() {
        Iterator iterator = pointMaps.keySet().iterator();
        MapPointView pointView;
        while (iterator.hasNext()) {
            pointView = pointMaps.get(iterator.next());
            pointView.release();
            this.removeView(pointView);
        }
        this.pointMaps.clear();
    }

    /**
     * 清空 线
     */
    public void clearMapLines() {
        this.baseImageView.clearLines();
    }


    /**
     * 得到 地图 点 的集合
     *
     * @return
     */
    public HashMap<Object, MapPointView> getPointMaps() {
        return pointMaps;
    }

    /**
     * 得到 地图 线 的 集合
     *
     * @return
     */
    public ArrayList<BaseImageView.MapLineCoord> getMapLines() {
        return baseImageView.getMapLineCoords();
    }

    /**
     * 得到 地图 点 的集合
     *
     * @return
     */
    public MapPointView getPointMapByTag(Object tag) {
        if (pointMaps != null) {
            return pointMaps.get(tag);
        }
        return null;
    }

    /**
     * 设置 地图 自定义 长按 监听
     *
     * @param mapOnLongClickListener 自定义 地图 长按 监听器
     */
    public void setMapOnLongClickListener(MapLongClickListener mapOnLongClickListener) {
        this.mapOnLongClickListener = mapOnLongClickListener;
        this.setOnLongClickListener(this);
    }

    public MapLongClickListener getMapOnLongClickListener() {
        return mapOnLongClickListener;
    }

    public float getTouchDownX() {
        return touchDownX;
    }

    public void setTouchDownX(float touchDownX) {
        this.touchDownX = touchDownX;
    }

    public float getTouchDownY() {
        return touchDownY;
    }

    public void setTouchDownY(float touchDownY) {
        this.touchDownY = touchDownY;
    }

    public boolean isScaleEnable() {
        return isScaleEnable;
    }

    public void setScaleEnable(boolean scaleEnable) {
        isScaleEnable = scaleEnable;
    }

    public MapTouchListener getMapTouchListener() {
        return touchListener;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        LogUtils.logArith("measure:" + widthMeasureSpec+"  " + heightMeasureSpec);
    }

    public float getScale() {
        return /*currentScale*/1.0f;
    }

    /**
     * 用于记录拖拉图片移动的坐标位置
     */
    private MapMatrix matrix;

    /**
     * 用于记录图片要进行拖拉时候的坐标位置
     */
    private Matrix currentMatrix;

    class MapTouchListener implements View.OnTouchListener {

        MapView mMapView;

        /**
         * 记录是拖拉照片模式还是放大缩小照片模式
         */
        private int mode = 0;// 初始状态
        /**
         * 拖拉照片模式
         */
        private static final int MODE_DRAG = 1;
        /**
         * 放大缩小照片模式
         */
        private static final int MODE_ZOOM = 2;

        /**
         * 用于记录开始时候的坐标位置
         */
        private PointF startPoint = new PointF();

        /**
         * 两个手指的开始距离
         */
        private float downDistance;
        /**
         * 两个手指的中间点
         */
        private PointF middlePoint;

        public MapTouchListener(MapView mapView) {
            this.mMapView = mapView;
            Matrix scrMatrix = this.mMapView.baseImageView.getImageMatrix();
            currentMatrix = new Matrix(scrMatrix);
            matrix = new MapMatrix();
            matrix.set(currentMatrix);

            this.mMapView.baseImageView.setImageMatrix(matrix);
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            /** 通过与运算保留最后八位 MotionEvent.ACTION_MASK = 255 */
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                // 手指压下屏幕
                case MotionEvent.ACTION_DOWN:
                    mode = MODE_DRAG;
                    this.mMapView.Touch_longPressTag = true;
                    this.mMapView.downTime = System.currentTimeMillis();
                    if (mMapView.isScaleEnable()) {
                        // 记录ImageView当前的移动位置
                        currentMatrix.set(this.mMapView.baseImageView.getImageMatrix());
                        startPoint.set(event.getX(), event.getY());

                        this.mMapView.setTouchDownX(event.getX());
                        this.mMapView.setTouchDownY(event.getY());
                    }
                    break;
                // 手指在屏幕上移动，改事件会被不断触发
                case MotionEvent.ACTION_MOVE:
                    if (mMapView.isScaleEnable()) {
                        float[] values = new float[9];
                        currentMatrix.getValues(values);

                        PointF pointLT = mMapView.baseImageView.getLeftPointF(values);
                        PointF pointRB = mMapView.baseImageView.getRightBottomPointF(values);
                        // 拖拉图片
                        if (mode == MODE_DRAG) {
                            float dx = event.getX() - startPoint.x; // 得到x轴的移动距离
                            float dy = event.getY() - startPoint.y; // 得到x轴的移动距离

                            float realW = mMapView.baseImageView.getRealWidth(values);
                            float realH = mMapView.baseImageView.getRealHeight(values);
                            float width = mMapView.baseImageView.getImageWidth();
                            float height = mMapView.baseImageView.getImageHeight();

                            LogUtils.logView("-----------------");
                            ////////////////////////////////////start////////////////////////////////////
                            { /** 拦截边界*/
                                LogUtils.logView("手势移动x:" + dx);
                                LogUtils.logView("手势移动y:" + dy);

                                /** 左滑：图片向右移：检查左边界，如果超出边界，则重置 */
                                if (dx > 0) {
                                    if (pointLT.x >= 0) { //如果图片已经超出，还原
                                        dx = -pointLT.x;
                                    } else if ((realW - Math.abs(pointLT.x) < dx)) {
                                        dx = realW - Math.abs(pointLT.x);
                                    } else if ((realW - Math.abs(pointRB.x) < dx)) {
                                        dx = realW - Math.abs(pointRB.x);
                                    }
                                    LogUtils.logView("x轴出左边界了，重置移动位置：" + dx);

                                } else if (dx < 0) { /** 右滑：图片向左移：检查右边界，如果超出边界，则重置 */
                                    if ((pointRB.x - Math.abs(dx)) <= width || pointRB.x <= width) {
                                        dx = -(pointRB.x - width);
                                        LogUtils.logView("x轴出右边界了，重置移动位置：" + dx);
                                    }
                                }

                                if (dy > 0) { /** 上滑：图片下移：检查上边界，如果超出边界，则重置 */
                                    if (pointLT.y >= 0) { //如果图片已经超出，还原
                                        dy = -pointLT.y;
                                    } else if ((realH - Math.abs(pointLT.y) < dy)) {
                                        dy = realH - Math.abs(pointLT.y);
                                    } else if ((realH - Math.abs(pointRB.y) < dy)) {
                                        dy = realH - Math.abs(pointRB.y);
                                    }
                                    LogUtils.logView("y轴出上边界了，重置移动位置：" + dy);
                                } else if (dy < 0) { /** 下滑：图片上移：检查下边界，如果超出边界，则重置 */
                                    if ((pointRB.y - Math.abs(dy)) <= height || pointRB.y <= height) {
                                        dy = -(pointRB.y - height);
                                        LogUtils.logView("y轴出下边界了，重置移动位置：" + dy);
                                    }
                                }

                                LogUtils.logView("pointLT.x:" + pointLT.x);
                                LogUtils.logView("pointLT.y:" + pointLT.y);

                                LogUtils.logView("pointRB.x:" + pointRB.x);
                                LogUtils.logView("pointRB.y:" + pointRB.y);

                                LogUtils.logView("realW:" + realW);
                                LogUtils.logView("realH:" + realH);

                                LogUtils.logView("width:" + width);
                                LogUtils.logView("height:" + height);
                                LogUtils.logView("移动 :" + dx + "  " + dy);
                                LogUtils.logView("-----------------");
                            }
                            /////////////////////////////////////end////////////////////////////////////

                            // 在没有移动之前的位置上进行移动
                            matrix.set(currentMatrix);
                            matrix.postTranslate(dx, dy);
                            matrix.setRealPoint(realW, realH);
                            matrix.setViewPoint(width, height);
                            matrix.setRBPoint(pointRB.x, pointRB.y);
                            // 如果 手指移动 距离不超过 5 个像素点 的 视为 没有移动
                            float offset = (float) Math.sqrt(dx * dx + dy * dy);
                            this.mMapView.Touch_longPressTag = offset < 5 ? true : false;
                        }
                        // 放大缩小图片
                        else if (mode == MODE_ZOOM) {
                            float moveDistance = distance(event);// 移动后距离
                            if (moveDistance > 10f) { // 两个手指并拢在一起的时候像素大于10
                                float scale = moveDistance / downDistance;// 得到缩放倍数

                                float dx = event.getX() - startPoint.x; // 得到x轴的移动距离
                                float dy = event.getY() - startPoint.y; // 得到x轴的移动距离

                                float realW = mMapView.baseImageView.getRealWidth(values);
                                float realH = mMapView.baseImageView.getRealHeight(values);
                                float width = mMapView.baseImageView.getImageWidth();
                                float height = mMapView.baseImageView.getImageHeight();

                                {/////////////////////////////////start/////////////////////////////////
                                    {/** 1.检查放大倍数 */
                                        /** 如果缩到小于屏幕时，则不让缩小 */
                                        if (scale >= mMapView.MAX_SCALE ||
                                                (realW <= width && scale < 1.0f) || (realH <= height && scale < 1.0f)
                                            /*|| pointRB.x < width || pointRB.y < height*/) {
                                            break;
                                        }

                                        float[] vl = baseImageView.getMatrixValues();
                                        if (scale > 1 && (vl[0] >= MAX_SCALE || vl[4] >= MAX_SCALE)) {
                                            scale = 1;
                                            break;
                                        }
                                        LogUtils.logArith("scle=" + scale);
                                        LogUtils.logArith("图sclex=" + vl[0] +" scley=" + vl[4]);
                                        LogUtils.logArith("cursclex=" + values[0] +" scley=" + values[4]);


                                        if (realW * scale <= width) {
                                            scale = width / realW;
                                        }
                                        if (realH * scale <= height) {
                                            scale = Math.min(scale, height / realH);
                                        }
                                    }

                                    LogUtils.logView("sc =" + scale);
                                    LogUtils.logView("down =" + downDistance);
                                    LogUtils.logView("moveDistance =" + moveDistance);
                                /*PointF centerPointF = mMapView.baseImageView.getCenter(scale, values);
                                middlePoint.x = centerPointF.x;
                                middlePoint.y = centerPointF.y;*/
                                }
                                //////////////////////////////////end///////////////////////////////////
                                matrix.set(currentMatrix);
                                matrix.postScale(scale, scale, middlePoint.x, middlePoint.y);

                                {/////////////////////////////////start/////////////////////////////////
                                    //复位图片
                                    values = new float[9];
                                    matrix.getValues(values);
                                    PointF p1 = getLeftPointF(values);
                                    PointF p2 = getRightPointF(values);
                                    //左边界复位
                                    if (p1.x > 0) {
                                        matrix.postTranslate(-p1.x, 0);
                                    }
                                    //右边界复位
                                    if (p2.x < mMapView.baseImageView.getWidth()) {
                                        matrix.postTranslate(mMapView.baseImageView.getWidth() - p2.x, 0);
                                    }
                                    //上边界复位
                                    if (p1.y > 0) {
                                        matrix.postTranslate(0, -p1.y);
                                    }
                                    //下边界复位
                                    if (p2.y < mMapView.baseImageView.getHeight()) {
                                        matrix.postTranslate(0, mMapView.baseImageView.getHeight() - p2.y);
                                    }
                                }//////////////////////////////////end///////////////////////////////////

//                            matrix.setRealPoint(realW, realH);
//                            matrix.setViewPoint(width, height);
//                            matrix.setRBPoint(pointRB.x, pointRB.y);
                                this.mMapView.baseImageView.setLastCurrentMills(System.currentTimeMillis());
                            }
                        }
                    }
                    break;
                // 手指离开屏幕
                case MotionEvent.ACTION_UP:
                    //如果 按下 抬起 时间 大于 2s 则是 长按 事件
                    this.mMapView.Touch_longPressTag = System.currentTimeMillis() - this.mMapView.downTime > 2000 ? true : false;
                    this.mMapView.baseImageView.setLastCurrentMills(System.currentTimeMillis());
                    // 当触点离开屏幕，但是屏幕上还有触点(手指)
                case MotionEvent.ACTION_POINTER_UP:
                    mode = 0;
                    break;
                // 当屏幕上已经有触点(手指)，再有一个触点压下屏幕
                case MotionEvent.ACTION_POINTER_DOWN:
                    this.mMapView.Touch_longPressTag = false;

                    mode = MODE_ZOOM;
                    /** 计算两个手指间的距离 */
                    downDistance = distance(event);
                    /** 计算两个手指间的中间点 */
                    if (downDistance > 10f) { // 两个手指并拢在一起的时候像素大于10
                        middlePoint = middle(event);
                        //记录当前ImageView的缩放倍数
                        currentMatrix.set(this.mMapView.baseImageView.getImageMatrix());
                    }
                    break;
            }

            /**
             * 如果此次触摸事件  是  移动，放大事件
             * 则改变地图 和 坐标点的位置
             */
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_MOVE:
                    // 移动 地图
                    baseImageView.setImageMatrix(matrix);

                    float[] matrixValues = new float[9];
                    matrix.getValues(matrixValues);

                    float tmpX = OriginalView.getX(), tmpY = OriginalView.getY();

                    // 地图原点移动
                    OriginalView.setX((float) (0 * matrixValues[0] + matrixValues[2]));
                    OriginalView.setY((float) (0 * matrixValues[4] + matrixValues[5]));

                    currentScale = matrixValues[0];
                    tmpX = this.mMapView.OriginalView.getX()/* - tmpX*/;
                    tmpY = this.mMapView.OriginalView.getY()/* - tmpY*/;

                    LogUtils.logArith("-------------------");
                    LogUtils.logArith("tmpX=" + tmpX +" tmpY=" + tmpY);
                    LogUtils.logArith("scaleX=" + matrixValues[0] +" scaleY=" + matrixValues[4]);
                    LogUtils.logArith("transx=" + matrixValues[2] +" transy=" + matrixValues[5]);

                    // 移动 点
                    Iterator iterator = this.mMapView.pointMaps.keySet().iterator();
                    while (iterator.hasNext()) {
                        Object key = iterator.next();
                        MapPointView point = this.mMapView.pointMaps.get(key);
                        LogUtils.logArith("Firstx=" + point.getFirstX() +" y=" + point.getFirstY());
                        LogUtils.logArith("Lastx=" + point.getX() +" y=" + point.getY());
                        double scaleX = point.getFirstX() * (matrixValues[0]/* - DEFAULT_SCALE + 1*/);
                        double scaleY = point.getFirstY() * (matrixValues[4]/* - DEFAULT_SCALE + 1*/);

                        point.setFirstXShow((float) (scaleX + tmpX));
                        point.setFirstYShow((float) (scaleY + tmpY));
                        LogUtils.logArith("最后x=" + (scaleX + tmpX) +" y=" + (scaleY + tmpY));
                        LogUtils.logArith("应该是x=" + (point.getFirstX() * (matrixValues[0] - DEFAULT_SCALE + 1) + tmpX) +" y=" + (point.getFirstY() * (matrixValues[4] - DEFAULT_SCALE + 1) + tmpY));
                        //point.getFirstX() * currentScale + OriginalView.getX()
                    }
                    LogUtils.logArith("-------------------");
//                    this.mMapView.baseImageView.invalidate();

                    /**
                     *如果 外层为ScrollView 此句代码是解决
                     * 地图的移动 和 ScrollView 的滚动冲突的
                     * 当触摸事件在地图范围内时，ScrollView 滚动事件无法响应
                     * 当触摸事件在 地图范围外时，ScrollView可以滚动
                     */
//                    this.mMapView.getParent().requestDisallowInterceptTouchEvent(true);
                    /*float[] mValues = new float[9];
                    matrix.getValues(mValues);
                    this.mMapView.invalidateMap(mValues);*/
                    break;
            }

            // 如果 设置 了 长按 监听 则 传递 事件
            // 否则 自己 消费 该 事件
            if (this.mMapView.getMapOnLongClickListener() != null) {
                return false;
            }
            return true;
        }

        //获取图片的上坐标
        private PointF getLeftPointF(float[] values) {
            float leftX = values[2];
            float leftY = values[5];
            return new PointF(leftX, leftY);
        }

        //获取图片的下坐标
        private PointF getRightPointF(float[] values) {
            Rect rectTemp = mMapView.baseImageView.getDrawable().getBounds();
            float leftX = values[2] + rectTemp.width() * values[0];
            float leftY = values[5] + rectTemp.height() * values[4];
            return new PointF(leftX, leftY);
        }

        /**
         * 和当前矩阵对比，检验dx，使图像移动后不会超出ImageView边界
         *
         * @param values
         * @param dx
         * @return
         */
        private float checkDxBound(float[] values, float dx) {
            float width = mMapView.baseImageView.getRealWidth(values);
            float mImageWidth = mMapView.baseImageView.getImageWidth();


            if (mImageWidth * values[Matrix.MSCALE_X] < width) {
                return 0;
            }
            if (values[Matrix.MTRANS_X] + dx > 0) {
                dx = -values[Matrix.MTRANS_X];
            } else if (values[Matrix.MTRANS_X] + dx < -(mImageWidth * values[Matrix.MSCALE_X] - width)) {
                dx = -(mImageWidth * values[Matrix.MSCALE_X] - width) - values[Matrix.MTRANS_X];
            }
            return dx;
        }

/*    private boolean checkRest(Matrix mMatrix) {
        float[] values=new float[9];
        getImageMatrix().getValues(values);
        //获取当前X轴缩放级别
        float scale=values[Matrix.MSCALE_X];
        //获取模板的X轴缩放级别，两者做比较
        mMatrix.getValues(values);
        return scale<values[Matrix.MSCALE_X];
    }*/

        /**
         * 和当前矩阵对比，检验dy，使图像移动后不会超出ImageView边界
         *
         * @param values
         * @param dy
         * @return
         */
        private float checkDyBound(float[] values, float dy) {
            float height = mMapView.baseImageView.getRealHeight(values);
            float mImageHeight = mMapView.baseImageView.getImageHeight();

            if (mImageHeight * values[Matrix.MSCALE_Y] < height) {
                return 0;
            }
            if (values[Matrix.MTRANS_Y] + dy > 0) {
                dy = -values[Matrix.MTRANS_Y];
            } else if (values[Matrix.MTRANS_Y] + dy < -(mImageHeight * values[Matrix.MSCALE_Y] - height)) {
                dy = -(mImageHeight * values[Matrix.MSCALE_Y] - height) - values[Matrix.MTRANS_Y];
            }
            return dy;
        }

        /**
         * 计算两个手指间的距离
         */
        private float distance(MotionEvent event) {
            float dx = event.getX(1) - event.getX(0);
            float dy = event.getY(1) - event.getY(0);
            /** 使用勾股定理返回两点之间的距离 */
            return (float) Math.sqrt(dx * dx + dy * dy);
        }

        /**
         * 计算两个手指间的中间点
         */
        private PointF middle(MotionEvent event) {
            float midX = (event.getX(1) + event.getX(0)) / 2;
            float midY = (event.getY(1) + event.getY(0)) / 2;
            return new PointF(midX, midY);
        }
    }
}
