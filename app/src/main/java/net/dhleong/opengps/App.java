package net.dhleong.opengps;

import android.app.Application;
import android.content.Context;

/**
 * @author dhleong
 */
public class App extends Application {

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
}
