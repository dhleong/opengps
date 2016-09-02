package net.dhleong.opengps.feat.charts;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;

/**
 * @author dhleong
 */
@Module
public class ChartPickerModule {
    static @Provides ChartsService service(@Named("charts") Retrofit retrofit) {
        return retrofit.create(ChartsService.class);
    }
}
