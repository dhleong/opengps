package net.dhleong.opengps.core;

import net.dhleong.opengps.feat.airport.pages.FreqsPageView;
import net.dhleong.opengps.feat.airport.pages.WxPageView;
import net.dhleong.opengps.feat.airway.AirwaySearchComponent;
import net.dhleong.opengps.feat.charts.ChartPickerComponent;
import net.dhleong.opengps.feat.charts.ChartPickerModule;
import net.dhleong.opengps.feat.connbar.ConnectionBarComponent;
import net.dhleong.opengps.feat.fpl.FlightPlannerComponent;
import net.dhleong.opengps.feat.home.HomeComponent;
import net.dhleong.opengps.feat.map.MapComponent;
import net.dhleong.opengps.feat.radios.RadiosView;
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
    ChartPickerComponent newChartPickerComponent(ChartPickerModule chartPickerModule);
    ConnectionBarComponent newConnectionBarComponent();
    HomeComponent newHomeComponent();
    FlightPlannerComponent newFlightPlannerComponent();
    MapComponent newMapComponent();
    WaypointSearchComponent newWaypointSearchComponent();

    void inject(FreqsPageView freqsPageView);
    void inject(RadiosView radiosView);
    void inject(WxPageView wxPageView);
    void inject(WaypointHeaderView waypointHeaderView);
}
