package org.collegelabs.buildmonitor.buildmonitor2.tc;

import org.collegelabs.buildmonitor.buildmonitor2.tc.models.BuildCollectionResponse;
import org.collegelabs.buildmonitor.buildmonitor2.tc.models.BuildDetailsResponse;
import org.collegelabs.buildmonitor.buildmonitor2.tc.models.ProjectResponse;

import retrofit.client.Response;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;
import rx.Observable;

public interface TeamCityService {

    String PREFIX = "/app/rest";

    @GET(PREFIX + "/projects/id:{projectId}")
    Observable<ProjectResponse> getProject(@Path("projectId") String projectId);

    @GET(PREFIX + "/builds")
    Observable<BuildCollectionResponse> getBuilds(@Query("locator") String locator, @Query("fields") String fields);

    @GET(PREFIX + "/builds/{buildLocator}")
    Observable<BuildDetailsResponse> getBuild(@Path("buildLocator") String locator);

    @GET(PREFIX + "/builds/{id}")
    Observable<BuildDetailsResponse> getBuild(@Path("id") int id);

    @GET(PREFIX + "/buildTypes")
    Observable<BuildTypeCollectionResponse> getBuildTypes();

    @GET(PREFIX + "/server")
    Observable<ServerResponse> getServer();

    @GET("/get/tests/buildId/{buildId}/tests-{buildId}.csv")
    Observable<Response> getTestResults(@Path("buildId") int buildId);

    @GET("/downloadBuildLog.html")
    Observable<Response> getLog(@Query("buildId") int buildId);
}

