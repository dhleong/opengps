package net.dhleong.opengps.feat.radios;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.dhleong.opengps.R;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author dhleong
 */
public class RadioView extends LinearLayout {

    @BindView(R.id.active) TextView activeView;
    @BindView(R.id.standby) TextView standbyView;

    private String label = "NAV";
    private Paint labelPaint;

    public RadioView(Context context) {
        this(context, null);
    }

    public RadioView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RadioView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RadioView);
            label = a.getString(R.styleable.RadioView_label);
            a.recycle();
        }

        setGravity(Gravity.CENTER);
        setOrientation(VERTICAL);
        setWillNotDraw(false);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);

        labelPaint = new Paint();
        labelPaint.setColor(activeView.getCurrentTextColor());
        labelPaint.setAntiAlias(true);
        labelPaint.setTextAlign(Paint.Align.LEFT);
        labelPaint.setTextSize(TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 12f, getResources().getDisplayMetrics()));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (label != null) {
            canvas.drawText(label,
                getPaddingLeft(),
                getPaddingTop() + labelPaint.getTextSize(),
                labelPaint);
        }

        canvas.drawText("STBY",
            getPaddingLeft(),
            getPaddingTop() + labelPaint.getTextSize() + standbyView.getTop(),
            labelPaint);
    }

    public void setFrequencies(float active, float standby) {
        activeView.setText(String.format(Locale.US, "%.3f", active));
        standbyView.setText(String.format(Locale.US, "%.3f", standby));
    }
}
