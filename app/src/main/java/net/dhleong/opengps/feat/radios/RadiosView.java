package net.dhleong.opengps.feat.radios;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.dhleong.opengps.App;
import net.dhleong.opengps.R;
import net.dhleong.opengps.connection.ConnectionDelegate;
import net.dhleong.opengps.connection.data.RadioData;
import net.dhleong.opengps.modules.DummyConnection;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * @author dhleong
 */
public class RadiosView extends LinearLayout {

    @Inject Observable<RadioData> radioData;
    @Inject Observable<ConnectionDelegate.State> stateChanges;

    @BindView(R.id.status) TextView status;
    @BindView(R.id.radio_com) RadioView radioCom;
    @BindView(R.id.radio_nav) RadioView radioNav;
    @BindView(R.id.radios_mic) ActiveRadiosView radioMic;
    @BindView(R.id.radios_mon) ActiveRadiosView radioMon;

    @BindViews({
        R.id.radio_com, R.id.radio_nav,
        R.id.selected_radios
    }) List<View> radios;

    private CompositeSubscription subs = new CompositeSubscription();

    public RadiosView(Context context) {
        super(context);
    }

    public RadiosView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RadiosView(Context context, AttributeSet attrs, int defStyleAttr) {
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
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (isInEditMode()) {
            RadioData data = new DummyConnection()
                .subscribe(RadioData.class)
                .toBlocking()
                .first();
            updateRadios(data);
            return;
        }

        setStatus(R.string.conn_status_connecting);

        subs.add(
            stateChanges.observeOn(AndroidSchedulers.mainThread())
                        .subscribe(state -> {
                            Timber.v("Got state %s", state);
                            switch (state) {
                            case CONNECTED:
                                if (status.getVisibility() != GONE) {
                                    setStatus(R.string.conn_status_connected);
                                }
                                break;
                            case CONNECTING:
                                setStatus(R.string.conn_status_connecting);
                                break;
                            case DISCONNECTED:
                                setStatus(R.string.conn_status_disconnected);
                                break;
                            }
                        })
        );

        subs.add(
            radioData.observeOn(AndroidSchedulers.mainThread())
                     .subscribe(data -> {
                         if (status.getVisibility() == VISIBLE) {
                             onConnected();
                         }

                         updateRadios(data);
                     })
        );
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        subs.clear();
    }

    void onConnected() {
        ButterKnife.apply(radios, new ButterKnife.Action<View>() {
            @Override
            public void apply(@NonNull View view, int index) {
                view.setVisibility(VISIBLE);
            }
        });
        status.setVisibility(GONE);
    }

    void updateRadios(RadioData data) {
        radioCom.setFrequencies(data.com1active, data.com1standby);
        radioNav.setFrequencies(data.nav1active, data.nav1standby);
        radioMic.setState(data.comTransmit1, data.comTransmit2);
        radioMon.setState(
            data.comTransmit1 || data.comReceiveAll,
            data.comTransmit2 || data.comReceiveAll
        );
    }

    void setStatus(@StringRes int statusRes) {
        ButterKnife.apply(radios, new ButterKnife.Action<View>() {
            @Override
            public void apply(@NonNull View view, int index) {
                view.setVisibility(GONE);
            }
        });
        status.setVisibility(VISIBLE);
        status.setText(statusRes);
    }
}
