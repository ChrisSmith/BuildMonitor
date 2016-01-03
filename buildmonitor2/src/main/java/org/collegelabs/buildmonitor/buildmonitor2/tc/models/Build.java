package org.collegelabs.buildmonitor.buildmonitor2.tc.models;

import org.collegelabs.buildmonitor.buildmonitor2.builds.BuildStatus;

import java.util.Date;

/**
 */
public class Build {

    public int id;
    public int percentageComplete;
    public boolean running;
    public String number;
    public String status;
    public String buildTypeId;
    public String href;
    public String webUrl;
    public Date startDate;

    public BuildStatus getBuildStatus(){
        return status.equalsIgnoreCase("SUCCESS")
                ? BuildStatus.Success
                : BuildStatus.Failure;
    }
}

