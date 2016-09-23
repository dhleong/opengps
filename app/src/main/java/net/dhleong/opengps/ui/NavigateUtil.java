package net.dhleong.opengps.ui;

import android.content.Context;
import android.content.ContextWrapper;
import android.support.annotation.LayoutRes;
import android.view.View;

import net.dhleong.opengps.AeroObject;
import net.dhleong.opengps.Airport;
import net.dhleong.opengps.NavFix;
import net.dhleong.opengps.Navaid;
import net.dhleong.opengps.R;
import net.dhleong.opengps.feat.airport.AirportInfoView;
import net.dhleong.opengps.feat.navaid.NavaidInfoView;
import net.dhleong.opengps.feat.navfix.NavFixInfoView;
import net.dhleong.opengps.feat.tuner.RadioTunerView;
import net.dhleong.opengps.util.RadioType;

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
        IntoNavigator nav = navigator(context);
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
            Timber.v("view navaid");
            NavigateUtil.into(context, NavaidInfoView.class,
                R.layout.feat_navaid, (Navaid) waypoint);
        } else if (waypoint instanceof NavFix) {
            Timber.v("view navfix");
            NavigateUtil.into(context, NavFixInfoView.class,
                R.layout.feat_navfix, (NavFix) waypoint);
        }
    }

    public static boolean intoRadio(Context context, RadioType type) {
        IntoNavigator navigator = navigator(context);
        View view = navigator.current();
        if (view instanceof RadioTunerView) {
            if (((RadioTunerView) view).getType() == type) {
                // same kind of radio; nop
                return false;
            } else {
                // other kind of radio; pop back first
                backFrom(view);
            }
        }

        into(view.getContext(), RadioTunerView.class, R.layout.feat_tuner, type);
        return true;
    }

    /**
     * Executes {@link #backFrom(View)} IF the *current*
     *  view is some kind of {@link RadioTunerView}
     */
    public static boolean backFromRadio(Context context) {
        final IntoNavigator navigator = navigator(context);
        final View view = navigator.current();
        if (view instanceof RadioTunerView) {
            backFrom(view);
            return true;
        }

        return false;
    }

    private static IntoNavigator navigator(Context context) {
        // handle ContextWrappers
        while (!(context instanceof IntoNavigator) && context instanceof ContextWrapper) {
            context = ((ContextWrapper) context).getBaseContext();
        }
        if (!(context instanceof IntoNavigator)) {
            throw new IllegalArgumentException("Given Context must implement IntoNavigator");
        }
        return (IntoNavigator) context;
    }


    public interface IntoNavigator {
        View navigateInto(View view);

        /**
         * Inflate the given layout using an appropriate
         *  view parent so layout params are inflated,
         *  but DO NOT attach
         */
        View inflate(@LayoutRes int layoutResId);

        View current();
    }
}
