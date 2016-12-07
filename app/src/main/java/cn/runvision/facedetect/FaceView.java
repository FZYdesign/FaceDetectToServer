package cn.runvision.facedetect;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.util.AttributeSet;
/**
 * Created by Administrator on 2016/11/1.
 */

public class FaceView extends android.widget.ImageView {
    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private RectF mFace = null;
    private Point[] mPoints = null;
    private float _width = 480.0f;
    private float _height = 640.0f;

    public FaceView(Context c) {
        super(c);
        init();
    }

    public FaceView(Context c, AttributeSet attrs) {
        super(c, attrs);
        init();
    }

    private void init() {
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setColor(Color.RED);
        mPaint.setStrokeWidth(3);
    }

    // set up detected face rectangle for display
    public void setDisplayRect(float left, float top, float right, float bottom, int width, int height) {
        mFace = new RectF(left, top, right, bottom);
        _width = width;
        _height = height;
        System.out.println("_width:" + _width + " _height:" + _height);
        float ratio = Math.min(getWidth() / _width, getHeight() / _height);
        System.out.println("getWidth():" + getWidth() + " getHeight():" + getHeight());
        float xOffset = (getWidth() - ratio * _width) / 2.0f;
        float yOffset = (getHeight() - ratio * _height) / 2.0f;
        System.out.println("ratio:" + ratio + " xOffset:" + xOffset + " yOffset:" + yOffset);
        mFace.left *= ratio;
        mFace.top *= ratio;
        mFace.right *= ratio;
        mFace.bottom *= ratio;
        mFace.offset(xOffset, yOffset);
        System.out.println(mFace.toString());
        this.invalidate();
    }

    public void setDisplayPoints(Point[] points) {
        System.out.println("_width:" + _width + " _height:" + _height);
        float ratio = Math.min(getWidth() / _width, getHeight() / _height);
        System.out.println("getWidth():" + getWidth() + " getHeight():" + getHeight());
        float xOffset = (getWidth() - ratio * _width) / 2.0f;
        float yOffset = (getHeight() - ratio * _height) / 2.0f;
        System.out.println("ratio:" + ratio + " xOffset:" + xOffset + " yOffset:" + yOffset);
        mPoints = points;
        for(Point p: mPoints) {
            p.x = (int)(p.x * ratio + xOffset);
            p.y = (int)(p.y * ratio + yOffset);
        }
        this.invalidate();
    }

    public void initDisplay() {
        mFace = new RectF(0, 0, 0, 0);
        this.invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mFace != null) {
            System.out.println(mFace.toString());
            canvas.drawRect(mFace, mPaint);
        }
        if (mPoints != null) {
            for (Point p: mPoints) {
                canvas.drawPoint( p.x, p.y, mPaint);
            }
        }
    }

}
