package net.dhleong.opengps.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.HapticFeedbackConstants;
import android.view.View;

import net.dhleong.opengps.R;
import net.dhleong.opengps.util.RadioType;

/**
 * @author dhleong
 */
public class IncrementRadioFreqView extends View {

    IncrementRadioFreqState state;

    Paint textPaint = new Paint();

    public IncrementRadioFreqView(Context context) {
        this(context, null);
    }

    public IncrementRadioFreqView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IncrementRadioFreqView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        textPaint.setTypeface(Typeface.MONOSPACE);

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.IncrementRadioFreqView);
            textPaint.setTextSize(
                a.getDimensionPixelSize(R.styleable.IncrementRadioFreqView_android_textSize,
                (int) textPaint.getTextSize())
            );
            a.recycle();
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        setWillNotDraw(false);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        final IncrementRadioFreqState state = this.state;
        if (state == null) return;

        canvas.save();
        canvas.translate(getPaddingLeft(), getPaddingTop());

        // TODO colors, typing indicator, etc.
        canvas.drawText(state.chars, 0, state.chars.length, 0, textPaint.getTextSize(), textPaint);

        canvas.restore();
    }

    public void setMode(RadioType mode) {
        if (mode == RadioType.COM) {
            state = new ComRadioFreqState();
        } else {
            state = new NavRadioFreqState();
        }
    }

    public void set(float currentFrequency) {
        if (state == null) throw new IllegalStateException("You must call setMode() first");

        state.set(currentFrequency);
        invalidate();
    }

    public void feed(int decimal) {
         if (state == null) throw new IllegalStateException("You must call setMode() first");

        if (state.feed(decimal)) {
            invalidate();
            performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
        }
    }

    static abstract class IncrementRadioFreqState {
        private static final int DECIMAL_INDEX = 3;

        private final float minValue, maxValue;
        private final int[] legalLast;

        char[] chars = {'1', '1', '8', '.', '7', '0'};
        private int nextIndex = 0;

        protected IncrementRadioFreqState(float minValue, float maxValue, int... legalLast) {
            this.minValue = minValue;
            this.maxValue = maxValue;
            this.legalLast = legalLast;
        }

        @SuppressWarnings("PointlessArithmeticExpression")
        public void set(float currentFrequency) {
            final int value = (int) (currentFrequency * 100);

            chars[5] = (char) ('0' + (value /     1) % 10);
            chars[4] = (char) ('0' + (value /    10) % 10);
            chars[2] = (char) ('0' + (value /   100) % 10);
            chars[1] = (char) ('0' + (value /  1000) % 10);
            chars[0] = (char) ('0' + (value / 10000) % 10);

            nextIndex = 0; // reset
        }

        boolean isTyping() {
            return nextIndex > 0;
        }

        float asFloat() {
            double whole = (chars[0] - '0') * 100
                         + (chars[1] - '0') * 10
                         + (chars[2] - '0');
            double decimal = (chars[4] - '0') * 100
                           + (chars[5] - '0') * 10;
            if (chars[5] == '7' || chars[5] == '2') {
                decimal += 5;
            }

            return (float) (whole + (decimal / 1000.));
        }

        boolean feed(int decimal) {
            if (decimal < 0 || decimal > 9) throw new IllegalArgumentException("Expected [0-9] but got " + decimal);

            if (nextIndex >= chars.length) return false;

            final float oldValue = asFloat();
            final int index = nextIndex;
            if (index == 0) {
                // initial press; reset everything to min value tentatively
                set(minValue);
            } else if (index == chars.length - 1) {
                // the last one is special; nav mode only has 0 and 5
                //  as legal values, because it increments by .5, while
                //  com mode allows 0, 2, 5, 7 because it increments by .25
                boolean lastIsLegal = false;
                //noinspection ForLoopReplaceableByForEach
                for (int i=0, len = legalLast.length; i < len; i++) {
                    if (legalLast[i] == decimal) {
                        lastIsLegal = true;
                        break;
                    }
                }

                if (!lastIsLegal) {
                    // easy
                    return false;
                }
            }

            // basically, insert it and check if it's valid; if not, undo the change
            chars[index] = (char) (decimal + '0');
            final float newValue = asFloat();
            if (newValue < minValue || newValue > maxValue) {
                // nope, not legal; if truncating the rest of the digits
                //  is legal, we can accept it
                for (int i=index + 1, len = chars.length; i < len; i++) {
                    if (i != DECIMAL_INDEX) {
                        chars[i] = '0';
                    }
                }
                float truncatedValue = asFloat();

                if (truncatedValue < minValue || truncatedValue > maxValue) {
                    // no longer valid; just reset
                    set(oldValue);
                    nextIndex = index;
                    return false;
                }
            }

            // looks good
            if (index + 1 == DECIMAL_INDEX) {
                nextIndex = 4;
            } else {
                nextIndex = index + 1;
            }
            return true;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        final IncrementRadioFreqState state = this.state;
        if (state == null) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }

        final int width = (int) Math.ceil(textPaint.measureText(state.chars, 0, state.chars.length));
        final int height = (int) Math.ceil(textPaint.getTextSize());
        setMeasuredDimension(
            getPaddingLeft() + getPaddingRight() + width,
            getPaddingTop() + getPaddingBottom() + height
        );
    }

    static class NavRadioFreqState extends IncrementRadioFreqState {
        NavRadioFreqState() {
            super(108.0f, 117.95f, 0, 5);
        }
    }

    static class ComRadioFreqState extends IncrementRadioFreqState {
        ComRadioFreqState() {
            super(118.0f, 136.975f, 0, 2, 5, 7);
        }
    }
}
