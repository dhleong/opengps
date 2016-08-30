package net.dhleong.opengps.core;

import android.content.Context;

import dagger.Module;
import dagger.Provides;

/**
 * @author dhleong
 */
@Module
public class ActivityModule {

    private final ActivityModuleActivity activity;

    public ActivityModule(ActivityModuleActivity activity) {
        this.activity = activity;
    }

    @Provides Context context() {
        return activity;
    }
}
