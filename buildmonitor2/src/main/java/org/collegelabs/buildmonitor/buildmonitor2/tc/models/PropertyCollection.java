package org.collegelabs.buildmonitor.buildmonitor2.tc.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 */
public class PropertyCollection {

    @SerializedName("property")
    public List<Property> properties;
}
