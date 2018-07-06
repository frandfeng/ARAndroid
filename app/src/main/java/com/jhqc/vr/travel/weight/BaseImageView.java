package com.jhqc.vr.travel.weight;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.jhqc.vr.travel.util.LogUtils;
import com.jhqc.vr.travel.weight.event.support.MapMatrix;

import java.util.ArrayList;

/**
 * 自定义 地图
 * Created by Administrator on 2016/6/30.
 */
public class BaseImageView extends android.support.v7.widget.AppCompatImageView {

    // 线 坐标
    public ArrayList<MapLineCoord> mapLineCoords;

    private float LINE_STOKE = 5.0f;

    MapMatrix mapMatrix;

    private long lastCurrentMills;

    @Override
    public void setImageMatrix(Matrix matrix) {
        super.setImageMatrix(matrix);
        if (matrix instanceof MapMatrix) {
            this.mapMatrix = (MapMatrix) matrix;
        }
    }

    @Override
    public Matrix getImageMatrix() {
        return super.getImageMatrix();
    }

    public MapMatrix getMapMatrix(boolean isRerush) {
        if (isRerush) {
            float[] values = getMatrixValues();
            PointF pointRB = this.getRightBottomPointF(values);
            float realW = this.getRealWidth(values);
            float realH = this.getRealHeight(values);
            float width = this.getImageWidth();
            float height = this.getImageHeight();

            mapMatrix.setRealPoint(realW, realH);
            mapMatrix.setViewPoint(width, height);
            mapMatrix.setRBPoint(pointRB.x, pointRB.y);
        }
        return mapMatrix;
    }

    public BaseImageView(Context context) {
        super(context);
        mapLineCoords = new ArrayList<>();
    }

    public BaseImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mapLineCoords = new ArrayList<>();
    }

    public ArrayList<MapLineCoord> getMapLineCoords() {
        return mapLineCoords;
    }

    public int getLineSize() {
        return mapLineCoords.size();
    }

    public void clearLines() {
        mapLineCoords.clear();
    }

    public void addLines(ArrayList<MapLineCoord> mapLineCoords) {
        this.mapLineCoords.addAll(mapLineCoords);
    }

    public MapLineCoord getLine(int index) {
        return mapLineCoords.get(index);
    }

    public void addLine(MapLineCoord mapLineCoord) {
        mapLineCoords.add(mapLineCoord);
        this.invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        /* TODO
        if (mapLineCoords == null || mapLineCoords.size() <= 0)
            return;
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStrokeWidth(LINE_STOKE);
        canvas.drawCircle(mapLineCoords.get(0).getViewX(), mapLineCoords.get(0).getViewY(), 10, paint);
        canvas.drawCircle(mapLineCoords.get(mapLineCoords.size() - 1).getViewX(),
                mapLineCoords.get(mapLineCoords.size() - 1).getViewY(), 10, paint);*/
        //TODO 画线
        /*for (int i = 1; i < mapLineCoords.size(); i++) {
            canvas.drawLine(mapLineCoords.get(i - 1).getViewX(),
                    mapLineCoords.get(i - 1).getViewY(),
                    mapLineCoords.get(i).getViewX(), mapLineCoords.get(i).getViewY(), paint);
        }*/
    }

    public float[] getMatrixValues() {
        float[] values = new float[9];
        getImageMatrix().getValues(values);

        return values;
    }

    /**
     * 获取缩放的中心点。
     *
     * @return
     */
    public PointF getBitmapInCenterXY() {
        MapMatrix matrix = getMapMatrix(true);
        LogUtils.logView("VIEW" +matrix.getViewPoint().toString());
        LogUtils.logView("RB"+matrix.getRbPoint().toString());
        LogUtils.logView("Real"+matrix.getRealPoint().toString());

        PointF viewPoint = matrix.getViewPoint();
        PointF rbPoint = matrix.getRbPoint();
        PointF realPoint = matrix.getRealPoint();
        PointF ltPoint = new PointF(realPoint.x - rbPoint.x, realPoint.y - rbPoint.y);

        PointF centerPoint = new PointF(ltPoint.x + viewPoint.x / 2, ltPoint.y + viewPoint.y / 2);
        return centerPoint;
    }

    /**
     * 获取图片的实际宽度,缩放后实际大小
     */
    public float getRealWidth(float[] values) {
        Rect rectTemp = this.getDrawable().getBounds();
        return rectTemp.width() * values[0];
    }

    /**
     * 获取图片的实际高度,缩放后实际大小
     */
    public float getRealHeight(float[] values) {
        Rect rectTemp = this.getDrawable().getBounds();
        return rectTemp.height() * values[4];
    }

    //获取图片控件宽度
    public float getImageWidth() {
        return this.getWidth();
    }

    //获取图片控件高度
    public float getImageHeight() {
        return this.getHeight();
    }

    /**
     * 获取图片的左上坐标 : 即移动后左上位置点
     */
    public PointF getLeftPointF(float[] values) {
        float leftX = values[2];
        float leftY = values[5];
        return new PointF(leftX, leftY);
    }

    /**
     * 获取图片的下坐标 : 即移动后右下位置点
     */
    public PointF getRightBottomPointF(float[] values) {
        Rect rectTemp = this.getDrawable().getBounds();
        float leftX = values[2] + rectTemp.width() * values[0];
        float leftY = values[5] + rectTemp.height() * values[4];
        return new PointF(leftX, leftY);
    }

    private Bitmap mBitmap;

    public int getMapWidth() {
        if (mBitmap != null) {
            return mBitmap.getWidth();
        }
        return 0;
    }

    public int getMapHeight() {
        if (mBitmap != null) {
            return mBitmap.getHeight();
        }
        return 0;
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        mBitmap = bm;
    }

    public long getLastCurrentMills() {
        return lastCurrentMills;
    }

    public BaseImageView setLastCurrentMills(long lastCurrentMills) {
        this.lastCurrentMills = lastCurrentMills;
        return this;
    }

    /**
     * 地图 线 拐点 坐标
     */
    public class MapLineCoord {
        private float firstX;
        private float firstY;

        private float viewX;
        private float viewY;

        public MapLineCoord() {
        }

        public MapLineCoord(float firstX, float firstY, float viewX, float viewY) {
            this.firstX = firstX;
            this.firstY = firstY;
            this.viewX = viewX;
            this.viewY = viewY;
        }

        public float getFirstX() {
            return firstX;
        }

        public void setFirstX(float firstX) {
            this.firstX = firstX;
        }

        public float getFirstY() {
            return firstY;
        }

        public void setFirstY(float firstY) {
            this.firstY = firstY;
        }

        public float getViewX() {
            return viewX;
        }

        public void setViewX(float viewX) {
            this.viewX = viewX;
        }

        public float getViewY() {
            return viewY;
        }

        public void setViewY(float viewY) {
            this.viewY = viewY;
        }

        @Override
        public String toString() {
            return "MapLineCoord{" +
                    "firstX=" + firstX +
                    ", firstY=" + firstY +
                    ", viewX=" + viewX +
                    ", viewY=" + viewY +
                    '}';
        }
    }
}
