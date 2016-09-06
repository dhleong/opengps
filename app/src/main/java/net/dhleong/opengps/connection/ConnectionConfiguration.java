package net.dhleong.opengps.connection;

/**
 * @author dhleong
 */
public class ConnectionConfiguration {
    public final ConnectionType type;
    public final String host;
    public final int port;

    public ConnectionConfiguration(ConnectionType connectionType, String host, int port) {
        this.type = connectionType;
        this.host = host;
        this.port = port;
    }
}
