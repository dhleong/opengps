package net.dhleong.opengps;

/**
 * @author dhleong
 */
public interface AeroObject {

    String id();
    String name();

    double lat();
    double lng();

    /** @return distance in nm */
    float distanceTo(AeroObject other);
}
