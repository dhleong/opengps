package net.dhleong.opengps.storage;

import net.dhleong.opengps.AeroObject;
import net.dhleong.opengps.Airport;
import net.dhleong.opengps.DataSource;
import net.dhleong.opengps.LabeledFrequency;
import net.dhleong.opengps.NavFix;
import net.dhleong.opengps.Navaid;
import net.dhleong.opengps.Storage;
import net.dhleong.opengps.impl.Const;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import rx.Observable;

/**
 * @author dhleong
 */
public class InMemoryStorage implements Storage {

    private static final double MIN_LAT = Math.toRadians(-90d);  // -PI/2
    private static final double MAX_LAT = Math.toRadians(90d);   //  PI/2
    private static final double MIN_LON = Math.toRadians(-180d); // -PI
    private static final double MAX_LON = Math.toRadians(180d);  //  PIkk0j

    private static final int EXPECTED_AIRPORTS = 20480;
    private static final int EXPECTED_NAVAIDS = 4096;
    private static final int EXPECTED_FIXES = 66560;

    private HashSet<String> dataSources = new HashSet<>();

    private final HashMap<String, Airport> airportsByNumber = new HashMap<>(EXPECTED_AIRPORTS);
    private HashMap<String, Airport> airportsById = new HashMap<>(EXPECTED_AIRPORTS);
    private HashMap<String, Navaid> navaidsById = new HashMap<>(EXPECTED_NAVAIDS);
    private HashMap<String, NavFix> fixesById = new HashMap<>(EXPECTED_FIXES);
    private ArrayList<AeroObject> allObjects = new ArrayList<>(
        EXPECTED_AIRPORTS + EXPECTED_NAVAIDS + EXPECTED_FIXES);

    @Override
    public Observable<Storage> load() {
        return Observable.just(this);
    }

    @Override
    public boolean isLoaded() {
        return true;
    }

    @Override
    public void finishSource(DataSource source) {
        dataSources.add(source.id());
    }

    @Override
    public boolean hasDataSource(DataSource source) {
        return dataSources.contains(source.id());
    }

    @Override
    public void put(Airport airport) {
        airportsByNumber.put(airport.number(), airport);
        airportsById.put(airport.id(), airport);
        allObjects.add(airport);
    }

    @Override
    public void put(Navaid navaid) {
        navaidsById.put(navaid.id(), navaid);
        allObjects.add(navaid);
    }

    @Override
    public void put(NavFix navFix) {
        fixesById.put(navFix.id(), navFix);
        allObjects.add(navFix);
    }

    @Override
    public void addIlsFrequency(String airportNumber, LabeledFrequency freq) {
        addFrequency(airportNumber, Airport.FrequencyType.NAV, freq);
    }

    @Override
    public void addFrequency(String airportNumber, Airport.FrequencyType type, LabeledFrequency freq) {
        synchronized (airportsByNumber) {
            Airport apt = airportsByNumber.get(airportNumber);
            if (apt == null) {
                throw new RuntimeException("No airport known for number " + airportNumber);
            }

            apt.addFrequency(type, freq);
        }
    }

    @Override
    public void beginTransaction() {
        // nop
    }

    @Override
    public void markTransactionSuccessful() {
        // nop
    }

    @Override
    public void endTransaction() {
        // nop
    }

    @Override
    public Observable<AeroObject> find(String objectId) {
        final Airport apt = airportsById.get(objectId);
        final NavFix navFix = fixesById.get(objectId);
        final Navaid navaid = navaidsById.get(objectId);

        // TODO would be nice to avoid allocating an array here:
        final List<AeroObject> list = new ArrayList<>(3);
        if (apt != null) list.add(apt);
        if (navaid != null) list.add(navaid);
        if (navFix != null) list.add(navFix);
        return Observable.from(list);
    }

    @Override
    public Observable<AeroObject> findNear(double lat, double lng, float range) {
        // FIXME we should store the points in a QuadTree or something

        // "radius" of search area around lat,lng in km
        final float R = range * Const.NM_TO_KM;

        // angular distance in radians on earth's surface
        final float radDist = R / Const.EARTH_RADIUS_KM;

        // lat,lng in radians
        final double radLat = Math.toRadians(lat);
        final double radLng = Math.toRadians(lng);

        double minLat = radLat - radDist;
        double maxLat = radLat + radDist;

        double minLng, maxLng;
        if (minLat > MIN_LAT && maxLat < MAX_LAT) {
			double deltaLng = Math.asin(Math.sin(radDist) /
				Math.cos(radLat));
			minLng = radLng - deltaLng;
			if (minLng < MIN_LON) minLng += 2d * Math.PI;
			maxLng = radLng + deltaLng;
			if (maxLng > MAX_LON) maxLng -= 2d * Math.PI;
		} else {
			// a pole is within the distance
			minLat = Math.max(minLat, MIN_LAT);
			maxLat = Math.min(maxLat, MAX_LAT);
			minLng = MIN_LON;
			maxLng = MAX_LON;
		}

        ArrayList<AeroObject> objs = allObjects;
        final int len = objs.size();

        final double minLatDeg = Math.toDegrees(minLat);
        final double maxLatDeg = Math.toDegrees(maxLat);
        final double minLngDeg = Math.toDegrees(minLng);
        final double maxLngDeg = Math.toDegrees(maxLng);
        Iterator<AeroObject> iter = new Iterator<AeroObject>() {
            int i=0;
            @Override
            public boolean hasNext() {
                for (; i < len; i++) {
                    AeroObject obj = objs.get(i);
                    final double lat = obj.lat();
                    final double lng = obj.lng();
                    if (lat >= minLatDeg && lat <= maxLatDeg
                            && lng >= minLngDeg && lng <= maxLngDeg) {
                        return true;
                    }
                }
                return false;
            }

            @Override
            public AeroObject next() {
                return objs.get(i++); // move forward
            }

        };

        return Observable.defer(() -> Observable.from(() -> iter));
    }
}
