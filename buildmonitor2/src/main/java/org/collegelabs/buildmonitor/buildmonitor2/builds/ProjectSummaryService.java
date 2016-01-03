package org.collegelabs.buildmonitor.buildmonitor2.builds;

import org.collegelabs.buildmonitor.buildmonitor2.storage.BuildTypeWithCredentials;
import org.collegelabs.buildmonitor.buildmonitor2.tc.ServiceHelper;
import org.collegelabs.buildmonitor.buildmonitor2.tc.models.BuildCollectionResponse;
import org.collegelabs.buildmonitor.buildmonitor2.tc.models.BuildDetailsResponse;

import rx.Observable;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 */
public class ProjectSummaryService {

    public static ProjectSummary makeProjectSummary(int sqliteBuildId, String displayName, BuildDetailsResponse response) {
        ProjectSummary summary = new ProjectSummary();
        summary.name = displayName;
        summary.startDate = response.startDate;
        summary.webUrl = response.webUrl;
        summary.status = response.status.equalsIgnoreCase("SUCCESS") ? BuildStatus.Success : BuildStatus.Failure;
        summary.statusText = response.statusText;
        summary.percentageComplete = response.runningInfo != null ? response.runningInfo.percentageComplete : 100;
        summary.isRunning = response.running;
        summary.sqliteBuildId = sqliteBuildId;


        return summary;
    }

    private static final SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd'T'HHmmssZ");

    public Observable<BuildCollectionResponse> getBuilds(BuildTypeWithCredentials buildTypeWithCredentials, int pageSize, int offset, Date sinceDate){

        String buildLocator = "buildType:" + buildTypeWithCredentials.buildType.buildTypeStringId + ",running:any,canceled:any,count:" + pageSize + ",start:" + offset;
        if(sinceDate != null){
            buildLocator += ",sinceDate:" + df.format(sinceDate);
        }

        // statusText + finishDate could be useful additions for TC 9
        final String fields = "build(startDate,status,state,running,id,percentageComplete,number,buildTypeId,href,webUrl)";

        return ServiceHelper.getService(buildTypeWithCredentials.credentials).getBuilds(buildLocator, fields);
    }

    public Observable<BuildDetailsResponse> getMostRecentBuild(BuildTypeWithCredentials buildType){

        String buildLocator = "buildType:" + buildType.buildType.buildTypeStringId + ",running:any,canceled:any";
        return ServiceHelper.getService(buildType.credentials).getBuild(buildLocator);
    }
}
