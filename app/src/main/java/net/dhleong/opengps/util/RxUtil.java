package net.dhleong.opengps.util;

import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import com.jakewharton.rxbinding.widget.RxTextView;

import rx.Observable;
import rx.functions.Func1;

/**
 * @author dhleong
 */
public class RxUtil {

    static final Func1<?, Boolean> NOT_NULL = thing -> thing != null;
    static final Func1<?,Void> TO_VOID = any -> null;

    @SuppressWarnings("unchecked")
    public static <T> Func1<T, Boolean> notNull() {
        return (Func1<T, Boolean>) NOT_NULL;
    }

    @SuppressWarnings("unchecked")
    public static <R> Func1<R, Void> toVoid() {
        return (Func1<R, Void>) TO_VOID;
    }

    public static Observable<Void> doneActions(EditText view) {
        return RxTextView.editorActions(view,
            actionId -> actionId == EditorInfo.IME_ACTION_DONE)
                         .map(toVoid());
    }

}
