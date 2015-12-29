package org.collegelabs.buildmonitor.buildmonitor2.tc;

public class Credentials {
    public String username, password, server;
    public boolean isGuest;
    public int id;

    public Credentials(int id, String server) {
        this.id = id;
        this.isGuest = true;
        this.server = server;
    }

    public Credentials(int id, String username, String password, String server) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.server = server;
    }

    @Override
    public String toString() {
        return (isGuest ? "guest" : username) + " @ " + server;
    }
}
