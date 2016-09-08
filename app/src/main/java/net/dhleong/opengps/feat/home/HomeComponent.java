package net.dhleong.opengps.feat.home;

import dagger.Subcomponent;

/**
 * @author dhleong
 */
@Subcomponent
public interface HomeComponent {
    void inject(HomeView homeView);
}
