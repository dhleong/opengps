package net.dhleong.opengps;

import net.dhleong.opengps.core.ActivityComponent;
import net.dhleong.opengps.core.ActivityModule;

import javax.inject.Singleton;

import dagger.Component;

/**
 * @author dhleong
 */
@Singleton
@Component(modules = AppModule.class)
public interface AppComponent {

    ActivityComponent newActivityComponent(ActivityModule module);
}
