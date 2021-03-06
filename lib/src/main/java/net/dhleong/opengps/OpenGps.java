package net.dhleong.opengps;

import net.dhleong.opengps.exc.GpsInitException;
import net.dhleong.opengps.status.DataKind;
import net.dhleong.opengps.status.StatusUpdate;
import net.dhleong.opengps.storage.DelegateStorage;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;
import rx.subjects.ReplaySubject;
import rx.subjects.SerializedSubject;

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

    public interface OnErrorListener {
        void onError(Throwable e);
    }

    BehaviorSubject<Storage> storage = BehaviorSubject.create();
    SerializedSubject<StatusUpdate, StatusUpdate> updates =
        ReplaySubject.<StatusUpdate>create()
            .toSerialized();

    private OpenGps(Builder builder, Storage myStorage) {
        // begin eager load
        myStorage
            .load()
            .doOnNext(any -> updates.onNext(new StatusUpdate(null, DataKind.STORAGE_READY)))
            .flatMap(s ->
                Observable.from(builder.sources)
                          .flatMap(dataSource -> {
                              if (!s.hasDataSource(dataSource)) {
                                  return dataSource
                                      .loadInto(s, updates)
                                      .toObservable()
                                      .map(any -> s);
                              } else {
                                  // already loaded into source
                                  return Observable.just(s);
                              }
                          })
            ).last()
            .doOnNext(any -> updates.onNext(new StatusUpdate(null, DataKind.ALL_READY)))
            .subscribe(storage::onNext, e -> {
                if (builder.onError != null) {
                    builder.onError.onError(e);
                } else if (builder.throwOnError) {
                    throw new GpsInitException(e);
                }

                // always notify status updates
                updates.onError(e);
        });
    }

    public Observable<StatusUpdate> statusUpdates() {
        return updates;
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

    public Observable<Airway> airwaysFor(AeroObject object) {
        return withStorage(storage -> storage.airwaysFor(object));
    }

    public Observable<List<ChartInfo>> chartsFor(Airport airport) {
        return withStorage(storage -> storage.chartsFor(airport));
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

    public Observable<PreferredRoute> preferredRoutes(Airport origin, Airport dest) {
        return withStorage(storage -> storage.preferredRoutes(origin, dest));
    }

    private <T> Observable<T> withStorage(Func1<Storage, Observable<T>> func) {
        return storage.first().flatMap(func);
    }

    public static class Builder {
        DelegateStorage.Builder storageBuilder = new DelegateStorage.Builder();
        List<DataSource> sources = new ArrayList<>();
        OnErrorListener onError;
        boolean throwOnError = true;

        public Builder addStorage(Storage storage) {
            this.storageBuilder.add(storage);
            return this;
        }

        public Builder addDataSource(DataSource dataSource) {
            sources.add(dataSource);
            return this;
        }

        public Builder onError(OnErrorListener errorListener) {
            this.onError = errorListener;
            return this;
        }

        /**
         * By default, if an exception is raised while initializing
         *  OpenGps, it will be thrown if {@link #onError(OnErrorListener)}
         *  has not been provided, in addition to being emitted on the
         *  {@link #statusUpdates()} Observable. If you call this, however,
         *  any exceptions will never be thrown, and instead will only emitted
         *  on {@link #statusUpdates()} and passed to {@link #onError(OnErrorListener)}
         *  if provided.
         *
         * In other words, if you plan to listen to {@link #statusUpdates()}
         *  to handle loading issues, you probably want to call this so the
         *  exception isn't also *thrown* where you can't catch it.
         */
        public Builder dontThrowOnError() {
            this.throwOnError = false;
            return this;
        }

        public OpenGps build() {
            if (sources.isEmpty()) throw new IllegalArgumentException("At least one data source required");
            final Storage storage = storageBuilder.build(); // throws exceptions if needed
            return new OpenGps(this, storage);
        }
    }
}
