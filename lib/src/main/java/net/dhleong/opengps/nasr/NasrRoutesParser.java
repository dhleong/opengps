package net.dhleong.opengps.nasr;

import net.dhleong.opengps.Airport;
import net.dhleong.opengps.PreferredRoute;
import net.dhleong.opengps.nasr.util.Parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okio.ByteString;
import okio.Options;

/**
 * @author dhleong
 */
class NasrRoutesParser {
    static final Options HEADERS = Options.of(
        ByteString.encodeUtf8("PFR1"),
        ByteString.encodeUtf8("PFR2")
    );
    static final int TYPE_MAIN = 0;
    static final int TYPE_SEGMENT = 1;


    public static List<PreferredRoute> find(Parser pfr, Airport from, Airport to) throws IOException {
        ArrayList<PreferredRoute> results = new ArrayList<>();

        PreferredRoute currentRoute = null;
        while (!pfr.exhausted()) {
            switch (pfr.select(HEADERS)) {
            case TYPE_MAIN:
                if (currentRoute != null) {
                    results.add(currentRoute);
                    currentRoute = null;
                }

                String thisFrom = pfr.string(5);
                if (!from.simpleId.equals(thisFrom)) {
                    pfr.skipToLineEnd();
                    continue;
                }

                String thisTo = pfr.string(5);
                if (!to.simpleId.equals(thisTo)) {
                    pfr.skipToLineEnd();
                    continue;
                }

                currentRoute = readRoute(pfr);
                currentRoute.from = from;
                currentRoute.to = to;
                break;

            case TYPE_SEGMENT:
                if (currentRoute != null) {
                    readSegment(pfr, currentRoute);
                }
                pfr.skipToLineEnd();
                break;

            default:
                pfr.skipToLineEnd();
            }
        }

        if (currentRoute != null) {
            results.add(currentRoute);
        }
        return results;
    }

    static PreferredRoute readRoute(Parser pfr) throws IOException {
        PreferredRoute route = new PreferredRoute();

        pfr.skip(3); // route type
        pfr.skip(2); // route seq no
        pfr.skip(30); // type (described)

        route.area = pfr.string(75);
        route.altitude = pfr.string(40);
        route.aircraftLimitations = pfr.string(50);

        pfr.skip(45); // GMT effective hours descriptions (1-3)

        route.direction = pfr.string(20);

        route.routeString = new StringBuilder(128);
        return route;
    }

    static void readSegment(Parser pfr, PreferredRoute route) throws IOException {
        pfr.skip(5); // from
        pfr.skip(5); // to
        pfr.skip(3); // type
        pfr.skip(2); // route id sequence number
        pfr.skip(3); // segment sequence number within the route

        String id = pfr.string(48); // segment identifier
        String type = pfr.string(7); // segment type

        pfr.skip(2); // fix state
        pfr.skip(2); // icao region
        pfr.skip(2); // navaid facility type
        pfr.skip(84); // navaid facility type (described)

        int radial = 0;
        boolean hasRadial = false;
        try {
            radial = (int) pfr.decimalNumber(3); // radial
            hasRadial = true;
        } catch (NumberFormatException e) {
            // ignore
        }

        StringBuilder builder = (StringBuilder) route.routeString;
        if (builder.length() > 0) {
            builder.append(' ');
        }

        builder.append(id);

        if (hasRadial) builder.append(radial).append("R");
    }
}
