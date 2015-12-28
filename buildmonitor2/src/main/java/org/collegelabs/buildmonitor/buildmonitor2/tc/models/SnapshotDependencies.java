package org.collegelabs.buildmonitor.buildmonitor2.tc.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 */
public class SnapshotDependencies {
    public int count;

    @SerializedName("build")
    public List<Build> builds;
}
