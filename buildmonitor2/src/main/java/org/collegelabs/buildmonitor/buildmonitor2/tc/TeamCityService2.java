package org.collegelabs.buildmonitor.buildmonitor2.tc;

import retrofit.client.Response;
import retrofit.http.GET;
import retrofit.http.Path;
import rx.Observable;

public interface TeamCityService2 {

    @GET("/get/tests/buildId/{buildId}/tests-{buildId}.csv")
    Observable<Response> getTestResults(@Path("buildId") int buildId);
}

