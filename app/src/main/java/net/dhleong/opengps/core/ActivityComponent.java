package net.dhleong.opengps.core;

import net.dhleong.opengps.feat.airway.AirwaySearchComponent;
import net.dhleong.opengps.feat.fpl.FlightPlannerComponent;
import net.dhleong.opengps.feat.waypoint.WaypointSearchComponent;
import net.dhleong.opengps.util.scopes.PerActivity;

import dagger.Subcomponent;

/**
 * @author dhleong
 */
@PerActivity
@Subcomponent(modules = ActivityModule.class)
public interface ActivityComponent {

    AirwaySearchComponent newAirwaySearchComponent();
    FlightPlannerComponent newFlightPlannerComponent();
    WaypointSearchComponent newWaypointSearchComponent();

}
