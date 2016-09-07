package net.dhleong.opengps.feat.airport.pages;

import android.content.Context;
import android.content.res.Resources;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.util.AttributeSet;
import android.widget.ScrollView;
import android.widget.TextView;

import net.dhleong.opengps.Airport;
import net.dhleong.opengps.App;
import net.dhleong.opengps.R;
import net.dhleong.opengps.feat.airport.AirportPageView;
import net.dhleong.opengps.util.wx.NoaaWxResponse;
import net.dhleong.opengps.util.wx.WxService;

import java.text.SimpleDateFormat;
import java.util.Locale;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * @author dhleong
 */
public class WxPageView
        extends ScrollView
        implements AirportPageView {

    static final String DATE_FORMAT = "dd MMM, HH:mm 'UTC'";

    @Inject WxService service;

    @BindView(R.id.loading) ContentLoadingProgressBar loading;

    @BindView(R.id.observed) TextView observed;
    @BindView(R.id.wind) TextView wind;
    @BindView(R.id.visibility) TextView visibility;
    @BindView(R.id.sky) TextView sky;
    @BindView(R.id.temp_dewpoint) TextView tempAndDew;
    @BindView(R.id.altimeter) TextView altimeter;
    @BindView(R.id.metar) TextView metar;

    CompositeSubscription subs = new CompositeSubscription();

    public WxPageView(Context context) {
        super(context);
    }

    public WxPageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WxPageView(Context context, AttributeSet attrs, int defStyleAttr) {
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
    public void bind(Airport airport) {
        loading.show();
        subs.add(
            service.getMetar(airport.id())
                   .subscribeOn(Schedulers.io())
                   .observeOn(AndroidSchedulers.mainThread())
                   .subscribe(this::updateMetar, this::setError)
        );
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        subs.clear();
    }

    void updateMetar(NoaaWxResponse wx) {
        loading.hide();
        observed.setText(new SimpleDateFormat(DATE_FORMAT, Locale.US)
            .format(wx.getObservationDate()));

        Resources res = getResources();

        if (wx.windSpeed <= 0) {
            wind.setText(R.string.airport_page_wx_wind_calm);
        } else {
            wind.setText(res.getString(R.string.airport_page_wx_wind,
                wx.windDir, wx.windSpeed));
        }

        visibility.setText(res.getString(R.string.airport_page_wx_visibility,
            wx.visibility));

        final StringBuilder skyConditions = new StringBuilder(64);
        for (int i=0, len=wx.conditions.size(); i < len; i++) {
            if (i > 0) {
                skyConditions.append(", ");
            }
            NoaaWxResponse.SkyCondition cond = wx.conditions.get(i);
            if ("CLR".equals(cond.type)) {
                skyConditions.append(res.getString(R.string.airport_page_wx_sky_clear));
            } else {
                // TODO translate type to a string
                skyConditions.append(cond.type)
                             .append(" @ ")
                             .append(cond.cloudBase);
            }
        }

        sky.setText(skyConditions);

        tempAndDew.setText(res.getString(R.string.airport_page_wx_temp_and_dew,
            wx.temp, wx.dewpoint));

        altimeter.setText(res.getString(R.string.airport_page_wx_altimeter,
            wx.altimeter));

        metar.setText(wx.metar);
    }

    void setError(Throwable e) {
        loading.hide();
        observed.setText(e.getMessage());
    }
}

