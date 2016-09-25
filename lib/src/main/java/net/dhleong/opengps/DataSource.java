package net.dhleong.opengps;

import net.dhleong.opengps.status.StatusUpdate;

import rx.Observable;
import rx.Observer;

/**
 * @author dhleong
 */
public interface DataSource {
    /** constant, internal ID */
    String id();

    /** user-facing name */
    String name();

    Observable<Boolean> loadInto(Storage storage, Observer<StatusUpdate> updates);
}
