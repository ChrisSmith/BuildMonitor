package org.collegelabs.buildmonitor.buildmonitor2.tc.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
*/
public class BuildCollectionResponse {
    public int count;
    public String nextHref;

    @SerializedName("build")
    public List<Build> builds;
}
