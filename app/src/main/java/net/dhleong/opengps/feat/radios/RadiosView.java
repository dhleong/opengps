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
import net.dhleong.opengps.connection.data.RadioData;

import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * @author dhleong
 */
public class RadiosView extends LinearLayout {

    @Inject Observable<RadioData> radioData;

    @BindView(R.id.status) TextView status;
    @BindView(R.id.radio_com) TextView radioCom;
    @BindView(R.id.radio_nav) TextView radioNav;

    @BindViews({R.id.radio_com, R.id.radio_nav}) List<View> radios;

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
        App.component(getContext())
           .inject(this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        // TODO track connection status

        setStatus(R.string.conn_status_connecting);

        subs.add(
            radioData.observeOn(AndroidSchedulers.mainThread())
                     .subscribe(data -> {
                         if (status.getVisibility() == VISIBLE) {
                             onConnected();
                         }

                         radioCom.setText(String.format(Locale.US, "%.3f", data.com1active));
                         radioNav.setText(String.format(Locale.US, "%.2f", data.nav1active));
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
