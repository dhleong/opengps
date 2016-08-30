package net.dhleong.opengps.feat.waypoint;

import dagger.Subcomponent;

/**
 * @author dhleong
 */
@Subcomponent
public interface WaypointSearchComponent {
    void inject(WaypointSearchView waypointSearchView);
}
