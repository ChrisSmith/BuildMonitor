package org.collegelabs.buildmonitor.buildmonitor2.builds;

import java.util.Date;

/**
 */
public class ProjectSummary {
    public BuildStatus status;
    public String statusText;
    public String name;
    public Date startDate;
    public boolean isRunning;
    public int percentageComplete;
    public String webUrl;
    public int sqliteBuildId;
}
