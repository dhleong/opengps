package net.dhleong.opengps;

import net.dhleong.opengps.feat.airway.AirwaySearchComponent;
import net.dhleong.opengps.feat.fpl.FlightPlannerComponent;
import net.dhleong.opengps.feat.waypoint.WaypointSearchComponent;

import dagger.Component;

/**
 * @author dhleong
 */
@Component(modules = AppModule.class)
public interface AppComponent {

    AirwaySearchComponent newAirwaySearchComponent();
    FlightPlannerComponent newFlightPlannerComponent();
    WaypointSearchComponent newWaypointSearchComponent();
}
