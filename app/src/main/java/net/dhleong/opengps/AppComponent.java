package net.dhleong.opengps;

import net.dhleong.opengps.feat.airway.AirwaySearchComponent;
import net.dhleong.opengps.feat.fpl.FlightPlannerComponent;
import net.dhleong.opengps.feat.waypoint.WaypointSearchComponent;

import javax.inject.Singleton;

import dagger.Component;

/**
 * @author dhleong
 */
@Singleton
@Component(modules = AppModule.class)
public interface AppComponent {

    AirwaySearchComponent newAirwaySearchComponent();
    FlightPlannerComponent newFlightPlannerComponent();
    WaypointSearchComponent newWaypointSearchComponent();
}
