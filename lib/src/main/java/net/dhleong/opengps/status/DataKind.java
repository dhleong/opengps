package net.dhleong.opengps.status;

/**
 * @author dhleong
 */
public enum DataKind {
    /**
     * Special Kind, provided by the OpenGps when the Storage
     *  has finished loading
     */
    STORAGE_READY,

    /**
     * Another special Kind, provided by OpenGps, when all sources
     *  have finished their init
     */
    ALL_READY,

    /** Indicates that a data source is *ready* */
    READY,

    /** The DataSource has to download some raw data first */
    RAW_INIT,

    /** If the DataSource had to download some raw data first, it's done */
    RAW_FETCHED,

    /** The raw data we had was out of date, and we're updating */
    RAW_UPDATE,

    AIRPORTS,
    NAVAIDS,
    AIRWAYS,
    CHARTS
}
