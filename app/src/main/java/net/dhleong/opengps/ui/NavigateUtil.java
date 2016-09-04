package net.dhleong.opengps.ui;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.view.View;

import net.dhleong.opengps.R;

/**
 * Wacky navigation that avoids gross fragments
 *  while allowing for a backstack. Probably not
 *  a terribly great idea, but probably okay for now...
 *
 * @author dhleong
 */
public class NavigateUtil {

    public static View into(Context context, @LayoutRes int layoutResId) {
        // TODO ContextWrappers?
        if (!(context instanceof IntoNavigator)) {
            throw new IllegalArgumentException("Given Context must implement IntoNavigator");
        }

        IntoNavigator nav = (IntoNavigator) context;
        View v = nav.inflate(layoutResId);

        View prev = nav.navigateInto(v);

        // super awful way to "maintain" backstack
        // (only really works if you lock orientation)
        v.setTag(R.id.previous, prev);

        return v;
    }

    public static <T, V extends DialogPrompter.PrompterView<T, ?>> void into(
            Context context, Class<V> viewClass, @LayoutRes int layoutResId, T arg) {
        View inflated = into(context, layoutResId);

        // pass along some data
        viewClass.cast(inflated).result(arg);
    }

    public static boolean backFrom(View view) {
        Context context = view.getContext();
        if (!(context instanceof IntoNavigator)) {
            throw new IllegalArgumentException();
        }

        View prev = (View) view.getTag(R.id.previous);
        if (prev == null) {
            return false;
        }

        IntoNavigator nav = (IntoNavigator) context;
        nav.navigateInto(prev);

        // at least pretend to not be leaky
        view.setTag(R.id.previous, null);
        return true;
    }

    public interface IntoNavigator {
        View navigateInto(View view);

        /**
         * Inflate the given layout using an appropriate
         *  view parent so layout params are inflated,
         *  but DO NOT attach
         */
        View inflate(@LayoutRes int layoutResId);
    }
}
