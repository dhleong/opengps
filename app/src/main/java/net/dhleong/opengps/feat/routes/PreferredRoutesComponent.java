package net.dhleong.opengps.feat.routes;

import dagger.Subcomponent;

/**
 * @author dhleong
 */
@Subcomponent
public interface PreferredRoutesComponent {
    void inject(PreferredRoutesView preferredRoutesView);
}
