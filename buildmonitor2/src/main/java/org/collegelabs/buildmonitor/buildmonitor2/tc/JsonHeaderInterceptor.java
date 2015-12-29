package org.collegelabs.buildmonitor.buildmonitor2.tc;


public class JsonHeaderInterceptor implements retrofit.RequestInterceptor {

    @Override
    public void intercept(RequestFacade request) {
        request.addHeader("Accept", "application/json");
    }
}
