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

import com.jakewharton.rxbinding.view.RxView;

import net.dhleong.opengps.R;
import net.dhleong.opengps.ui.UiUtil;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;

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
        labelPaint.setTextAlign(Paint.Align.CENTER);
        labelPaint.setTextSize(UiUtil.applyDimen(this, TypedValue.COMPLEX_UNIT_SP, 14));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawText(label,
                getWidth() / 2,
                getPaddingTop() + labelPaint.getTextSize(),
                labelPaint);

        canvas.drawText("STBY",
            getWidth() / 2,
            standbyView.getTop() + labelPaint.getTextSize(),
            labelPaint);
    }

    public void setFrequencies(float active, float standby) {
        activeView.setText(String.format(Locale.US, "%.3f", active));
        standbyView.setText(String.format(Locale.US, "%.3f", standby));
    }

    public Observable<Void> swaps() {
        return RxView.clicks(activeView);
    }

    public Observable<Void> changeRequests() {
        return RxView.clicks(standbyView);
    }
}
