package org.collegelabs.buildmonitor.buildmonitor2.buildhistory;

import org.collegelabs.buildmonitor.buildmonitor2.builds.BuildStatus;
import org.collegelabs.buildmonitor.buildmonitor2.builds.ProjectSummaryService;
import org.collegelabs.buildmonitor.buildmonitor2.buildstatus.BuildChainObservable;
import org.collegelabs.buildmonitor.buildmonitor2.storage.BuildTypeWithCredentials;
import org.collegelabs.buildmonitor.buildmonitor2.tc.ServiceHelper;
import org.collegelabs.buildmonitor.buildmonitor2.tc.TcUtil;
import org.collegelabs.buildmonitor.buildmonitor2.tc.TeamCityService;
import org.collegelabs.buildmonitor.buildmonitor2.tc.models.Build;
import org.collegelabs.buildmonitor.buildmonitor2.tc.models.BuildCollectionResponse;
import org.collegelabs.buildmonitor.buildmonitor2.tc.models.BuildDetailsResponse;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;

public class StatisticsService {

    public Observable<BuildDetailsResponse> getRecentFailedBuilds(BuildTypeWithCredentials build){

        GregorianCalendar cal = new GregorianCalendar();
        cal.add(Calendar.MONTH, -2);

        TeamCityService tcService = ServiceHelper.getService(build.credentials);

        // get the most recent 1K builds in the last 2 months
        ProjectSummaryService summaryService = new ProjectSummaryService();

        final BuildDetailsResponse errorResponse = new BuildDetailsResponse();
        errorResponse.status = "SUCCESS"; // TODO transform this into a view model so it has a proper status

        return summaryService.getBuilds(build, 1000, 0, cal.getTime())
            .flatMap(b -> Observable.from(b.builds))
            .filter(b -> TcUtil.getBuildStatus(b.status) == BuildStatus.Failure)
            .flatMap(b ->

                    BuildChainObservable.create(tcService, b.id, x -> TcUtil.getBuildStatus(x.status) == BuildStatus.Failure)

                        .onErrorReturn(t -> errorResponse)
            )
            .filter(b -> TcUtil.getBuildStatus(b.status) == BuildStatus.Failure)
            .filter(b -> !b.statusText.toLowerCase().contains("snapshot"))
        ;
    }
}
