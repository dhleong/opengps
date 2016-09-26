package net.dhleong.opengps.feat.loading;

import android.content.Context;
import android.support.annotation.StringRes;
import android.util.AttributeSet;
import android.widget.TextView;

import com.jakewharton.rxbinding.widget.RxTextView;

import net.dhleong.opengps.App;
import net.dhleong.opengps.OpenGps;
import net.dhleong.opengps.R;
import net.dhleong.opengps.status.DataKind;
import net.dhleong.opengps.status.StatusUpdate;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Indicates the status of OpenGps
 *
 * @author dhleong
 */
public class LoadingStatusView extends TextView {

    @Inject OpenGps gps;

    int readySources = 0;

    CompositeSubscription subs = new CompositeSubscription();

    public LoadingStatusView(Context context) {
        super(context);
    }

    public LoadingStatusView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LoadingStatusView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        App.activityComponent(this)
           .inject(this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (isInEditMode()) {
            setHeight(0);
        } else {
            // receive status updates
            subs.add(
                gps.statusUpdates()
                   .onBackpressureBuffer()
                   .map(this::stringify)
                   .observeOn(AndroidSchedulers.mainThread())
                   .subscribe(RxTextView.text(this), this::onError)
            );
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        subs.clear();
    }

    void onError(Throwable e) {
        Timber.e(e, "ERROR Initializing OpenGps!");
        setText(getContext().getString(
            R.string.status_error,
            e.getMessage()));
    }

    void onLoadingDone() {
        subs.add(
            Observable.just(0)
                      .delay(1, TimeUnit.SECONDS)
                      .observeOn(AndroidSchedulers.mainThread())
                      .subscribe(any -> animate()
                          .translationY(-getMeasuredHeight())
                          .withEndAction(() -> {
                              setHeight(0);
                              setTranslationY(0);
                          }))
        );
    }

    CharSequence stringify(StatusUpdate update) {
        if (update.kind == DataKind.ALL_READY) {
            onLoadingDone();
            return getContext().getString(R.string.status_all_ready);
        }

        final @StringRes int format = statusFormatRes(update);
        if (update.source == null) {
            return getContext().getString(format);
        } else {
            return getContext().getString(format, update.source.name());
        }
    }

    static @StringRes int statusFormatRes(StatusUpdate update) {
        switch (update.kind) {
        default:
        case READY: return R.string.status_ready;
        case STORAGE_READY: return R.string.status_storage_ready;
        case RAW_INIT: return R.string.status_raw_init;
        case RAW_UPDATE: return R.string.status_raw_update;
        case RAW_FETCHED: return R.string.status_raw_fetched;
        case AIRPORTS: return R.string.status_airports;
        case NAVAIDS: return R.string.status_navaids;
        case AIRWAYS: return R.string.status_airways;
        case CHARTS: return R.string.status_charts;
        }
    }

}
