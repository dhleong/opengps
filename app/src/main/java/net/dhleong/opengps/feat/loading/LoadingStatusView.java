package net.dhleong.opengps.feat.loading;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import net.dhleong.opengps.App;
import net.dhleong.opengps.OpenGps;

import javax.inject.Inject;

import rx.subscriptions.CompositeSubscription;

/**
 * Indicates the status of OpenGps
 *
 * @author dhleong
 */
public class LoadingStatusView extends TextView {

    @Inject OpenGps gps;

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

        // TODO receive status updates

        // TODO until then...:
        onLoadingDone();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        subs.clear();
    }

    void onLoadingDone() {
        setHeight(0);
    }

}
