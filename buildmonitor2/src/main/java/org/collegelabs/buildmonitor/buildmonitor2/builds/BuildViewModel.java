package org.collegelabs.buildmonitor.buildmonitor2.builds;

import org.collegelabs.buildmonitor.buildmonitor2.tc.models.Build;
import org.collegelabs.buildmonitor.buildmonitor2.util.TimeUtil;

import java.util.Date;

/**
 */
public class BuildViewModel {

    public final Date startDate;
    public final String status;
    public final String summary;
    public final String percentageComplete;
    public final boolean isRunning;
    public final boolean isEmpty;
    public final BuildStatus buildStatus;
    public final String webUrl;

    public BuildViewModel(){
        startDate = new Date();
        status = "No Builds";
        summary = "";
        isRunning = false;
        percentageComplete = "";
        isEmpty = true;
        buildStatus = BuildStatus.Success;
        webUrl = "";
    }

    public BuildViewModel(Build build){
        startDate = build.startDate;
        summary = getSummary(build);
        isRunning = build.running;
        percentageComplete = build.percentageComplete + "%";
        isEmpty = false;
        buildStatus = build.status.equalsIgnoreCase("SUCCESS") ? BuildStatus.Success : BuildStatus.Failure;
        status = build.status;
        webUrl = build.webUrl;
    }

    private String getSummary(Build build) {
        String humanTime = TimeUtil.human(build.startDate);
        if(build.running){
            humanTime += " " + build.percentageComplete + "%";
        }
        return humanTime;
    }
}
