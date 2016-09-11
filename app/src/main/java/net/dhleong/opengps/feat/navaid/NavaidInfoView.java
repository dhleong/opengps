package net.dhleong.opengps.feat.navaid;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.jakewharton.rxbinding.view.RxView;

import net.dhleong.opengps.App;
import net.dhleong.opengps.Navaid;
import net.dhleong.opengps.R;
import net.dhleong.opengps.connection.ConnectionDelegate;
import net.dhleong.opengps.ui.DialogPrompter;
import net.dhleong.opengps.ui.TextUtil;
import net.dhleong.opengps.ui.WaypointHeaderView;
import net.dhleong.opengps.views.MorseView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Single;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * @author dhleong
 */
public class NavaidInfoView
        extends CoordinatorLayout
        implements DialogPrompter.PrompterView<Navaid, Void> {

    @Inject ConnectionDelegate conn;

    @BindView(R.id.waypoint) WaypointHeaderView headerView;
    @BindView(R.id.type) TextView type;
    @BindView(R.id.freq_container) View freqContainer;
    @BindView(R.id.freq) TextView freq;
    @BindView(R.id.ident) MorseView ident;

    CompositeSubscription subs = new CompositeSubscription();

    public NavaidInfoView(Context context) {
        super(context);
    }

    public NavaidInfoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NavaidInfoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
        App.activityComponent(this)
           .inject(this);
    }

    @Override
    public Single<Void> result(Navaid input) {
        headerView.bind(input);

        subs.add(
            RxView.clicks(freqContainer)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(any ->
                conn.setNav1Standby((float) input.freq()))
        );

        type.setText(input.type().toString());
        freq.setText(TextUtil.formatFreq(input.freq()));
        ident.setText(input.id());

        // just something that never auto-completes
        return PublishSubject.<Void>create().toSingle();
    }
}
