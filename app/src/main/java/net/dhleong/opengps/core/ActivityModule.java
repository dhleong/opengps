package net.dhleong.opengps.core;

import android.content.Context;

import net.dhleong.opengps.util.wx.WxService;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;

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

    @Provides WxService wxService(@Named("wx") Retrofit retrofit) {
        return retrofit.create(WxService.class);
    }
}
