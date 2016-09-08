package net.dhleong.opengps.feat.map;

import dagger.Subcomponent;

/**
 * @author dhleong
 */
@Subcomponent
public interface MapComponent {
    void inject(MapFeatureView mapFeatureView);
}
