package net.dhleong.opengps;

import net.dhleong.opengps.status.StatusUpdate;

import rx.Observer;
import rx.Single;

/**
 * @author dhleong
 */
public interface DataSource {
    /** constant, internal ID */
    String id();

    /** user-facing name */
    String name();

    /**
     * Load data into the given Storage, emitting
     *  status updates to `updates`
     *
     * @return A Single which emits this DataSource
     *  again on success, or an Error on failure
     */
    Single<? extends DataSource> loadInto(Storage storage, Observer<StatusUpdate> updates);
}
