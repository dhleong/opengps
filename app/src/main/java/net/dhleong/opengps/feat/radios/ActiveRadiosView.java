package net.dhleong.opengps.feat.radios;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;

import net.dhleong.opengps.R;
import net.dhleong.opengps.ui.UiUtil;

/**
 * @author dhleong
 */
public class ActiveRadiosView extends TextView {

    final int bgColor;
    final float labelMargin;

    String label = "MIC";
    Paint labelPaint;

    public ActiveRadiosView(Context context) {
        this(context, null);
    }

    public ActiveRadiosView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ActiveRadiosView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        labelPaint = new Paint();
        labelPaint.setAntiAlias(true);
        labelPaint.setTextAlign(Paint.Align.CENTER);
        labelPaint.setTextSize(UiUtil.applyDimen(this, TypedValue.COMPLEX_UNIT_SP, 14));

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ActiveRadiosView);
            label = a.getString(R.styleable.ActiveRadiosView_label);
            labelPaint.setColor(
                a.getColor(R.styleable.ActiveRadiosView_labelColor, 0xffFFFFFF)
            );
            a.recycle();
        }

        labelMargin = labelPaint.getTextSize() * .3f;

        final ColorDrawable bgDraw = (ColorDrawable) getBackground();
        bgColor = bgDraw.getColor();
        setBackground(null);

        setWillNotDraw(false);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        setPadding(
            getPaddingLeft(),
            (int) (getPaddingTop() + labelPaint.getTextSize() + 2 * labelMargin),
            getPaddingRight(),
            getPaddingBottom());
    }

    @Override
    protected void onDraw(Canvas canvas) {

        float textSize = labelPaint.getTextSize();

        canvas.save();
        canvas.clipRect(0, textSize + labelMargin + labelMargin, getWidth(), getHeight());
        canvas.drawColor(bgColor);
        canvas.restore();

        super.onDraw(canvas);

        canvas.drawText(label, getWidth() / 2, textSize + labelMargin, labelPaint);
    }

    @SuppressLint("SetTextI18n")
    public void setState(boolean oneActive, boolean twoActive) {
        if (oneActive && twoActive) {
            setText("12");
        } else if (oneActive) {
            setText("1 ");
        } else if (twoActive) {
            setText(" 2");
        } else {
            setText(null);
        }
    }
}
