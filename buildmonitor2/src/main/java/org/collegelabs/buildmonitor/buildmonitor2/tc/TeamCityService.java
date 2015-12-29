package org.collegelabs.buildmonitor.buildmonitor2.tc;

import org.collegelabs.buildmonitor.buildmonitor2.tc.models.BuildCollectionResponse;
import org.collegelabs.buildmonitor.buildmonitor2.tc.models.BuildDetailsResponse;
import org.collegelabs.buildmonitor.buildmonitor2.tc.models.ProjectResponse;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;
import rx.Observable;

public interface TeamCityService {

    @GET("/projects/id:{projectId}")
    Observable<ProjectResponse> getProject(@Path("projectId") String projectId);

    @GET("/builds")
    Observable<BuildCollectionResponse> getBuilds(@Query("locator") String locator);

    @GET("/builds/{buildLocator}")
    Observable<BuildDetailsResponse> getBuild(@Path("buildLocator") String locator);

    @GET("/buildTypes")
    Observable<BuildTypeCollectionResponse> getBuildTypes();

    @GET("/server")
    Observable<ServerResponse> getServer();
}

