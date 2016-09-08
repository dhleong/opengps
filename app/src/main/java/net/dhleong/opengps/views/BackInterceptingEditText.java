package net.dhleong.opengps.views;

import android.content.Context;
import android.support.v7.widget.AppCompatEditText;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.KeyEvent;

import rx.subjects.PublishSubject;

/**
 * @author dhleong
 */
public class BackInterceptingEditText extends AppCompatEditText {

    PublishSubject<Void> backPressedEvents = PublishSubject.create();

    public BackInterceptingEditText(Context context) {
        super(context);
    }

    public BackInterceptingEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BackInterceptingEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        // un-set the "auto-complete" type, so IME will still provide suggestions
        //  (normally, MultiAutoCompleteTextView disables that with this flag)
        setInputType(getInputType() & ~InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE);
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK
            && event.getAction() == KeyEvent.ACTION_UP) {
            backPressedEvents.onNext(null);
            return true;
        }
        return super.onKeyPreIme(keyCode, event);
    }

    public PublishSubject<Void> backPresses() {
        return backPressedEvents;
    }
}

