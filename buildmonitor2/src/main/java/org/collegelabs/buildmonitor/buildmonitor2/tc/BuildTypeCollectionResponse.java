package org.collegelabs.buildmonitor.buildmonitor2.tc;

import com.google.gson.annotations.SerializedName;
import org.collegelabs.buildmonitor.buildmonitor2.tc.models.BuildType;

import java.util.ArrayList;
import java.util.List;

/**
 */
public class BuildTypeCollectionResponse {

    @SerializedName("buildType")
    public List<BuildType> buildTypes;

    public BuildTypeCollectionResponse(){
        buildTypes = new ArrayList<>();
    }
}
