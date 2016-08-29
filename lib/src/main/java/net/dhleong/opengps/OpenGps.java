package net.dhleong.opengps;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;

/**
 * OpenGps is the primary means of accessing data stored
 *  in a {@link DataSource}. The data will be stored in
 *  a {@link Storage}, which may take some time to prepare;
 *  this class shields you from worrying about that, and
 *  provides a unified interface that hides the details
 *  of fetching, loading, and querying the underlying data.
 *
 * {@link net.dhleong.opengps.storage.InMemoryStorage} is provided
 *  if reading the data set from disk on every access is acceptable,
 *  and if you have sufficient RAM to hold the entire data set in
 *  memory.
 */
public class OpenGps {

    BehaviorSubject<Storage> storage = BehaviorSubject.create();

    private OpenGps(Builder builder) {
        // begin eager load
        builder.storage.load().flatMap(s ->
            Observable.from(builder.sources)
                      .flatMap(dataSource -> {
                          if (!s.hasDataSource(dataSource)) {
                              return dataSource
                                  .loadInto(s)
                                  .flatMap(result -> {
                                      // TODO better logging
                                      if (!result) {
                                          System.err.println("Failed to load " + dataSource);
                                          return Observable.empty();
                                      }
                                      return Observable.just(s);
                                  });
                          } else {
                              return Observable.just(s);
                          }
                      })
        ).last().subscribe(storage::onNext);
    }

    public Observable<AeroObject> find(String objectId) {
        return withStorage(s -> s.find(objectId));
    }

    public Observable<Airport> airport(String airportId) {
        return find(airportId)
            .flatMap(obj -> {
                if (obj instanceof Airport) {
                    return Observable.just((Airport) obj);
                } else {
                    return Observable.empty();
                }
            });
    }

    public Observable<Airway> airway(String airwayId) {
        return find(airwayId)
            .flatMap(obj -> {
                if (obj instanceof Airway) {
                    return Observable.just((Airway) obj);
                } else {
                    return Observable.empty();
                }
            });
    }

    public Observable<Navaid> navaid(String navaidId) {
        return find(navaidId)
            .flatMap(obj -> {
                if (obj instanceof Navaid) {
                    return Observable.just((Navaid) obj);
                } else {
                    return Observable.empty();
                }
            });
    }

    public Observable<AeroObject> anyNear(double lat, double lng, float range) {
        return withStorage(storage -> storage.findNear(lat, lng, range));
    }

    public Observable<GpsRoute> parseRoute(Airport start, Airport end, List<String> rawRouteParts) {
        return parseRoute(start, end, rawRouteParts, GpsRoute.FLAGS_DEFAULT);
    }
    public Observable<GpsRoute> parseRoute(Airport start, Airport end, List<String> rawRouteParts, int flags) {
        return withStorage(storage -> Observable.from(rawRouteParts)
            .flatMap(storage::findFix)
            .subscribeOn(Schedulers.io())
        ).filter(obj -> obj != null)
         .toList()
         .observeOn(Schedulers.computation())
         .map(objs -> {
             final GpsRoute route = new GpsRoute(flags);
             route.add(start);

             System.out.println("FOUND: " + objs);
             for (int i=0, len=objs.size(); i < len; i++) {
                 final AeroObject obj = objs.get(i);
                 final Airway awy = obj instanceof Airway
                     ? ((Airway) obj)
                     : null;
                 if (awy == null) {
                     // simple
                     route.add(obj);
                 } else {

                     // airway points
                     AeroObject prev = i == 0
                         ? start
                         : objs.get(i - 1);

                     AeroObject next = i >= len - 1
                         ? end
                         : objs.get(i + 1);

                     if (!awy.contains(prev)) {
                         prev = awy.nearestTo(prev);
                     }
                     if (!awy.contains(next)) {
                         next = awy.nearestTo(next);
                     }

                    awy.appendPointsBetween(prev, next, route);
                 }
             }

             route.add(end);
             return route;
         });
    }

    private <T> Observable<T> withStorage(Func1<Storage, Observable<T>> func) {
        return storage.first().flatMap(func);
    }

    public static class Builder {
        Storage storage;
        List<DataSource> sources = new ArrayList<>();

        public Builder storage(Storage storage) {
            this.storage = storage;
            return this;
        }

        public Builder addDataSource(DataSource dataSource) {
            sources.add(dataSource);
            return this;
        }

        public OpenGps build() {
            if (storage == null) throw new IllegalArgumentException("Missing data storage");
            if (sources.isEmpty()) throw new IllegalArgumentException("At least one data source required");
            return new OpenGps(this);
        }
    }
}
