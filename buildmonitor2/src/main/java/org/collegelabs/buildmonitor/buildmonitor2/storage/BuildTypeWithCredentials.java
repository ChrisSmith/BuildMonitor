package org.collegelabs.buildmonitor.buildmonitor2.storage;

import org.collegelabs.buildmonitor.buildmonitor2.tc.Credentials;

/**
 */
public class BuildTypeWithCredentials {
    public final BuildTypeDto buildType;
    public final Credentials credentials;

    public BuildTypeWithCredentials(BuildTypeDto buildType, Credentials credentials) {
        this.buildType = buildType;
        this.credentials = credentials;
    }
}
