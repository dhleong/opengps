package net.dhleong.opengps;

import java.util.Arrays;

import rx.Observable;

/**
 * @author dhleong
 */
public class PreferredRoute {
    public Airport from, to;
    public CharSequence routeString;

    public String area;
    public String altitude;
    public String aircraftLimitations;
    public String direction;

    public Observable<GpsRoute> gpsRoute(OpenGps gps) {
        return gps.parseRoute(from, to, Arrays.asList(routeString.toString().split(" ")))
            .map(route -> {
                if (route.size() > 0) {
                    route.add(0, GpsRoute.Step.routeString(routeString));
                }
                return route;
            });
    }

    @Override
    public String toString() {
        return "PreferredRoute{" +
            "from=" + from +
            ", to=" + to +
            ", routeString=" + routeString +
            ", area='" + area + '\'' +
            ", altitude='" + altitude + '\'' +
            ", aircraftLimitations='" + aircraftLimitations + '\'' +
            ", direction='" + direction + '\'' +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PreferredRoute)) return false;

        PreferredRoute that = (PreferredRoute) o;

        if (!from.equals(that.from)) return false;
        if (!to.equals(that.to)) return false;
        if (area != null ? !area.equals(that.area) : that.area != null) return false;
        if (altitude != null ? !altitude.equals(that.altitude) : that.altitude != null)
            return false;
        if (aircraftLimitations != null ? !aircraftLimitations.equals(that.aircraftLimitations) : that.aircraftLimitations != null)
            return false;
        if (!(direction != null ? direction.equals(that.direction) : that.direction == null)) {
            return false;
        }

        // NB: wildly inefficient; should only really be used in tests
        return routeString.toString().equals(that.routeString.toString());
    }

    @Override
    public int hashCode() {
        int result = from.hashCode();
        result = 31 * result + to.hashCode();
        result = 31 * result + routeString.hashCode();
        result = 31 * result + (area != null ? area.hashCode() : 0);
        result = 31 * result + (altitude != null ? altitude.hashCode() : 0);
        result = 31 * result + (aircraftLimitations != null ? aircraftLimitations.hashCode() : 0);
        result = 31 * result + (direction != null ? direction.hashCode() : 0);
        return result;
    }
}
