package net.dhleong.opengps.feat.tuner;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.dhleong.opengps.App;
import net.dhleong.opengps.R;
import net.dhleong.opengps.connection.ConnectionDelegate;
import net.dhleong.opengps.connection.data.RadioData;
import net.dhleong.opengps.ui.DialogPrompter;
import net.dhleong.opengps.ui.IncrementRadioFreqView;
import net.dhleong.opengps.util.RadioType;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import rx.Observable;
import rx.Single;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * @author dhleong
 */
public class RadioTunerView
        extends LinearLayout
        implements DialogPrompter.PrompterView<RadioType, Void> {

    @Inject ConnectionDelegate delegate;
    @Inject Observable<RadioData> radioData;

    @BindView(R.id.freq) IncrementRadioFreqView freq;
    @BindViews({
        R.id.number_1, R.id.number_2, R.id.number_3,
        R.id.number_4, R.id.number_5, R.id.number_6,
        R.id.number_7, R.id.number_8, R.id.number_9,
        R.id.number_0
    }) List<TextView> numbers;

    RadioType mode;

    CompositeSubscription subs = new CompositeSubscription();

    public RadioTunerView(Context context) {
        super(context);
    }

    public RadioTunerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RadioTunerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
        App.activityComponent(getContext())
           .inject(this);
    }

    @Override
    public Single<Void> result(RadioType input) {
        this.mode = input;
        freq.setMode(input);

        subs.add(
            radioData.first()
                     .subscribe(data -> {
                         final float initialValue = (mode == RadioType.NAV)
                             ? data.nav1standby
                             : data.com1standby;
                         freq.set(initialValue);
                     })
        );

        for (TextView numberView : numbers) {
            numberView.setOnClickListener(v -> {
                int number = Integer.parseInt(((TextView) v).getText().toString());
                freq.feed(number);
            });
        }

        // just something that never auto-completes
        return PublishSubject.<Void>create().toSingle();
    }

    public RadioType getType() {
        return mode;
    }
}
