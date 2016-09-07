package net.dhleong.opengps.util.wx;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

/**
 * @author dhleong
 */
public interface WxService {

    @GET("httpparam?dataSource=metars&requestType=retrieve&format=xml&hoursBeforeNow=2&mostRecent=true")
    Observable<NoaaWxResponse> getMetar(@Query("stationString") String station);
}
