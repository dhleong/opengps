package net.dhleong.opengps.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import net.dhleong.opengps.R;

/**
 * @author dhleong
 */
public class BearingIndicatorView extends View {

    Paint paint = new Paint();

    int size;
    float bearing = -1;

    public BearingIndicatorView(Context context) {
        this(context, null);
    }

    public BearingIndicatorView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BearingIndicatorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setWillNotDraw(false);

        final float density = getResources().getDisplayMetrics().density;
        paint.setColor(0xff000000);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(2 * density);
        paint.setStyle(Paint.Style.STROKE);
        size = (int) (48 * density);

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BearingIndicatorView);
            paint.setColor(
                a.getColor(R.styleable.BearingIndicatorView_indicatorColor, paint.getColor())
            );
            bearing = a.getFloat(R.styleable.BearingIndicatorView_bearing, bearing);
            a.recycle();
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        if (isInEditMode() && bearing < 0) {
            setBearing(263);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float bearing = this.bearing;
        if (bearing >= 0) {
            canvas.save();

            canvas.translate(getPaddingLeft(), 0);

            final float center = size * 0.5f;
            final float radius = size * 0.4f; // not quite half, because diagonals
            final float arrowHead = 2 * paint.getStrokeWidth();
            canvas.translate(center, center);
            canvas.rotate(bearing);
            canvas.drawLine(0, -radius, 0, radius, paint);
            canvas.drawLine(0, -radius, -arrowHead, -radius + arrowHead, paint);
            canvas.drawLine(0, -radius, arrowHead, -radius + arrowHead, paint);
            canvas.restore();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.EXACTLY) {
            size = MeasureSpec.getSize(heightMeasureSpec);
        }

        setMeasuredDimension(
            size + getPaddingLeft() + getPaddingRight(),
            size + getPaddingTop() + getPaddingBottom()
        );
    }

    public void setBearing(float bearing) {
        this.bearing = bearing;
        invalidate();
    }
}
