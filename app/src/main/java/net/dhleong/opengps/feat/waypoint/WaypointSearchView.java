package net.dhleong.opengps.feat.waypoint;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.widget.TextView;

import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.widget.RxTextView;

import net.dhleong.opengps.AeroObject;
import net.dhleong.opengps.Airport;
import net.dhleong.opengps.App;
import net.dhleong.opengps.NavFix;
import net.dhleong.opengps.Navaid;
import net.dhleong.opengps.OpenGps;
import net.dhleong.opengps.R;
import net.dhleong.opengps.ui.DialogPrompter;
import net.dhleong.opengps.ui.UiUtil;
import net.dhleong.opengps.util.RxUtil;
import net.dhleong.opengps.views.BackInterceptingEditText;

import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.Single;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * @author dhleong
 */
public class WaypointSearchView
        extends CoordinatorLayout
        implements DialogPrompter.PrompterView<Class<? extends AeroObject>, AeroObject> {

    @Inject OpenGps gps;

    @BindView(R.id.input) BackInterceptingEditText input;
    @BindView(R.id.quick_match) TextView quickMatch;
//    @BindView(R.id.confirm) TextView confirm;

    CompositeSubscription subs = new CompositeSubscription();
    AeroObject quickMatchObj;
    List<AeroObject> allMatches;
    Class<? extends AeroObject> expectedType = AeroObject.class;

    public WaypointSearchView(Context context) {
        super(context);
    }

    public WaypointSearchView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WaypointSearchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
        App.activityComponent(this)
           .newWaypointSearchComponent()
           .inject(this);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();

        bindQuickMatch();

        subs.add(
            RxTextView.textChanges(input)
                      .observeOn(Schedulers.io())
                      .doOnNext(search ->
                          Timber.v("Search `%s`...", search))
                      .flatMap(search ->
                          gps.find(cleanInput(search))
                             .filter(obj -> expectedType.isAssignableFrom(obj.getClass()))
                             .toList())
                      .observeOn(AndroidSchedulers.mainThread())
                      .subscribe(quickMatches -> {
                          Timber.v("Found: %s", quickMatches);
                          allMatches = quickMatches;
                          quickMatchObj = quickMatches.size() > 0
                            ? quickMatches.get(0)
                            : null;
                          bindQuickMatch();
                      }, Timber::e)
        );

        UiUtil.requestKeyboard(input);
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        subs.clear();
    }

    @Override
    public Single<AeroObject> result(Class<? extends AeroObject> expectedType) {
        this.expectedType = expectedType == null ? AeroObject.class : expectedType;
        return Observable.merge(
            RxUtil.doneActions(input)
                  .doOnNext(any -> Timber.v("DONE!"))
                  .map(any -> allMatches)
                  .doOnNext(matches -> Timber.v("Matches=%s", matches))
                  .filter(RxUtil.notNull())
                  .flatMap(matches -> {
                      if (matches.size() == 0) {
                          return Observable.empty();
                      } else if (matches.size() == 1) {
                          return Observable.just(matches.get(0));
                      } else {
                          return DialogPrompter.prompt(getContext(),
                              getResources().getString(R.string.waypoint_multiple_title),
                              matches,
                              WaypointSearchView::describe);
                      }
                  }),
            RxView.clicks(quickMatch)
                  .map(any -> quickMatchObj),

            input.backPresses().map(any -> null)
        ).take(1).toSingle();
    }

    void bindQuickMatch() {
        if (quickMatchObj == null) {
            quickMatch.setEnabled(false);
        } else {
            quickMatch.setEnabled(true);
            quickMatch.setText(describe(quickMatchObj));
        }
    }

    static CharSequence describe(AeroObject obj) {
        String typeDesc;
        if (obj instanceof Airport) {
            typeDesc = "APT";
        } else if (obj instanceof Navaid) {
            typeDesc = ((Navaid) obj).type().name();
        } else if (obj instanceof NavFix) {
            typeDesc = "ITXN";
        } else {
            typeDesc = "";
        }

        return String.format("%s %s", obj.name(), typeDesc);
    }

    static String cleanInput(CharSequence text) {
        return text.toString().trim().toUpperCase(Locale.US);
    }
}
