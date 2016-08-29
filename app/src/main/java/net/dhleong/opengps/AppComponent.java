package net.dhleong.opengps;

import net.dhleong.opengps.feat.fpl.FlightPlannerComponent;

import dagger.Component;

/**
 * @author dhleong
 */
@Component(modules = AppModule.class)
public interface AppComponent {

    FlightPlannerComponent newFlightPlannerComponent();
}
