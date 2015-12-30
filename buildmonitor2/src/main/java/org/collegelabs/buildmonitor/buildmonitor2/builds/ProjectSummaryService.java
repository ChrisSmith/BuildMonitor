package org.collegelabs.buildmonitor.buildmonitor2.builds;

import org.collegelabs.buildmonitor.buildmonitor2.BuildMonitorApplication;
import org.collegelabs.buildmonitor.buildmonitor2.storage.BuildTypeDto;
import org.collegelabs.buildmonitor.buildmonitor2.storage.BuildTypeWithCredentials;
import org.collegelabs.buildmonitor.buildmonitor2.tc.ServiceHelper;
import org.collegelabs.buildmonitor.buildmonitor2.tc.TeamCityService;
import org.collegelabs.buildmonitor.buildmonitor2.tc.models.Build;
import org.collegelabs.buildmonitor.buildmonitor2.tc.models.BuildCollectionResponse;
import org.collegelabs.buildmonitor.buildmonitor2.tc.models.BuildDetailsResponse;
import org.collegelabs.buildmonitor.buildmonitor2.tc.models.BuildType;
import rx.Observable;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 */
public class ProjectSummaryService {

    public static ProjectSummary makeProjectSummary(int buildId, String displayName, BuildDetailsResponse response) {
        ProjectSummary summary = new ProjectSummary();
        summary.name = displayName;
        summary.startDate = response.startDate;
        summary.webUrl = response.webUrl;
        summary.status = response.status.equalsIgnoreCase("SUCCESS") ? BuildStatus.Success : BuildStatus.Failure;
        summary.statusText = response.statusText;
        summary.percentageComplete = response.runningInfo != null ? response.runningInfo.percentageComplete : 100;
        summary.isRunning = response.running;
        summary.buildId = buildId;


        return summary;
    }

    public Observable<BuildCollectionResponse> getBuilds(BuildTypeWithCredentials buildTypeWithCredentials, int pageSize, int offset){

        String buildLocator = "buildType:" + buildTypeWithCredentials.buildType.buildTypeStringId + ",running:any,canceled:any,count:" + pageSize + ",start:" + offset;
        return ServiceHelper.getService(buildTypeWithCredentials.credentials).getBuilds(buildLocator);
    }

    public Observable<BuildDetailsResponse> getMostRecentBuild(BuildTypeWithCredentials buildType){

        String buildLocator = "buildType:" + buildType.buildType.buildTypeStringId + ",running:any,canceled:any";
        return ServiceHelper.getService(buildType.credentials).getBuild(buildLocator);
    }
}
