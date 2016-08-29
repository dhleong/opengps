package net.dhleong.opengps.util;

import android.support.annotation.CallSuper;

import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

/**
 * @author dhleong
 */
public abstract class BasePresenter<T> implements Presenter<T> {

    private CompositeSubscription subs = new CompositeSubscription();

    @Override
    public void onViewCreated(T view) {

    }

    @Override
    public void onViewAttached(T view) {

    }

    @Override
    @CallSuper
    public void onViewDetached(T view) {
        subs.clear();
    }

    protected void subscribe(Subscription sub) {
        subs.add(sub);
    }
}
