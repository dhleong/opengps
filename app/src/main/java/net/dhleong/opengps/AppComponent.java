package net.dhleong.opengps;

import net.dhleong.opengps.core.ActivityComponent;
import net.dhleong.opengps.core.ActivityModule;
import net.dhleong.opengps.core.ActivityModuleActivity;
import net.dhleong.opengps.feat.chartDisplay.ChartDisplayView;
import net.dhleong.opengps.feat.radios.RadiosView;
import net.dhleong.opengps.feat.settings.SettingsView;

import javax.inject.Singleton;

import dagger.Component;

/**
 * @author dhleong
 */
@Singleton
@Component(modules = AppModule.class)
public interface AppComponent {

    ActivityComponent newActivityComponent(ActivityModule module);

    void inject(ActivityModuleActivity activityModuleActivity);
    void inject(ChartDisplayView chartDisplayView);
    void inject(RadiosView radiosView);
    void inject(SettingsView.PrefsFragment prefsFragment);
}
