package net.dhleong.opengps.feat.fpl;

import dagger.Subcomponent;

/**
 * @author dhleong
 */
@Subcomponent
public interface FlightPlannerComponent {
    void inject(FlightPlannerView view);
}
