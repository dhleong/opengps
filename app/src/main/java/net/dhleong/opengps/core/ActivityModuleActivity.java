package net.dhleong.opengps.core;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import com.f2prateek.rx.preferences.Preference;

import net.dhleong.opengps.App;
import net.dhleong.opengps.connection.ConnectionDelegate;

import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;
import javax.inject.Named;

import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

import static net.dhleong.opengps.modules.PrefsModule.PREF_KEEP_SCREEN;

/**
 * @author dhleong
 */
public class ActivityModuleActivity extends AppCompatActivity {

    static AtomicInteger visibleActivities = new AtomicInteger();

    @Inject @Named(PREF_KEEP_SCREEN) Preference<Boolean> keepScreenOn;
    @Inject ConnectionDelegate connection;

    private ActivityComponent myActivityComponent;
    private ActivityModule module;

    CompositeSubscription subs = new CompositeSubscription();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        App.component(this)
           .inject(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (0 == visibleActivities.getAndIncrement()) {
            connection.open();
        }

        subs.add(
            keepScreenOn.asObservable()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(keepScreenOn -> {
                            Timber.v("set(keepScreenOn, %s)", keepScreenOn);
                            getWindow().getDecorView().setKeepScreenOn(keepScreenOn);
                        })
        );
    }

    @Override
    protected void onStop() {
        super.onStop();

        subs.clear();

        if (visibleActivities.decrementAndGet() == 0) {
            connection.close();
        }
    }

    private ActivityModule onCreateActivityModule() {
        return new ActivityModule(this);
    }

    @Override
    public Object getSystemService(@NonNull String name) {
        // enforce a per-activity singleton component instance
        //  (so @PerActivity scope actually works)
        if (App.ACTIVITY_COMPONENT.equals(name)) {
            final ActivityComponent existing = myActivityComponent;
            if (existing != null) return existing;

            if (module == null) {
                module = onCreateActivityModule();
            }

            //noinspection WrongConstant
            return myActivityComponent =
                App.component(this)
                   .newActivityComponent(module);
        }

        return super.getSystemService(name);
    }
}
