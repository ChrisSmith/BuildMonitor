package org.collegelabs.buildmonitor.buildmonitor2.tc.models;

public class Project {
    public String id, name, href;

    public Project(){}

    @Override
    public String toString() {
        return String.format("%s %s %s", id, name, href);
    }
}
