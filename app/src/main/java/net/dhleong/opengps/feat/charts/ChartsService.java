package net.dhleong.opengps.feat.charts;

import retrofit2.http.GET;
import retrofit2.http.Path;
import rx.Observable;

/**
 * @author dhleong
 */
public interface ChartsService {
    @GET("/Airport/{icao}.json")
    Observable<AirportCharts> getCharts(@Path("icao") String icao);
}
