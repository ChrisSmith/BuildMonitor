package org.collegelabs.buildmonitor.buildmonitor2.tc;

import org.collegelabs.buildmonitor.buildmonitor2.tc.models.BuildCollectionResponse;
import org.collegelabs.buildmonitor.buildmonitor2.tc.models.BuildDetailsResponse;
import org.collegelabs.buildmonitor.buildmonitor2.tc.models.ProjectResponse;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;
import rx.Observable;

public interface TeamCityService {

    @GET("/app/rest/projects/id:{projectId}")
    Observable<ProjectResponse> getProject(@Path("projectId") String projectId);

    @GET("/app/rest/builds")
    Observable<BuildCollectionResponse> getBuilds(@Query("locator") String locator);

    @GET("/app/rest/builds/{buildLocator}")
    Observable<BuildDetailsResponse> getBuild(@Path("buildLocator") String locator);
}

