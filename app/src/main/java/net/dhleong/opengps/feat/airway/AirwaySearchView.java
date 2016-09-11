package net.dhleong.opengps.feat.airway;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.RelativeSizeSpan;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jakewharton.rxbinding.view.RxView;

import net.dhleong.opengps.AeroObject;
import net.dhleong.opengps.Airway;
import net.dhleong.opengps.App;
import net.dhleong.opengps.NavFix;
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
        extends LinearLayout
        implements DialogPrompter.PrompterView<AeroObject, AirwaySearchResult> {

    @Inject OpenGps gps;

    @BindView(R.id.airway) TextView airwayLabel;
    @BindView(R.id.entry) TextView entryLabel;
    @BindView(R.id.exit) TextView exitLabel;
    @BindView(R.id.confirm) View loadButton;

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

        if (!isInEditMode()) {
            App.activityComponent(this)
               .newAirwaySearchComponent()
               .inject(this);
        }
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        subs.clear();
    }

    @Override
    public Single<AirwaySearchResult> result(AeroObject start) {
        this.start = start;

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

        subs.add(
            RxView.clicks(airwayLabel)
                  .subscribe(any -> pickAirway())
        );

        subs.add(
            RxView.clicks(exitLabel)
                  .subscribe(any -> pickExit())
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
                  updateUi();
                  pickExit();
              })
        );
    }

    void pickExit() {
        subs.add(
            DialogPrompter.prompt(getContext(),
                getContext().getString(R.string.airway_select_exit),
                airway.getPoints(),
                AirwaySearchView::describeExit)
              .observeOn(AndroidSchedulers.mainThread())
              .subscribe(chosenExit -> {
                  exit = chosenExit;
                  updateUi();
              })
        );
    }

    void updateUi() {
        final Airway airway = this.airway;
        final AeroObject entry = start;
        final AeroObject exit = this.exit;
        loadButton.setEnabled(entry != null && exit != null && airway != null);

        if (airway == null) {
            airwayLabel.setText(null);
        } else {
            airwayLabel.setText(airway.name());
        }

        if (entry == null) {
            entryLabel.setText(null);
        } else {
            entryLabel.setText(entry.name());
        }

        if (exit == null) {
            exitLabel.setText(null);
        } else {
            exitLabel.setText(exit.name());
        }
    }

    static CharSequence describeExit(AeroObject exit) {
        if (exit instanceof NavFix) return exit.name();

        SpannableStringBuilder builder = new SpannableStringBuilder(exit.id());
        builder.append(' ');
        int start = builder.length();
        builder.append(exit.name());
        builder.setSpan(new RelativeSizeSpan(0.7f), start, builder.length(),
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return builder;
    }
}
