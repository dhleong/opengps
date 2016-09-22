package net.dhleong.opengps.nasr;

import net.dhleong.opengps.AbstractStorage;
import net.dhleong.opengps.Airport;
import net.dhleong.opengps.DataSource;
import net.dhleong.opengps.PreferredRoute;

import rx.Observable;

/**
 * MUST be installed (as well as a {@link NasrTextDataSource})
 *  to be able to query {@link PreferredRoute}s from the NASR dataset
 * @author dhleong
 * @see net.dhleong.opengps.storage.DelegateStorage
 */
public class NasrPreferredRoutesStorage extends AbstractStorage {

    NasrTextDataSource source;

    @Override
    public void finishSource(DataSource source) {
        if (source instanceof NasrTextDataSource) {
            this.source = (NasrTextDataSource) source;
        }
    }

    @Override
    public Observable<PreferredRoute> preferredRoutes(Airport origin, Airport dest) {
        return source.preferredRoutes(origin, dest);
    }
}
