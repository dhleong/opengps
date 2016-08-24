package net.dhleong.opengps;

import java.util.HashSet;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

/**
 * The GpsModel is a convenience layer on top of
 *  {@link OpenGps} for implementing a GPS UI. It
 *  maintains some state and provides simple event
 *  streams to observe and inputs to provide
 *
 * @author dhleong
 */
public class GpsModel {

    private static final int DEFAULT_SET_SIZE = 1024;

    private final OpenGps gps;

    private final PublishSubject<AeroObject> discovered = PublishSubject.create();
    private final PublishSubject<AeroObject> removed = PublishSubject.create();
    private final PublishSubject<LatLngRange> updates = PublishSubject.create();

    private HashSet<AeroObject> visibleSet = new HashSet<>(DEFAULT_SET_SIZE);
    private HashSet<AeroObject> workspaceSet = new HashSet<>(DEFAULT_SET_SIZE);

    private LatLngRange latLngRange = new LatLngRange(0, 0, 0);

    Subscription updatesSub;

    public GpsModel(OpenGps gps) {
        this.gps = gps;

        updatesSub =
            updates.observeOn(Schedulers.io())
                   .throttleLast(150, TimeUnit.MILLISECONDS)
                   .flatMap(loc -> gps.anyNear(loc.lat, loc.lng, loc.range).toList())
                   .subscribe(newVisibleList -> {
                       workspaceSet.clear();
                       for (int i=0, len=newVisibleList.size(); i < len; i++) {
                           AeroObject o = newVisibleList.get(i);
                           if (!visibleSet.contains(o)) {
                               discovered.onNext(o);
                           }

                           workspaceSet.add(o);
                       }

                       for (AeroObject o : visibleSet) {
                           if (!workspaceSet.contains(o)) {
                               removed.onNext(o);
                           }
                       }

                       final HashSet<AeroObject> oldVisible = visibleSet;
                       visibleSet = workspaceSet;
                       workspaceSet = oldVisible;
                   });
    }

    public void destroy() {
        Subscription sub = updatesSub;
        if (sub != null) sub.unsubscribe();
    }

    /**
     * @return A stream of AeroObjects to add to a display
     *  list; this will only repeat elements if they've
     *  first been emitted from {@link #removedObjects()}
     */
    public Observable<AeroObject> discoveredObjects() {
        return discovered;
    }

    /**
     * @return A stream of AeroObjects to remove from
     *  a display list; this will only emit elements
     *  that have been previously emitted from {@link #discoveredObjects()}
     */
    public Observable<AeroObject> removedObjects() {
        return removed;
    }

    /**
     * Set the current location and range. This may result
     *  in new items being emitted from {@link #discoveredObjects()}
     *  and/or {@link #removedObjects()}
     *
     * @param range Visible range around lat/lng, in nautical miles
     */
    public void setLocation(double lat, double lng, float range) {
        workspaceSet.clear();

        // wacky hack to avoid excessive allocations; we could
        //  pool them, but that's probably unnecessary overhead;
        //  we only care about the most recent location anyway
        latLngRange.lat = lat;
        latLngRange.lng = lng;
        latLngRange.range = range;
        updates.onNext(latLngRange);
    }

    private static class LatLngRange {
        double lat;
        double lng;
        float range;

        LatLngRange(double lat, double lng, float range) {
            this.lat = lat;
            this.lng = lng;
            this.range = range;
        }
    }
}
