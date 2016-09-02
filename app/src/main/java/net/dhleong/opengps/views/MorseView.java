package net.dhleong.opengps.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import net.dhleong.opengps.R;

import butterknife.BindDimen;
import butterknife.ButterKnife;

/**
 * @author dhleong
 */
public class MorseView extends View {
    static final String[] TABLE = {
        ".-",   /* A */ "-...", /* B */ "-.-.", /* C */
        "-..",  /* D */ ".",    /* E */ "..-.", /* F */
        "--.",  /* G */ "....", /* H */ "..",   /* I */
        ".---", /* J */ "-.-",  /* K */ ".-..", /* L */
        "--",   /* M */ "-.",   /* N */ "---",  /* O */
        ".--.", /* P */ "--.-", /* Q */ ".-.",  /* R */
        "...",  /* S */ "-",    /* T */ "..-",  /* U */
        "...-", /* V */ ".--",  /* W */ "-..-", /* X */
        "-.--", /* Y */ "--.."  /* Z */
    };

    @BindDimen(R.dimen.morse_dot_radius) float dotRadius;
    @BindDimen(R.dimen.morse_dash_length) float dashLength;
    float dotDiameter;

    private @Nullable String text;
    private Paint paint;

    public MorseView(Context context) {
        this(context, null);
    }

    public MorseView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MorseView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        ButterKnife.bind(this);
        dotDiameter = dotRadius * 2;

        paint = new Paint();
        paint.setColor(0xff000000);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(dotRadius * .9f);

        setWillNotDraw(false);
        if (isInEditMode()) {
            setText("LGA");
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        final String text = this.text;
        if (text == null) return;

        canvas.save();

        canvas.translate(getPaddingLeft(), dotRadius);

        for (int i=0, len=text.length(); i < len; i++) {
            final String code = codeFor(text.charAt(i));
            if (code == null) continue;

            canvas.save();
            for (int j=0, codeLen=code.length(); j < codeLen; j++) {
                if (code.charAt(j) == '.') {
                    canvas.drawCircle(dotRadius, 0, dotRadius, paint);
                    canvas.translate(dotDiameter * 2, 0);
                } else {
                    canvas.drawLine(0, 0, dashLength, 0, paint);
                    canvas.translate(dotDiameter + dashLength, 0);
                }
            }
            canvas.restore();

            canvas.translate(0, dotDiameter * 2);
        }

        canvas.restore();
    }

    public void setText(@Nullable String text) {
        this.text = text;
        requestLayout();
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (text == null) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }

        int rows = 0;
        for (int i=0, len=text.length(); i < len; i++) {
            String code = codeFor(text.charAt(i));
            if (code != null) {
                rows++;
            }
        }

        if (rows == 0) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }

        final int width = (int) ((dashLength + dotDiameter) * 3);
        final int height = (int) (rows * dotDiameter + (rows - 1) * dotDiameter);

        setMeasuredDimension(
            getPaddingLeft() + width + getPaddingRight(),
            getPaddingTop() + height + getPaddingBottom());
    }

    static @Nullable String codeFor(char character) {
        final int offset = Character.toUpperCase(character) - 'A';
        if (offset < 0 || offset >= TABLE.length) return null;
        return TABLE[offset];
    }
}
