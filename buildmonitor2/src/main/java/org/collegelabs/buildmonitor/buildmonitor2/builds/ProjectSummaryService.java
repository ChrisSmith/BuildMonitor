package org.collegelabs.buildmonitor.buildmonitor2.builds;

import org.collegelabs.buildmonitor.buildmonitor2.BuildMonitorApplication;
import org.collegelabs.buildmonitor.buildmonitor2.tc.ServiceHelper;
import org.collegelabs.buildmonitor.buildmonitor2.tc.TeamCityService;
import org.collegelabs.buildmonitor.buildmonitor2.tc.models.Build;
import org.collegelabs.buildmonitor.buildmonitor2.tc.models.BuildCollectionResponse;
import org.collegelabs.buildmonitor.buildmonitor2.tc.models.BuildDetailsResponse;
import org.collegelabs.buildmonitor.buildmonitor2.tc.models.BuildType;
import rx.Observable;

import java.util.ArrayList;
import java.util.List;

/**
 */
public class ProjectSummaryService {

    public Observable<List<ProjectSummary>> getSummaries(List<String> buildTypes){
        List<Observable<BuildDetailsResponse>> sources = new ArrayList<>(buildTypes.size());
        for(String buildType : buildTypes){
            sources.add(getMostRecentBuild(buildType));
        }

        return Observable.combineLatest(sources, items -> {
            ArrayList<ProjectSummary> summaries = new ArrayList<>(items.length);

            for(Object obj : items){
                BuildDetailsResponse response = (BuildDetailsResponse) obj;
                summaries.add(makeProjectSummary(response));
            }

            return summaries;
        });
    }

    private ProjectSummary makeProjectSummary(BuildDetailsResponse response) {
        ProjectSummary summary = new ProjectSummary();
        BuildType buildType = response.buildType;
        summary.name = buildType.projectName + " " + buildType.name;
        summary.startDate = response.startDate;
        summary.webUrl = response.webUrl;
        summary.status = response.status.equalsIgnoreCase("SUCCESS") ? BuildStatus.Success : BuildStatus.Failure;
        summary.statusText = response.statusText;
        summary.percentageComplete = response.runningInfo != null ? response.runningInfo.percentageComplete : 100;
        summary.isRunning = response.running;


        return summary;
    }

    public Observable<BuildCollectionResponse> getBuilds(String buildTypeId, int pageSize, int offset){

        String buildLocator = "buildType:" + buildTypeId + ",running:any,canceled:any,count:" + pageSize + ",start:" + offset;

        Observable<TeamCityService> o1 = ServiceHelper.getService(BuildMonitorApplication.Db);

        return o1.flatMap(x -> x.getBuilds(buildLocator));
    }

    public Observable<BuildDetailsResponse> getMostRecentBuild(String buildTypeId){

        String buildLocator = "buildType:" + buildTypeId + ",running:any,canceled:any";
        Observable<TeamCityService> o1 = ServiceHelper.getService(BuildMonitorApplication.Db);

        return o1.flatMap(x -> x.getBuild(buildLocator));
    }
}
