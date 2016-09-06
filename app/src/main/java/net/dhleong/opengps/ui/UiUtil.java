package net.dhleong.opengps.ui;

import android.content.Context;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.lang.ref.WeakReference;

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
}
