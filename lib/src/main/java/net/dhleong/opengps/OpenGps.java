package net.dhleong.opengps;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;

public class OpenGps {

    private OpenGps(Builder builder) {

    }

    public Observable<AeroObject> find(String objectId) {
        return Observable.empty();
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
        return Observable.empty();
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
