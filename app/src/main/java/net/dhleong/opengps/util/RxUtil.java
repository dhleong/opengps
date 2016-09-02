package net.dhleong.opengps.util;

import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import com.jakewharton.rxbinding.widget.RxTextView;

import rx.Observable;
import rx.Observer;
import rx.functions.Action1;
import rx.functions.Func1;
import timber.log.Timber;

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

    /**
     * Convience to create an Observer when all you care
     *  about is the onNext
     */
    public static <T> Observer<T> observer(Action1<T> action) {
        return new Observer<T>() {
            @Override public void onCompleted() { }

            @Override public void onError(Throwable e) {
                Timber.e(e, "Unexpected error in observer for %s", action);
            }

            @Override
            public void onNext(T t) {
                action.call(t);
            }
        };
    }
}
