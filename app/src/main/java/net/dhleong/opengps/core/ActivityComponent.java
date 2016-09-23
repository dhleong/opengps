package net.dhleong.opengps.core;

import net.dhleong.opengps.feat.airport.pages.FreqsPageView;
import net.dhleong.opengps.feat.airport.pages.WxPageView;
import net.dhleong.opengps.feat.airway.AirwaySearchComponent;
import net.dhleong.opengps.feat.charts.ChartPickerComponent;
import net.dhleong.opengps.feat.connbar.ConnectionBarComponent;
import net.dhleong.opengps.feat.fpl.FlightPlannerComponent;
import net.dhleong.opengps.feat.home.HomeComponent;
import net.dhleong.opengps.feat.map.MapComponent;
import net.dhleong.opengps.feat.navaid.NavaidInfoView;
import net.dhleong.opengps.feat.navfix.NavFixInfoView;
import net.dhleong.opengps.feat.radios.RadiosView;
import net.dhleong.opengps.feat.routes.PreferredRoutesComponent;
import net.dhleong.opengps.feat.tuner.RadioTunerView;
import net.dhleong.opengps.feat.waypoint.WaypointSearchComponent;
import net.dhleong.opengps.ui.WaypointHeaderView;
import net.dhleong.opengps.util.scopes.PerActivity;

import dagger.Subcomponent;

/**
 * @author dhleong
 */
@PerActivity
@Subcomponent(modules = ActivityModule.class)
public interface ActivityComponent {

    AirwaySearchComponent newAirwaySearchComponent();
    ChartPickerComponent newChartPickerComponent();
    ConnectionBarComponent newConnectionBarComponent();
    HomeComponent newHomeComponent();
    FlightPlannerComponent newFlightPlannerComponent();
    MapComponent newMapComponent();
    PreferredRoutesComponent newPreferredRoutesComponent();
    WaypointSearchComponent newWaypointSearchComponent();

    void inject(FreqsPageView freqsPageView);
    void inject(NavaidInfoView navaidInfoView);
    void inject(NavFixInfoView navFixInfoView);
    void inject(RadiosView radiosView);
    void inject(RadioTunerView radioTunerView);
    void inject(WaypointHeaderView waypointHeaderView);
    void inject(WxPageView wxPageView);
}
