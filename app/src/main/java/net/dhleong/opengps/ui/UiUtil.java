package net.dhleong.opengps.ui;

import android.app.Activity;
import android.content.Context;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.lang.ref.WeakReference;

import rx.Subscription;
import rx.functions.Func1;
import rx.subscriptions.CompositeSubscription;

/**
 * @author dhleong
 */
public class UiUtil {

    public static void requestKeyboard(View view) {
        if (view == null) {
            return;
        }

        WeakReference<View> ref = new WeakReference<>(view);
        view.requestFocus();
        view.postDelayed(() -> {
            View v = ref.get();
            if (v == null) return;

            final Context context = v.getContext();
            final InputMethodManager im  = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            im.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT);
        }, 50);
    }

    /**
     * Convenience
     */
    public static float applyDimen(View view, int unit, float value) {
        return TypedValue.applyDimension(unit, value, view.getResources().getDisplayMetrics());
    }

    public static void hideKeyboard(Activity act) {
        if (act == null) {
            return;
        }

        hideKeyboard(act.findViewById(android.R.id.content));

        View focus;
        if ((focus = act.getCurrentFocus()) != null) {
            hideKeyboard(focus);
        }
    }

    public static void hideKeyboard(View view) {
        if (view == null)
            return;

        final Context context = view.getContext();
        final InputMethodManager im  = (InputMethodManager) context
            .getSystemService(Context.INPUT_METHOD_SERVICE);
        im.hideSoftInputFromWindow(view.getWindowToken(), 0);
        view.clearFocus();
    }

    /**
     * Calls the subscriber factory function whenever the
     *  view gets attached to the window, and unsubscribes
     *  from that subscription whenever it is detached.
     */
    public static <V extends View> void onAttachSubscribe(V view, Func1<V, Subscription> subscriber) {

        final CompositeSubscription subs = new CompositeSubscription();

        if (view.isAttachedToWindow()) {
            subs.add(subscriber.call(view));
        }

        view.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {
                //noinspection unchecked
                subs.add(subscriber.call((V) v));
            }

            @Override
            public void onViewDetachedFromWindow(View v) {
                subs.clear();
            }
        });
    }
}
