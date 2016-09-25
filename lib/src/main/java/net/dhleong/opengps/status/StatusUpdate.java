package net.dhleong.opengps.status;

import net.dhleong.opengps.DataSource;

/**
 * @author dhleong
 */
public class StatusUpdate {
    public DataSource source;
    public DataKind kind;

    public StatusUpdate(DataSource source, DataKind kind) {
        this.source = source;
        this.kind = kind;
    }
}
