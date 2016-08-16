package net.dhleong.opengps;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.functions.Func1;
import rx.subjects.BehaviorSubject;

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
        return withStorage(s ->
            s.find(objectId));
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

    public Observable<AeroObject> anyNear(double lat, double lng, double range) {
        // TODO
        return Observable.empty();
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
