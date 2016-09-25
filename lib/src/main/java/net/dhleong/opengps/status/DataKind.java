package net.dhleong.opengps.status;

/**
 * @author dhleong
 */
public enum DataKind {
    /** Special Kind, provided by the {@link net.dhleong.opengps.Storage} */
    CACHE,

    /** If the DataSource has to download some raw data first */
    RAW,

    /** The raw data we had was out of date, and we're updating */
    RAW_UPDATE,

    AIRPORTS,
    NAVAIDS,
    AIRWAYS,
    CHARTS
}
