package org.collegelabs.buildmonitor.buildmonitor2.tc.models;

import com.google.gson.annotations.SerializedName;

/**
 */
public class Revision {
    public String version;

    @SerializedName("vcs-root-instance")
    public VcsRoot vcsRoot;
}
