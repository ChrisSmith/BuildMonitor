package org.collegelabs.buildmonitor.buildmonitor2.tc.models;

import com.google.gson.annotations.SerializedName;

/**
 */
public class VcsRoot {
    public String id, name, href;

    @SerializedName("vcs-root-id")
    public String vcsRootId;
}
