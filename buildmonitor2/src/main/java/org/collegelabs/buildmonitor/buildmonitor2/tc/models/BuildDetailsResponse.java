package org.collegelabs.buildmonitor.buildmonitor2.tc.models;

import android.support.annotation.Nullable;
import com.google.gson.annotations.SerializedName;

import java.util.Date;

/**
 */
public class BuildDetailsResponse {

    @SerializedName("id")
    public int buildId;

    public String number; // ?

    public Date startDate;
    public Date finishDate;

    public String status;
    public String statusText;

    public String href;
    public String webUrl;

    public boolean personal;
    public boolean history;
    public boolean pinned;
    public boolean running;

    public BuildType buildType;
    public Agent agent;

    @Nullable
    @SerializedName("running-info")
    public RunningInfo runningInfo;

    @SerializedName("properties")
    public PropertyCollection propertyCollection;

    @SerializedName("snapshot-dependencies")
    public SnapshotDependencies snapshotDependencies;

    @SerializedName("artifact-dependencies")
    public SnapshotDependencies artifactDependencies;

    @SerializedName("revisions")
    public RevisionCollection revisionCollection;

    @SerializedName("triggered")
    public TriggeringEvent triggeringEvent;

    public Changes changes;
}


