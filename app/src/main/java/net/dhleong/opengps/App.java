package net.dhleong.opengps;

import android.app.Application;
import android.content.Context;
import android.view.View;

import net.dhleong.opengps.core.ActivityComponent;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import dagger.Subcomponent;
import timber.log.Timber;

/**
 * @author dhleong
 */
public class App extends Application {

    public static final String ACTIVITY_COMPONENT = "net.dhleong.opengps.activity.COMPONENT_SERVICE";

    /** Enables stubbing for functional tests */
    public interface ComponentProvider {
        AppComponent provide();
    }
    static ComponentProvider provider;


    private AppComponent appComponent;

    @Override
    public void onCreate() {
        super.onCreate();

        initComponent();

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
    }

    private void initComponent() {
        // NB: we could init a DummyAppModule or something here
        final AppModule module = new AppModule(this);
        appComponent = DaggerAppComponent.builder()
                                         .appModule(module)
                                         .build();
    }

    public static AppComponent component(final Context context) {
        final ComponentProvider provider = App.provider;
        if (provider != null) return provider.provide();

        final App app = (App) context.getApplicationContext();
        return app.appComponent;
    }

    @SuppressWarnings("ResourceType")
    public static ActivityComponent activityComponent(Context context) {
        final Object activityComponent = context.getSystemService(ACTIVITY_COMPONENT);
        if (activityComponent instanceof ActivityComponent) {
            return (ActivityComponent) activityComponent;
        }

        throw new RuntimeException("Provided context does not support ActivityModule");
    }

    /** Convenience */
    public static ActivityComponent activityComponent(View view) {
        if (view.isInEditMode()) {
            // this should never happen in production
            return (ActivityComponent) Proxy.newProxyInstance(view.getClass().getClassLoader(),
                new Class<?>[]{ActivityComponent.class},
                (proxy, method, args) -> subComponentOrNil(method));
        }
        return activityComponent(view.getContext());
    }

//    /** Convenience */
//    public static AppComponent component(View view) {
//        if (view.isInEditMode()) {
//            // this should never happen in production
//            return (AppComponent) Proxy.newProxyInstance(view.getClass().getClassLoader(),
//                new Class<?>[]{AppComponent.class},
//                (proxy, method, args) -> subComponentOrNil(method));
//        }
//        return component(view.getContext());
//    }

    private static Object subComponentOrNil(Method method) {
        Class<?> returns = method.getReturnType();
        if (returns.isInterface() && returns.isAnnotationPresent(Subcomponent.class)) {
            return Proxy.newProxyInstance(method.getDeclaringClass().getClassLoader(),
                new Class<?>[]{returns},
                (proxy, subCompMethod, args) -> subComponentOrNil(subCompMethod));
        }

        return null;
    }
}
