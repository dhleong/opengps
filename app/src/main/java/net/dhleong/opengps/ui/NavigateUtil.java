package net.dhleong.opengps.ui;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.view.View;

import net.dhleong.opengps.AeroObject;
import net.dhleong.opengps.Airport;
import net.dhleong.opengps.NavFix;
import net.dhleong.opengps.Navaid;
import net.dhleong.opengps.R;
import net.dhleong.opengps.feat.airport.AirportInfoView;
import net.dhleong.opengps.feat.navfix.NavFixInfoView;

import timber.log.Timber;

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

    public static void intoWaypoint(Context context, AeroObject waypoint) {
        if (waypoint instanceof Airport) {
            Timber.v("view airport");
            NavigateUtil.into(context, AirportInfoView.class,
                R.layout.feat_airport, (Airport) waypoint);
        } else if (waypoint instanceof Navaid) {
            Timber.v("TODO view navaid");
        } else if (waypoint instanceof NavFix) {
            Timber.v("view navfix");
            NavigateUtil.into(context, NavFixInfoView.class,
                R.layout.feat_navfix, (NavFix) waypoint);
        }
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
