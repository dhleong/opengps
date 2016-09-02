package net.dhleong.opengps.ui;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.view.View;

import net.dhleong.opengps.R;

/**
 * @author dhleong
 */
public class NavigateUtil {
    public static <T, V extends DialogPrompter.PrompterView<T, ?>> void into(
            Context context, Class<V> viewClass, @LayoutRes int layoutResId, T arg) {
        if (!(context instanceof IntoNavigator)) {
            throw new IllegalArgumentException();
        }

        IntoNavigator nav = (IntoNavigator) context;
        V v = nav.inflate(viewClass, layoutResId);

        View prev = nav.navigateInto((View) v);

        v.result(arg);
        ((View) v).setTag(R.id.previous, prev);
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

        view.setTag(R.id.previous, null);
        return true;
    }

    public interface IntoNavigator {
        View navigateInto(View view);

        <T, V extends DialogPrompter.PrompterView<T, ?>> V inflate(Class<V> viewClass, int layoutResId);
    }
}
