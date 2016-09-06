package net.dhleong.opengps.feat.connbar;

import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import net.dhleong.opengps.App;
import net.dhleong.opengps.R;
import net.dhleong.opengps.connection.ConnectionType;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

/**
 * @author dhleong
 */
public class ConnectionAppBarLayout extends AppBarLayout {

    @Inject Observable<ConnectionType> connectionType;

    @BindView(R.id.toolbar) Toolbar toolbar;

    private Subscription subs;

    public ConnectionAppBarLayout(Context context) {
        super(context);
    }

    public ConnectionAppBarLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
        App.activityComponent(this)
           .newConnectionBarComponent()
           .inject(this);

        toolbar.setTitle(R.string.app_name);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        subs = connectionType.observeOn(AndroidSchedulers.mainThread())
            .subscribe(type -> {
                if (type == ConnectionType.NONE) {
                    switchToToolbar();
                } else {
                    switchToConnection();
                }
            });
    }

    void switchToToolbar() {
        if (toolbar.getParent() == null) {
            // TODO remove the connection view
            addView(toolbar, 0);
        }
    }

    void switchToConnection() {
        // TODO
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        subs.unsubscribe();
    }

    public void intercept(View oldView, View newView) {
        // remove any views that were added
        int myKids = getChildCount();
        if (myKids > 1 ) {
            AppBarLayout oldViewLayout = new AppBarLayout(getContext());
            oldViewLayout.setId(R.id.appbar);
            while (myKids-- > 1) {
                View removed = getChildAt(1);
                removeViewAt(1);
                oldViewLayout.addView(removed);
            }
            ((ViewGroup) oldView).addView(oldViewLayout);
        }

        // remove any of their AppBarLayouts and add their children to us
        AppBarLayout layout = ButterKnife.findById(newView, R.id.appbar);
        if (layout != null) {
            ((ViewGroup) newView).removeView(layout);

            for (int i=0, kids=layout.getChildCount(); i < kids; i++) {
                View v = layout.getChildAt(0);
                layout.removeViewAt(0);
                addView(v);
            }
        }
    }
}
