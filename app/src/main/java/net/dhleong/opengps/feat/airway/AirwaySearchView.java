package net.dhleong.opengps.feat.airway;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.View;

import com.jakewharton.rxbinding.view.RxView;

import net.dhleong.opengps.AeroObject;
import net.dhleong.opengps.Airway;
import net.dhleong.opengps.App;
import net.dhleong.opengps.OpenGps;
import net.dhleong.opengps.R;
import net.dhleong.opengps.ui.DialogPrompter;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Single;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * @author dhleong
 */
public class AirwaySearchView
        extends CoordinatorLayout
        implements DialogPrompter.PrompterView<AeroObject, AirwaySearchResult> {

    @Inject OpenGps gps;

    @BindView(R.id.load) View loadButton;

    private AeroObject start, exit;
    private Airway airway;

    CompositeSubscription subs = new CompositeSubscription();
    private List<Airway> airwayCandidates;

    public AirwaySearchView(Context context) {
        super(context);
    }

    public AirwaySearchView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AirwaySearchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);

        App.activityComponent(this)
           .newAirwaySearchComponent()
           .inject(this);
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        subs.clear();
    }

    @Override
    public Single<AirwaySearchResult> result(AeroObject start) {
        this.start = start;

        // TODO bind UI; show prompt for airway, exit
        subs.add(
            gps.airwaysFor(start)
               .subscribeOn(Schedulers.io())
               .toList()
               .observeOn(AndroidSchedulers.mainThread())
               .subscribe(candidates -> {
                   airwayCandidates = candidates;
                   pickAirway();
               })
        );

        return RxView.clicks(loadButton)
                     .first()
                     .map(any -> new AirwaySearchResult(airway, start, exit))
                     .toSingle();
    }

    void pickAirway() {
        subs.add(
            DialogPrompter.prompt(getContext(),
                    getContext().getString(R.string.airway_select),
                    airwayCandidates,
                    AeroObject::name)
              .observeOn(AndroidSchedulers.mainThread())
              .subscribe(chosenAirway -> {
                  airway = chosenAirway;
                  pickExit();
              })
        );
    }

    void pickExit() {
        subs.add(
            DialogPrompter.prompt(getContext(),
                getContext().getString(R.string.airway_select_exit),
                airway.getPoints(),
                AeroObject::name)
              .observeOn(AndroidSchedulers.mainThread())
              .subscribe(chosenExit -> {
                  exit = chosenExit;
                  loadButton.setEnabled(true);
              })
        );
    }

}
