package net.dhleong.opengps.feat.airway;

import dagger.Subcomponent;

/**
 * @author dhleong
 */
@Subcomponent
public interface AirwaySearchComponent {
    void inject(AirwaySearchView airwaySearchView);
}
