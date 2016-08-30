package net.dhleong.opengps.feat.airway;

import net.dhleong.opengps.AeroObject;
import net.dhleong.opengps.Airway;

/**
 * @author dhleong
 */
public class AirwaySearchResult {
    public final Airway airway;
    public final AeroObject entry, exit;

    public AirwaySearchResult(Airway airway, AeroObject entry, AeroObject exit) {
        this.airway = airway;
        this.entry = entry;
        this.exit = exit;
    }
}
