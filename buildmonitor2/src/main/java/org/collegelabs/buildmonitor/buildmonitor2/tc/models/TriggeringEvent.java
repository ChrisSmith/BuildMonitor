package org.collegelabs.buildmonitor.buildmonitor2.tc.models;

import android.support.annotation.Nullable;

import java.util.Date;

/**
 */
public class TriggeringEvent {
    public String type, details;
    public Date date;

    @Nullable
    public User user;

    @Nullable
    public BuildType buildType;
}
